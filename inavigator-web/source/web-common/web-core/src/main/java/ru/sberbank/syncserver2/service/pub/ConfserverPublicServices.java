package ru.sberbank.syncserver2.service.pub;

import org.springframework.beans.factory.annotation.Autowired;
import ru.sberbank.syncserver2.gui.db.dao.MobileLogDao;
import ru.sberbank.syncserver2.gui.util.format.JSPFormatPool;
import ru.sberbank.syncserver2.service.core.XmlPublicService;
import ru.sberbank.syncserver2.service.pub.xml.*;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;
import ru.sberbank.syncserver2.service.sql.query.*;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Набор методов для поддержки клиентского логирования
 * @author sbt-gordienko-mv
 *
 */
public class ConfserverPublicServices extends XmlPublicService {

	@Autowired
	private MobileLogDao mobileLogDao;

	private static Class[] XMLClasses = {
		GetApplicationsRequest.class,
		GetVersionsRequest.class,
		DataResponse.class,
		GetLoggingModeRequest.class,
		SetLoggingModeRequest.class,
		EventInfo.class,DeviceInfo.class,SendUserLogMessageRequest.class,
		GetUserLogMessagesRequest.class,GetUserLogMessagesResponse.class

	};



	@Override
	protected Class[] getSupportedXmlClasses() {
		// TODO Auto-generated method stub
		return XMLClasses;
	}


	/** Получить список приложений зарегистрированных в конф сервере
	 * (Метод необходим для утилиты отлаки работы девайса)
	 * @param request - запрос
	 * @return
	 */
	private DataResponse getApplications(GetApplicationsRequest request) {
		DataResponse dr = new DataResponse();

		dr.setMetadata(new DatasetMetaData());
		dr.getMetadata().addField(new DatasetFieldMetaData("applicationBundle",FieldType.STRING));
		dr.setDataset(new Dataset());

		List<String> applications = getDatabase().listClientApplications();
		for(String applicationBundle:applications) {
			DatasetRow row = new DatasetRow();
			row.addValue(applicationBundle);
			dr.getDataset().addRow(row);
		}

		dr.setError("");
		dr.setResult(Result.OK);

		return dr;
	}


	/** Получить список версий по идентиифкатору приложения
	 * (Метод необходим для утилиты отлаки работы девайса)
	 * @param request - запрос
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private DataResponse getVersions(GetVersionsRequest request) {
		DataResponse dr = new DataResponse();

		dr.setMetadata(new DatasetMetaData());
		dr.getMetadata().addField(new DatasetFieldMetaData("versionCode",FieldType.STRING));
		dr.setDataset(new Dataset());

		List<String> applicationVersions = getDatabase().listClientAppVersions(request.getApplicationBundle());
		for(String applicationVersion: applicationVersions) {
			DatasetRow row = new DatasetRow();
			row.addValue(applicationVersion);
			dr.getDataset().addRow(row);
		}

		dr.setError("");
		dr.setResult(Result.OK);

		return dr;
	}


	/**
	 * Получить режим логирования для конкретного клиента
	 * @param request
	 * @param userEmail
	 * @return
	 */
	public DataResponse getLoggingModeRequest(GetLoggingModeRequest request, String userEmail) {
		DataResponse dr = new DataResponse();
		dr.setError("");
		dr.setResult(Result.OK);
		// Алгоритм отправки сообщения на сервер google
		String sql = "SELECT MODE FROM SYNC_MOBILE_MODE WHERE lower(USER_EMAIL)=lower('"+userEmail+"')";
		String result = getDatabase().getStringValue(sql);
		if (result=="") result="0";
		// добавляем столбец с реузльтатом
		dr.setMetadata(new DatasetMetaData());
		dr.getMetadata().addField(new DatasetFieldMetaData("loggingMode",FieldType.NUMBER));
		dr.setDataset(new Dataset());
		DatasetRow row = new DatasetRow();
		row.addValue(result);
		dr.getDataset().addRow(row);

		return dr;
	}

	/**
	 * Установить режим логирования для клиента ( Временная: Используется в отладочных целях)
	 * @param request
	 * @return
	 */
	public DataResponse setLoggingModeRequest(SetLoggingModeRequest request) {
		DataResponse dr = new DataResponse();
		dr.setError("");
		dr.setResult(Result.OK);
		String sql = "exec SP_SYNC_MOBILE_SET_MODE ?,?,?";
		getDatabase().executePatternUnicode(sql, new Object[] {request.getUserEmail(), null, request.isMode()?1:0}, null);
		return dr;

	}

