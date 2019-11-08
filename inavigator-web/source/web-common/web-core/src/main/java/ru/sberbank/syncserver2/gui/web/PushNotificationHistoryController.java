package ru.sberbank.syncserver2.gui.web;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.datamappers.DatapowerDataResponseHandler;
import ru.sberbank.syncserver2.service.datamappers.DatapowerObjectMapperRequester;
import ru.sberbank.syncserver2.service.datamappers.DatapowerResponseMapper;
import ru.sberbank.syncserver2.service.datamappers.DatapowerResultObjectListHandler;
import ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger;
import ru.sberbank.syncserver2.service.monitor.check.PushNotificationChecker;
import ru.sberbank.syncserver2.service.pushnotifications.DataPowerPushnotificationsUploader;
import ru.sberbank.syncserver2.service.sql.DataPowerService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.DatasetRow;
import ru.sberbank.syncserver2.service.sql.query.FieldType;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest.Arguments.Argument;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class PushNotificationHistoryController extends ShowTableController {
	private volatile DataPowerService dpService;
	private volatile String alphaConectionService = null;
	private final String sqlHistory = "INAVIGATOR20.SELECT_NOTIFICATION_HISTORY";
	private final String sqlMessages = "INAVIGATOR20.GET_NOTIFICATIONS_FOR_MONITOR";
	private final String sqlUpdate = "INAVIGATOR20.UPDATE_NOTIFICATION_SEND_STATUS";
	private DataPowerPushnotificationsUploader dppu; // this service will provide alphaConectionService
	private PushNotificationChecker pnch;			// this service will provide alphaConectionService

	public PushNotificationHistoryController() {
		super(PushNotificationHistoryController.class);
		numberOfColumns = 2;
	}

	@Override
	protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {

		String servletPath = HttpRequestUtils.getFulRequestPath(request);
		if (servletPath.contains("table.dppush.gui")) {
			transmit(request, response);
			return null;
		}

		if (servletPath.contains("cancel.dppush.gui")) {
			String notificationId = request.getParameter("NID");
			if (notificationId!=null && !notificationId.equals("")) {
				try {
					long id = Long.valueOf(notificationId);
					cancel(id, request.getRemoteAddr());
					PrintWriter w =response.getWriter();
					w.print("OK");
					w.flush();
					w.close();
				} catch (NumberFormatException e) {}
			}
			return null;
		}

		if (dpService == null)
			dpService = (DataPowerService) ServiceManager.getInstance().findFirstServiceByClassCode(DataPowerService.class);

		if (dppu == null) {
			dppu = ((DataPowerPushnotificationsUploader) ServiceManager.getInstance().findFirstServiceByClassCode(DataPowerPushnotificationsUploader.class));

			if (dppu == null)
				pnch = ((PushNotificationChecker) ServiceManager.getInstance().findFirstServiceByClassCode(PushNotificationChecker.class));
		}

		alphaConectionService = getAlphaConectionService();

		return new ModelAndView("DPPushMsgHistory");
	}

	@Override
	protected SearchResult search(HttpServletRequest request, int orderCol,
			int direction, List<String> searchValues, int startIndex,
			int numberOfMessages) {
		String notificationId = request.getParameter("NID");
		if (notificationId!=null && !notificationId.equals("")) {
			try {
				//считаем что notificationId задан, поэтому возвращаемисторию уведомления
				long id = Long.valueOf(notificationId);
				return getHistory(id);
			} catch (NumberFormatException e) {}
		}
		//считаем что notificationId не задан, возвращаем список уведомлений
		return getNotifications();
	}

	private String getAlphaConectionService() {
		if (dppu != null)
			return dppu.getConnectionService();

		if (pnch != null)
			return pnch.getConnectionService();

		return ((DataPowerNotificationLogger) ServiceManager.getInstance().findFirstServiceByClassCode(DataPowerNotificationLogger.class)).getService();
	}

	private SearchResult getNotifications() {
		final List<String[]> notifiacations = new LinkedList<String[]>();

		try {
			new DatapowerObjectMapperRequester<String[]>().request(sqlMessages, null, alphaConectionService, dpService, new DatapowerResponseMapper<String[]>() {

				@Override
				public String[] convertResultToObject(DatasetRow row) {
					return new String[] {row.getValues().get(0),row.getValues().get(1), row.getValues().get(4)};
				}
			}, new DatapowerResultObjectListHandler<String[]>() {

				@Override
				public void handleResultObjectList(List<String[]> results)
						throws IOException {
					if (results!=null)
						notifiacations.addAll(results);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new SearchResult(notifiacations, notifiacations.size(), notifiacations.size());
	}

	private SearchResult getHistory(long notificationId) {
		List<Argument> arguments = new ArrayList<OnlineRequest.Arguments.Argument>();
		arguments.add(new Argument(1, FieldType.NUMBER, String.valueOf(notificationId)));

		final List<String[]> msgs = new LinkedList<String[]>();
		try {
			new DatapowerObjectMapperRequester<String[]>().request(sqlHistory, arguments, alphaConectionService, dpService, new DatapowerResponseMapper<String[]>() {

				@Override
				public String[] convertResultToObject(DatasetRow row) {
					return new String[] {row.getValues().get(2),row.getValues().get(1),row.getValues().get(0)};
				}
			}, new DatapowerResultObjectListHandler<String[]>() {

				@Override
				public void handleResultObjectList(List<String[]> results)
						throws IOException {
					if (results!=null)
						msgs.addAll(results);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new SearchResult(msgs, msgs.size(), msgs.size());
	}

	private void cancel(long notificationId, String ip) {
		List<Argument> arguments = new ArrayList<OnlineRequest.Arguments.Argument>();
		arguments.add(new Argument(1, FieldType.NUMBER, String.valueOf(notificationId)));
		arguments.add(new Argument(2, FieldType.NUMBER, "5"));
		arguments.add(new Argument(3, FieldType.STRING, ip));
		try {
			new DatapowerObjectMapperRequester<String[]>().request(sqlUpdate, arguments, alphaConectionService, dpService, new DatapowerDataResponseHandler() {
				@Override
				public void handleDataResponse(DataResponse dataset) throws IOException {

				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	@Override
	protected void generateFile(HttpServletRequest request,
			HttpServletResponse response, SearchResult searchResult) {
	}

}
