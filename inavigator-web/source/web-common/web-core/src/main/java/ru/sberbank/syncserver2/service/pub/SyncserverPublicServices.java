package ru.sberbank.syncserver2.service.pub;

import org.springframework.beans.factory.annotation.Autowired;
import ru.sberbank.syncserver2.gui.db.dao.PushNotificationDao;
import ru.sberbank.syncserver2.service.core.XmlPublicService;
import ru.sberbank.syncserver2.service.pub.xml.*;
import ru.sberbank.syncserver2.service.pushnotifications.model.OperationSystemTypes;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotificationClient;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Сервис привязки
 * @author sbt-gordienko-mv
 *
 */
public class SyncserverPublicServices extends XmlPublicService {

	private PushNotificationDao pushNotificationDao;

	@Autowired
	public void setPushNotificationDao(PushNotificationDao pushNotificationDao) {
		this.pushNotificationDao = pushNotificationDao;
	}

	private static Class[] XMLClasses = {
		GetApplicationsRequest.class,
		GetVersionsRequest.class,
		RegisterClientTokenRequest.class,SendPushNotificationRequest.class,
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


	/**
	 *  Зарегистрировать apple-токен для клиента
	 * @param request
	 * @return
	 */
	private DataResponse registerClientToken(RegisterClientTokenRequest request) {
		DataResponse dr = new DataResponse();

		if (
				(request.getUserApplicationCode() == null || request.getUserApplicationCode().equals("")) ||
				(request.getUserApplicationVersion() == null || request.getUserApplicationVersion().equals("")) ||
				(request.getUserEmail() == null || request.getUserEmail().trim().isEmpty()) ||
				(request.getUserDeviceCode() == null || request.getUserDeviceCode().equals("")) ||
				(request.getUserDeviceName() == null || request.getUserDeviceName().equals("")) ||
				(request.getUserToken() == null || request.getUserToken().equals(""))
			) {
			dr.setError("Error input parameters");
			dr.setResult(Result.FAIL);

			return dr;
		}

		PushNotificationClient client = new PushNotificationClient();
		OperationSystemTypes type = request.getOsCode();
		client.setOsCode(type==null?OperationSystemTypes.IOS:type);
		client.setDeviceName(request.getUserDeviceName());
		client.setDeviceCode(request.getUserDeviceCode());
		client.setApplicationCode(request.getUserApplicationCode());
		client.setApplicationVersion(request.getUserApplicationVersion());
		client.setEmail(request.getUserEmail().trim());
		client.setToken(request.getUserToken());

		try {
			pushNotificationDao.registerNotificationClient(client);
		} catch (Exception e) {
			tagLogger.log("Не удалось зарегистрировать токен от пользователя "+request.getUserEmail()+" с устройства "+request.getUserDeviceName()+
					"("+request.getUserDeviceCode()+"), полученный токен - "+request.getUserToken()+". "+e.getMessage());
			logger.error("Registration failed for "+request.getUserEmail()+" with device "+request.getUserDeviceName()+"("+request.getUserDeviceCode()+")", e);
			dr.setError("Error on server side");
			dr.setResult(Result.FAIL);
			return dr;
		}

		dr.setError("");
		dr.setResult(Result.OK);

		return dr;
	}

	@Override
	/**
	 * Обработка запроса
	 */
	public Object xmlRequest(HttpServletRequest request,
			HttpServletResponse response, Object xmlRequest) {

		if (xmlRequest instanceof RegisterClientTokenRequest)
			return registerClientToken((RegisterClientTokenRequest)xmlRequest);
		else if (xmlRequest instanceof SendPushNotificationRequest)
			return sendNotification((SendPushNotificationRequest)xmlRequest);

		return null;
	}


	private DataResponse sendNotification(SendPushNotificationRequest notification) {
		DataResponse dr = new DataResponse();
		/*try {
			int n = pushNotificationDao.addPushNotificationToQueue(notification.getMessage(), null, null, null, null, null);
			tagLogger.log(n+" messages were added");
		} catch (Exception e) {
			dr.setError(e.getMessage());
			dr.setResult(Result.FAIL);
		}		*/

		dr.setError("");
		dr.setResult(Result.OK);
		return dr;
	}




}
