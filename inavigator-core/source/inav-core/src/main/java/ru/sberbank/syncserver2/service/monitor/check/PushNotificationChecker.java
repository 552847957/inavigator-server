package ru.sberbank.syncserver2.service.monitor.check;

import javapns.Push;

import java.security.KeyStore;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class PushNotificationChecker extends AbstractCheckAction {

	private String connectionService;
	private String sqlForMonitor = "INAVIGATOR20.GET_NOTIFICATIONS_FOR_MONITOR";
	private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
	public static final String SUB_CHECK_RESULT_CODE = "PUSH_NOTIFICATION_IS_NOT_SENT_";
	public static final String CHECK_RESULT_CODE_APPLE_SERVER_IS_NOT_AVAILABLE = "APPLE_SERVER_IS_NOT_AVAILABLE";

	@Override
	protected void doStart() {
		clearAllCheckResults();
		super.doStart();
	}

	@Override
	protected List<CheckResult> doCheck() {
		final List<CheckResult> result = new ArrayList<CheckResult>();
		if (!checkAppleServerAvailability()) {
			result.add(new CheckResult(CHECK_RESULT_CODE_APPLE_SERVER_IS_NOT_AVAILABLE, false, LOCAL_HOST_NAME+" says: can't connect to Apple server, push notifications will not be sent."));
			return result;
		} else {
			if (addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_APPLE_SERVER_IS_NOT_AVAILABLE, LOCAL_HOST_NAME+" says: connection to Apple server has been restored."))
				tagLogger.log("Apple сервер доступен.");
		}

		/*try {
			DataPowerService DPService = (DataPowerService)ServiceManager.getInstance().findFirstServiceByClassCode(DataPowerService.class);
			new DatapowerObjectMapperRequester<Void>().request(
					sqlForMonitor,
					null,
					connectionService,
					DPService,
					new DatapowerDataResponseHandler() {
						@Override
						public void handleDataResponse(DataResponse dataset) throws IOException {

							if (dataset.getResult() == Result.OK) {
								//если нет сообщений, ничего не делаем
								if (dataset.getDataset().getRows()==null)
									return;

								for (DatasetRow row: dataset.getDataset().getRows()) {
									Long outerId =Long.valueOf(row.getValues().get(0));
									String msg = row.getValues().get(1);
									String email = row.getValues().get(2);
									int status = Integer.parseInt(row.getValues().get(3));
									String date =row.getValues().get(4);
									String failString = row.getValues().get(5);
									int failCount = failString.equals("") ? 0 : Integer.parseInt(failString);


									String s;

									if (failCount>0) {
										s = "Push notification with id "+outerId+" and message '"+msg+"' can't be sent to user "+email;

										if (getLastCheckResult(SUB_CHECK_RESULT_CODE+outerId)==null) {
											//это уведомление обрабатывается 1 раз, добавляем описание проблемы в логи
											tagLogger.log(s);
										}

										result.add(new CheckResult(SUB_CHECK_RESULT_CODE+outerId, false, LOCAL_HOST_NAME+" says: "+s));
									} else {
										s = "Push notification with id "+outerId+", message '"+msg+"' and user "+email+" has status "+status+" since "+date;

										if (getLastCheckResult(SUB_CHECK_RESULT_CODE+outerId)==null) {
											//это уведомление обрабатывается 1 раз, добавляем описание проблемы в логи
											tagLogger.log(s);
										}

										result.add(new CheckResult(SUB_CHECK_RESULT_CODE+outerId, false, LOCAL_HOST_NAME+" says: "+s));
									}

								}
							}

						}
					});
		} catch (IOException e) {
			e.printStackTrace();
		}*/

		return result;
	}

	private boolean checkAppleServerAvailability() {
		try {
			KeyStore instance = KeyStore.getInstance(KeyStore.getDefaultType());
	        instance.load(null);
	        Push.alert("test",instance, "1234", true, new String[] {"1111111111000000000011111111110000000000111111111100000000001111"});
		} catch (Exception e) {
			logger.error("Can't connect to Apple server", e);
			tagLogger.log("Не удалось подключиться к Apple серверу. "+e);
			return false;
		}

		return true;
	}


	public String getConnectionService() {
		return connectionService;
	}


	public void setConnectionService(String connectionService) {
		this.connectionService = connectionService;
	}

	@Override
	public String getDescription() {
		return "Чекер для проверки доступности Apple серверов";
	}

}
