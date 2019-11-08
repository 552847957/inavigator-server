package ru.sberbank.syncserver2.service.pushnotifications.senders;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import ru.sberbank.syncserver2.gui.db.dao.PushNotificationDao;
import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.pushnotifications.ClientsToNotificationMapper;
import ru.sberbank.syncserver2.service.pushnotifications.model.OperationSystemTypes;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushEventTypes;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotification;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotificationClient;
import ru.sberbank.syncserver2.util.FileHelper;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;

public class FirebasePushNotificationSender extends AbstractService implements BaseNotificationSender {

	private static final String DEFAULT_PUSH_URL = "https://fcm.googleapis.com/fcm/send";
	private static final String DEFAULT_CONTENT_TYPE = "application/json";
	private String pushURL = DEFAULT_PUSH_URL;
	private String contentType = DEFAULT_CONTENT_TYPE;

	private volatile Map<String, String> notificationsKeys = new HashMap<String, String>();

	private String configFolder = null;

	private boolean cutMessage = false;
	private int maxPayloadSize = 4096;

	//additional properties
	private String timeToLive = null;
	private String priority = null;


	@Override
	protected void doStart() {
		super.doStart();
		init();
	}
	
    private PushNotificationDao pushNotificationDao;
    
    @Autowired
    public void setPushNotificationDao(PushNotificationDao pushNotificationDao) {
    	this.pushNotificationDao = pushNotificationDao;
    }



	@Override
	public List<PushedNotificationInfo> sendNotification(List<PushNotification> allNotifications,
			List<List<PushNotificationClient>> allClients) {

		List<PushedNotificationInfo> result = new ArrayList<PushedNotificationInfo>();

		ClientsToNotificationMapper<String> appMapper = ClientsToNotificationMapper.parse(allNotifications, allClients);

		for (String appName: appMapper.getTypes()) {

			List<PushNotification> notifications = appMapper.getNotificationsFor(appName);
			List<List<PushNotificationClient>> clientsList = appMapper.getClientsFor(appName);


			if (notificationsKeys.get(appName) == null) {
				String msg = "Не загружен ключ приложения " + appName + " для отправки";
				tagLogger.log(msg);
				addErrorToAllClients(result, notifications, clientsList, msg);
				continue;
			}

			for (int i=0; i < notifications.size(); i++) {
				PushNotification notification = notifications.get(i);
				List<PushNotificationClient> clients = clientsList.get(i);
				try {
					JSONObject pushPayload = composePayload(notification, clients, cutMessage);

					String answer = sendPushToFCM(notificationsKeys.get(appName), pushPayload.toString());

					JSONObject response = new JSONObject(answer);

					if (response.has("multicast_id")) {
						// это токены
						JSONArray array = response.getJSONArray("results");

						for (int j=0; j<array.length(); j++) {
							JSONObject pushResult = array.getJSONObject(j);
							PushNotificationClient client = clients.get(j);
							if (pushResult.has("message_id")) {
								if (pushResult.has("registration_id")) {
									updateToken(client, pushResult.getString("registration_id"));
								}
								result.add(new PushedNotificationInfo(notification, client, null));
							} else {
								String er = pushResult.getString("error");
								result.add(new PushedNotificationInfo(notification, client, er));
								if ("InvalidRegistration".equals(er) || "NotRegistered".equals(er)) {
									deleteInvalidToken(client);
								}
							}
						}
					} else if (response.has("error")) {
						// это топик с ошибкой
						String er = response.getString("error");
						for (PushNotificationClient client: clients) {
							result.add(new PushedNotificationInfo(notification, client, "Ошибка при отправке по топику "+client.getDeviceName()+": "+er));
						}
					}
				} catch (Exception e) {
					logger.error(e, e);
					tagLogger.log("Ошибка при отправке уведомленя для приложений "+appName+": "+e.toString());
					addErrorToAllClients(result, notification, clients, e.toString());
				}
			}
		}

		return result;
	}

	private JSONObject composePayload(PushNotification notification, List<PushNotificationClient> clients, boolean cutMessage) throws JSONException, UnsupportedEncodingException {
    	JSONObject payload = new JSONObject();
		JSONObject notificationJson = new JSONObject();
		if (notification.getTitle() != null && !notification.getTitle().isEmpty())
			notificationJson.put("title", notification.getTitle());
		if (notification.getSoundName() != null && !notification.getSoundName().isEmpty())
			notificationJson.put("sound", notification.getSoundName());
		if (notification.getMessageText() != null)
			notificationJson.put("body", notification.getMessageText());
		notificationJson.put("badge", notification.getBadgeNumber());

		if (notification.getCustomParameters() != null) {
			for (String parameter: notification.getCustomParameters().split(";")) {
				String[] keyValue = parameter.split(":");
				if (keyValue.length==2) {
					notificationJson.put(keyValue[0].trim(),keyValue[1].trim());
				}
			}
		}

		if (priority != null)
			payload.put("priority", priority);
		if (timeToLive != null)
			payload.put("time_to_live", timeToLive);

		if (notification.getEventType() == PushEventTypes.MESSAGE)
			payload.put("notification", notificationJson);
		else
			payload.put("data", notificationJson);

    	if (clients.size() == 1) {
    		payload.put("to", clients.get(0).getToken());
		} else {
			List<String> reg_ids = new ArrayList<String>(clients.size());
			for (PushNotificationClient client: clients) {
				reg_ids.add(client.getToken());
			}
			payload.put("registration_ids", reg_ids);
		}


		if (cutMessage) {
    		int size = payload.toString().getBytes("UTF-8").length;
    		if (size > maxPayloadSize) {
				int d = size - maxPayloadSize;
				byte[] text = notification.getMessageText().getBytes("UTF-8");

				String str1 = new String(text, 0, text.length - d, "UTF-8");
				String str2 = new String(text, 0, text.length - d -1, "UTF-8");
				if (str1.length() <= str2.length()) {
					notification.setMessageText(str1); // str2 обрывает массив байтов в середине символа
				} else {
					notification.setMessageText(str2); // str1 обрывает массив байтов в середине символа
				}
			}
			return composePayload(notification, clients, false); // запускаем заново с измененым текстом

		}
    	return payload;
	}