	/**
	 * Отправить данные лога. Метод вызывается с клиентского утройства
	 * @param request
	 * @return
	 */
	public DataResponse sendUserLogMessageRequest(SendUserLogMessageRequest  request) {
		DataResponse dr = new DataResponse();
		dr.setError("");
		dr.setResult(Result.OK);

		// Простая валидация входных параметров
		if (
				(request.getUserEmail() == null || "".equals(request.getUserEmail())) ||
				(request.getLogEvent() == null)
			) {
			dr.setError("Input Params Error");
			dr.setResult(Result.FAIL);

			return dr;
		}
		List<Object> par = new ArrayList<Object>();
		EventInfo ei = request.getLogEvent().getEventInfo();
		DeviceInfo di = request.getLogEvent().getDeviceInfo();
		if (di!=null) {
			par.add(di.getDeviceInfoSourceId());
			par.add(di.getDeviceModel());
			par.add(di.getiOsVersion());
			par.add(di.getAppVersion());
			par.add(di.getBundleId());
			String formatedDate="";
			try {
				long timestamp = Long.parseLong(di.getUpdateTime());
				Date date = new Date(timestamp*1000);
				formatedDate = JSPFormatPool.formatDateAndTime(date);
			} catch (NumberFormatException e) {	}
			par.add(formatedDate);

		} else {
			par.add(null);
			par.add(null);
			par.add(null);
			par.add(null);
			par.add(null);
			par.add(null);
		}
		if (ei!=null) {
			par.add(ei.getEventSourceId());
			Date date = null;
			try {
				long timestamp = Long.parseLong(ei.getEventTime());
				date = new Date(timestamp*1000);
			} catch (NumberFormatException e) {	}
			par.add(date);
			par.add(ei.getTimeZone());
			par.add(request.getUserEmail());
			par.add(ei.getEventType());
			par.add(ei.getEventDesc());
			par.add(ei.getIpAddress());
			par.add(ei.getDataServer());
			par.add(ei.getDistribServer());
			par.add(ei.getEventInfo());
			par.add(ei.getErrorStackTrace());
			par.add(ei.getConfigurationServer());
		} else {
			par.add(null);			par.add(null);
			par.add(null);			par.add(request.getUserEmail());
			par.add(null);			par.add(null);
			par.add(null);			par.add(null);
			par.add(null);			par.add(null);
			par.add(null);			par.add(null);
		}
		String exec = "exec SP_SYNC_MOBILE_LOG ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";
		getDatabase().executePatternUnicode(exec, par.toArray(), null);

		return dr;
	}

	/**
	 * Получить список всех логов  для конкретного клиента ( Временный: используется для отладочных целей)
	 * @param request
	 * @return
	 */
	public GetUserLogMessagesResponse getUserLogMessagesRequest(GetUserLogMessagesRequest request) {
		GetUserLogMessagesResponse response = new GetUserLogMessagesResponse();
		response.setStatus(new ResponseStatus());
		response.getStatus().setErrorCode("0");

		String where = " WHERE lower(USER_EMAIL)=lower('"+request.getUserEmail()+"')";
		List<LogEvent> events = mobileLogDao.listLogEvents(where);
		response.setLogEvent(events);

		return response;

	}

	@Override
	/**
	 * Обработка запроса
	 */
	public Object xmlRequest(HttpServletRequest request,
			HttpServletResponse response, Object xmlRequest) {

		// Методы для поддержки утилиты отладки
		if (xmlRequest instanceof GetApplicationsRequest)
			return getApplications((GetApplicationsRequest)xmlRequest);
		else if (xmlRequest instanceof GetVersionsRequest)
			return getVersions((GetVersionsRequest)xmlRequest);

		// Методы для поддержки механизма клиентского логирования
		else if (xmlRequest instanceof GetLoggingModeRequest)
			return getLoggingModeRequest((GetLoggingModeRequest)xmlRequest, HttpRequestUtils.getUsernameFromRequest(request));
		else if (xmlRequest instanceof SetLoggingModeRequest)
			return setLoggingModeRequest((SetLoggingModeRequest)xmlRequest);
		else if (xmlRequest instanceof SendUserLogMessageRequest) {
			SendUserLogMessageRequest message = (SendUserLogMessageRequest) xmlRequest;
			message.setUserEmail(HttpRequestUtils.getUsernameFromRequest(request));
			return sendUserLogMessageRequest(message);
		} else if (xmlRequest instanceof GetUserLogMessagesRequest)
			return getUserLogMessagesRequest((GetUserLogMessagesRequest)xmlRequest);

		return null;
	}





}
