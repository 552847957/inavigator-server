package ru.sberbank.syncserver2.service.pushnotifications;

import org.springframework.beans.factory.annotation.Autowired;
import ru.sberbank.syncserver2.gui.db.dao.PushNotificationDao;
import ru.sberbank.syncserver2.gui.db.dao.PushNotificationDao.SourcePushStatus;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.datamappers.DatapowerDataResponseHandler;
import ru.sberbank.syncserver2.service.datamappers.DatapowerObjectMapperRequester;
import ru.sberbank.syncserver2.service.generator.ClusterManager;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotification;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotificationListener;
import ru.sberbank.syncserver2.service.pushnotifications.senders.BaseNotificationSender.PushedNotificationInfo;
import ru.sberbank.syncserver2.service.sql.DataPowerService;
import ru.sberbank.syncserver2.service.sql.query.*;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest.Arguments.Argument;
import ru.sberbank.syncserver2.util.XMLHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataPowerPushnotificationsUploader extends SingleThreadBackgroundService implements PushNotificationListener{

	private PushNotificationDao pushNotificationDao;
	private String connectionService;
	private DataPowerService DPService;
	private String sqlForSend = "INAVIGATOR20.GET_NOTIFICATIONS_FOR_SEND";
	private String sqlForUpdate = "INAVIGATOR20.UPDATE_NOTIFICATION_SEND_STATUS";
	private String appName = "INAVIGATOR20";


	// id успешно отправленных сообщений для обновления в БД источнике через DataPower
	private ConcurrentLinkedQueue<Long> successfulIds = new ConcurrentLinkedQueue<Long>();

	// id сообщений, отправленных с ошибкой, для обновления в БД источнике через DataPower
	private ConcurrentLinkedQueue<Long> failedIds = new ConcurrentLinkedQueue<Long>();

	private ClusterManager clusterManager;

	@Autowired
	public void setPushNotificationDao(PushNotificationDao pushNotificationDao) {
		this.pushNotificationDao = pushNotificationDao;
	}

	public DataPowerPushnotificationsUploader() {
		super(60);
	}

	@Override
	public void doInit() {
		//запуск DataPowerPushnotificationsUploader должен быть после PushNotificationService(старый способ обновления статусов)
		/*PushNotificationService PNService = (PushNotificationService)ServiceManager.getInstance().findFirstServiceByClassCode(PushNotificationService.class);
		if (PNService != null)
			PNService.addListener(this);*/

		//DaptaPower также уже должен быть проинициализирован
		DPService = (DataPowerService)ServiceManager.getInstance().findFirstServiceByClassCode(DataPowerService.class);
		//DPService = new DPTest(); // test without datapower

		clusterManager = (ClusterManager) ServiceManager.getInstance().findFirstServiceByClassCode(ClusterManager.class);
	}

	private boolean isHostActive() {
		return (clusterManager == null || clusterManager.isActive());
	}

	@Override
	public void doRun() {
		//if (true) return; // заглушка для localhost
		// продолжаем только если этот узел кластера активный
		if (!isHostActive())
			return;

		try {
			getStatusesForUpdate();
		} catch (Exception e) {
			tagLogger.log("Ошибка при загрузке статусов уведомлений для обновления: "+e.getMessage());
			logger.error(e, e);
		}

		// сначала обновляем статусы отправленых уведомлений
		updateNotificationSendStatuses(failedIds, 4);
		if (shouldInternalTaskStop())
			return;

		// сначала со статусами 4, потом со статусами 3, чтобы конечный статус остался 3
		updateNotificationSendStatuses(successfulIds, 3);
		if (shouldInternalTaskStop())
			return;

		try {
			// потом запрашиваем сообщения для отправки
			loadNotificationsForSend();
		} catch (Exception e) {
			tagLogger.log("Ошибка при загрузке уведомлений: "+e.getMessage());
			logger.error(e, e);
		}
	}

	void getStatusesForUpdate() {
		for (SourcePushStatus status: pushNotificationDao.getStatusesForUpdate()) {
			if (status.getSuccessCount() == 0)
				failedIds.add(status.getId());
			else
				successfulIds.add(status.getId());
		}
	}

	@Override
	public void pushNotificationsWasSent(PushResult result) {
		if (result.isFailed()) {
			for (PushNotification notification: result.getNotifications()) {
				pushNotificationsWasSent(notification);
			}
		} else {
			for (PushedNotificationInfo notification: result.getPushed()) {
				pushNotificationsWasSent(notification);
			}
		}
	}

	/**
	 * поставить всем уведомлениям статус 4
	 * @param notification
	 */
	private void pushNotificationsWasSent(PushNotification notification) {
		if (notification.getSourceId() != null) {
			failedIds.add(notification.getSourceId());
		}
	}

	private void pushNotificationsWasSent(PushedNotificationInfo notification) {
		Long notificationSourceId = notification.getNotification().getSourceId();
		if (notificationSourceId == null)
			return;
		if (notification.isFailed()) {
			failedIds.add(notificationSourceId);
		} else {
			successfulIds.add(notificationSourceId);
		}
	}

	/**
	 * Для каждого уведомления(id) в очереди q выставить статус status
	 * @param q
	 * @param status
	 * @throws IOException
	 */
	private void updateNotificationSendStatuses(Queue<Long> q, int status) {
		if (q.isEmpty())
			return;
		List<Argument> arguments = new ArrayList<OnlineRequest.Arguments.Argument>();
		arguments.add(new Argument(1, FieldType.NUMBER, "-1"));
		arguments.add(new Argument(2, FieldType.NUMBER, String.valueOf(status)));
		arguments.add(new Argument(3, FieldType.STRING, LOCAL_HOST_NAME));
		Long id;
		Exception exception = null;
		List<Long> faildUpdates = new ArrayList<Long>(); // список сообщений, статус которых поменять не удалось.
		while ((id = q.poll())!=null) {
			//переиспользуем список
			arguments.get(0).setValue(id.toString());
			try {
				if (updateNotificationSendStatus(arguments, exception == null))
					faildUpdates.add(id);
			} catch (IOException e) {
				exception = e;
				faildUpdates.add(id);
			}
		}
		if (exception != null) {
			tagLogger.log("Не удалось обновить статус "+ faildUpdates.size() +" сообщений(я): "+exception.getMessage());
			logger.error(exception, exception);
			q.addAll(faildUpdates);
		}
	}

	/**
	 *
	 * @param arguments
	 * @param throwException бросить исключение, если вернулась ошибка
	 * @return обновился ли статус
	 * @throws IOException
	 */
	private boolean updateNotificationSendStatus(List<Argument> arguments, final boolean throwException) throws IOException {
		final boolean[] result = {true};
		new DatapowerObjectMapperRequester<Void>().request(
				sqlForUpdate,
				arguments,
				connectionService,
				DPService,
				new DatapowerDataResponseHandler() {
					@Override
					public void handleDataResponse(DataResponse dataset) throws IOException {
						if (dataset.getResult() != Result.OK) {
							if (throwException)
								throw new IOException("Ошибка при обновлении статуса: "+dataset.getError());
							else
								result[0] = false;
						}
					}
		});
		return result[0];
	}

	/**
	 * Обновить статус сообщения
	 * @param id
	 * @param status
	 * @throws IOException
	 */
	private void updateNotificationSendStatus(long id, int status) throws IOException {
		List<Argument> arguments = new ArrayList<OnlineRequest.Arguments.Argument>();
		arguments.add(new Argument(1, FieldType.NUMBER, String.valueOf(id)));
		arguments.add(new Argument(2, FieldType.NUMBER, String.valueOf(status)));
		arguments.add(new Argument(3, FieldType.STRING, LOCAL_HOST_NAME));
		updateNotificationSendStatus(arguments, true);
	}

	/**
	 * загрузить push уведомления из альфы через DataPower и добавить их в очередь на отправку
	 * @throws IOException
	 */
	private void loadNotificationsForSend() throws IOException {
		new DatapowerObjectMapperRequester<Void>().request(
				sqlForSend,
				null,
				connectionService,
				DPService,
				new DatapowerDataResponseHandler() {
					@Override
					public void handleDataResponse(DataResponse dataset) throws IOException {
						int sum = 0; // сумарное количество уведомлений

						if (dataset.getResult() == Result.OK) {
							//если нет сообщений, ничего не делаем
							if (dataset.getDataset().getRows()==null)
								return;

							List<Long> ids = new ArrayList<Long>();

							// сначала добавляем все уведомления в очередь
							for (DatasetRow row: dataset.getDataset().getRows()) {
								Long outerId =Long.valueOf(row.getValues().get(0));
								String msg = row.getValues().get(1);
								//int type = Integer.parseInt(row.getValues().get(2));
								String params = row.getValues().get(3);
								String email = row.getValues().get(4);
								//Date date = row.getValues().get(5);
								Integer badge;
								try {
									badge = row.getValues().get(6).trim().toUpperCase().equals("NULL") ? 0 : Integer.valueOf(row.getValues().get(6).trim());
								} catch (Exception e) {
									badge = 0;
								}

								PushNotification notification = new PushNotification(null,msg, badge);
								notification.setCustomParameters(params);

								String[] emailArray = parseStringEmail(email);
								int count = pushNotificationDao.addPushNotificationToQueue(outerId, notification, null, appName, null, emailArray,null);

								if (count < 1) {
									//если пользователи не найдены, то уведомление не добавилось, поэтому ставим статус 4 (отправлено с ошибкой)
									failedIds.add(outerId);
									logger.info("Не найдены пользователи "+ email + " для приложения " + appName);
									continue;
								} else {
									sum += count;
									ids.add(outerId);
								}
							}

							//уведомления выгружены в подсистему отправки
							try {
								for (Long id: ids) {
									updateNotificationSendStatus(id, 2);
								}
							} catch (IOException e) {
								tagLogger.log(e.getMessage());
								logger.error(e, e);
								// эта ошибка некритичная, т.к. в дальнейшем может проставиться статус 3/4
							}

							//логируем количество, если были добавлены сообщения
							if (sum>0)
								tagLogger.log("Было загружено " + ids.size() + " сообщений в очередь, "+ sum + " пользователям будут отправлены уведомления.");
						} else {
							tagLogger.log("Ошибка при загрузке уведомлений: "+dataset.getError());
						}

					}
				});
	}

	public String getConnectionService() {
		return connectionService;
	}


	public void setConnectionService(String connectionService) {
		this.connectionService = connectionService;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	/**
	 * Распарсить email по ";" или вернуть пустой масив, если строка равна "ALL"
	 * @param email
	 * @return
	 */
	private static String[] parseStringEmail(String email) {
		if (email == null || email.equals("") || email.trim().toUpperCase().equals("ALL"))
			return new String[0];
		return email.split(";");
	}

	public static void main(String[] args) {
		System.out.println(parseStringEmail("asdf;as").length);
	}

	private void test() throws IOException {
		PushNotification notification = new PushNotification(null, "haha", 0);

		int count = pushNotificationDao.addPushNotificationToQueue(1L, notification, null, "competitors", null, null,null);
		System.out.println(count+" "+notification.getNotificationId());
	}

	/**
	 *  класс - заглушка для ДП
	 * @author Светлана
	 *
	 */
	private class DPTest extends DataPowerService{
		private int t = 1;
		@Override
		public DataResponse request(OnlineRequest request) {
			if (request.getStoredProcedure().equals(sqlForSend)) {
				DataResponse dr = new DataResponse();
				dr.setResult(Result.OK);
				Dataset ds = new Dataset();
				for (int j = 1; j < 4 ; j++) {
					DatasetRow dsr = new DatasetRow();

					dsr.addValue(String.valueOf(j));
					dsr.addValue("HAHA");
					dsr.addValue("1");
					dsr.addValue("");
					dsr.addValue("ALL");
					dsr.addValue("2016-04-18");
					ds.addRow(dsr);
				}

				dr.setDataset(ds);
				if (t++>3)
					dr.setResult(Result.FAIL_DB);
				return dr;
			} else {
				System.out.println("++++++++++++++++++++++++++++++++ \n"+XMLHelper.writeXMLToString(request, true, OnlineRequest.class));
				DataResponse dr = new DataResponse();
				dr.setResult(Result.OK);
				dr.setError("EEEEE");
				return dr;
			}

		}

	}
}