	private String sendPushToFCM(String key, String pushPayload) throws IOException, ProtocolException {

		InputStream is = null;
		try {
			HttpsURLConnection connection = (HttpsURLConnection) new URL(pushURL).openConnection();
			connection.setRequestProperty("Authorization", key);
			connection.setRequestProperty("Content-Type", contentType);
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);

			OutputStream output = connection.getOutputStream();
			output.write(pushPayload.getBytes("UTF-8"));
			output.flush();
			output.close();

			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK ) {
				InputStream es = connection.getErrorStream();
				if (es == null) {
					throw new RuntimeException("Сервер вернул ошибку "+connection.getResponseCode());
				}
				try {
					StringWriter writer = new StringWriter();
					IOUtils.copy(es, writer, "UTF-8");
					throw new RuntimeException("Ошибка "+connection.getResponseCode()+": "+writer.toString());
				} finally {
					IOUtils.closeQuietly(es);
				}
			}

			is = connection.getInputStream();
			String responseString = new Scanner(is, "UTF-8").useDelimiter("\\A").next();
			return responseString;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	private void deleteInvalidToken(PushNotificationClient client) {
		try {
			pushNotificationDao.deleteInvalidToken(client.getClientId());
			logger.debug("Удален токен клиента " + client.getClientId());
		} catch (Exception e) {
			logger.error(e, e);
			tagLogger.log("Не удалось удалить неверный токен из БД для пользователя с id="+client.getClientId() +": "+e.toString());
		}
	}

	private void updateToken(PushNotificationClient client, String newToken) {
		try {
			pushNotificationDao.updateToken(client.getToken(), newToken);
			logger.debug("Обновлен токен клиента " + client.getClientId());
		} catch (Exception e) {
			logger.error(e, e);
			tagLogger.log("Не удалось обновить токен для пользователя с id="+client.getClientId() +": "+e.toString());
		}
	}


	private void addErrorToAllClients(List<PushedNotificationInfo> result,
									  List<PushNotification> notifications,
									  List<List<PushNotificationClient>> clientsList, String error) {
		for (int i=0; i < notifications.size(); i++) {
			addErrorToAllClients(result, notifications.get(i), clientsList.get(i), error);
		}
	}

	private void addErrorToAllClients(List<PushedNotificationInfo> result,
									  PushNotification notification,
									  List<PushNotificationClient> clients, String error) {
		for (PushNotificationClient client: clients) {
			result.add(new PushedNotificationInfo(notification, client, error));
		}
	}

	@Override
	public OperationSystemTypes getOSType() {
		return OperationSystemTypes.FIREBASE;
	}

	@Override
	protected void doStop() {
	}

	@Override
	protected void waitUntilStopped() {
	}

	public void init() {
		Map<String, String> local = new HashMap<String, String>();
		File configFolder = new File(this.configFolder);
		if (!configFolder.exists()) {
			tagLogger.log("Не найдена папка конфигурации для ANDROID");
			return;
		}
		File[] settingsAppsFolders = configFolder.listFiles();

		for (File appFolder : settingsAppsFolders) {

			if (!appFolder.isDirectory())
				continue;

			File file = new File(appFolder, "settings.properties");
			if (!file.exists()) {
				tagLogger.log("Не найдены настройки (файл settings.properties) для ANDROID для приложения "
						+ appFolder.getName());
				continue;
			}

			BufferedReader br = null;
			try {
				br = new BufferedReader(new FileReader(file));
				Properties properties = new Properties();
				properties.load(br);
				String key = properties.getProperty("key");
				//String pushUrl = properties.getProperty("serverUrl");
				if (key != null) {
					local.put(appFolder.getName(), key);
					tagLogger.log("Найден файл настроек для ANDROID для приложения " + appFolder.getName());
				} else
					tagLogger.log("Не найден ключ для отправки пушей на ANDROID устройства для приложения " + appFolder.getName());
			} catch (Exception e) {
				logger.error(e, e);
				tagLogger.log("При чтении настроек для ANDROID для приложения " + appFolder.getName() +" возникла ошибка " + e.getMessage());
			} finally {
				FileHelper.close(br);
			}
		}
		notificationsKeys = local;
	}

	public String getConfigFolder() {
		return configFolder;
	}

	public void setConfigFolder(String configFolder) {
		this.configFolder = configFolder;
	}

	public String getPushURL() {
		return pushURL;
	}

	public void setPushURL(String pushURL) {
		this.pushURL = pushURL;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getPriority() {
		return priority;
	}

	public void setPriority(String priority) {
		this.priority = priority;
	}

	public int getMaxPayloadSize() {
		return maxPayloadSize;
	}

	public void setMaxPayloadSize(String maxPayloadSize) {
		this.maxPayloadSize = Integer.valueOf(maxPayloadSize);
	}

	public boolean isCutMessage() {
		return cutMessage;
	}

	public void setCutMessage(String cutMessage) {
		this.cutMessage = Boolean.valueOf(cutMessage);
	}
}
