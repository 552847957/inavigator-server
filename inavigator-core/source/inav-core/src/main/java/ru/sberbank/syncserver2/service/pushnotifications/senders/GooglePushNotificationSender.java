package ru.sberbank.syncserver2.service.pushnotifications.senders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;

import ru.sberbank.syncserver2.gui.db.dao.PushNotificationDao;
import ru.sberbank.syncserver2.service.pushnotifications.model.OperationSystemTypes;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotification;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotificationClient;
import ru.sberbank.syncserver2.service.pushnotifications.senders.BaseNotificationSender.PushedNotificationInfo;
import ru.sberbank.syncserver2.util.FileHelper;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

/**
 * Сендер для Apple
 * @author sbt-Shakhov-IN
 *
 */
public class GooglePushNotificationSender implements BaseNotificationSender {
	public static final int MAX_RETRIES = 10;

    private int maxRetries = MAX_RETRIES;

    private Sender sender;    
    
    private PushNotificationDao pushNotificationDao;
    
    private String configFolder = null; 
    
    @Autowired
    public void setPushNotificationDao(PushNotificationDao pushNotificationDao) {
    	this.pushNotificationDao = pushNotificationDao;
    }
	
	public List<String> init() {
		List<String> msgs = new ArrayList<String>();
		File configFolder = new File(this.configFolder);
		if (!configFolder.exists()) {
			msgs.add("Не найдена папка конфигурации для ANDROID");
			return msgs;
		}
		
		File file = new File(configFolder, "settings.properties");
		if (!file.exists()) {
			msgs.add("Не найдены настройки (файл settings.properties) для ANDROID");
			return msgs;
		}
		msgs.add("Найден файл настроек для ANDROID");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			Properties properties = new Properties();
			properties.load(br);
			String key = properties.getProperty("key");
			if (key != null) {
				sender = new Sender(key);
				String s = properties.getProperty("maxRetries"); 
				if (s!=null)
					maxRetries = Integer.parseInt(s);
				else
					maxRetries = MAX_RETRIES;
			} else
				msgs.add("Не найден ключ для отправки пушей на ANDROID устройства");
			
		} catch (NumberFormatException nfe) {
			maxRetries = MAX_RETRIES;
		} catch (IOException e) {
			e.printStackTrace();
			msgs.add("При чтении настроек для ANDROID возникла ошибка "+e.getMessage());
		} finally {
			FileHelper.close(br);
		}
		
		return msgs;
	}

	@Override
	public List<PushedNotificationInfo> sendNotification(List<PushNotification> notifications, List<List<PushNotificationClient>> allClients) {
		List<PushedNotificationInfo> result = new ArrayList<BaseNotificationSender.PushedNotificationInfo>(notifications.size());
		for (int i=0; i < notifications.size(); i++) {
			PushNotification notification = notifications.get(i);
			List<PushNotificationClient> clients = allClients.get(i);

	        // формируем объект PUSH уведомления
	        Message.Builder builder = new Message.Builder();
	        
	        builder.addData("text", notification.getMessageText());
	        builder.addData("badgeNumber", String.valueOf(notification.getBadgeNumber()));
	        builder.addData("soundName", notification.getSoundName());
	        builder.addData("eventCode", notification.getEventType().toString());
	        String[] customParameters = notification.getCustomParameters().split(";");
			for (String parameter: customParameters) {
				String[] keyValue = parameter.split(":");
				if (keyValue.length==2) {
					builder.addData(keyValue[0].trim(),keyValue[1].trim());
				}
			}
	        Message message = builder.build();        
	        /*
	        try {
	        	//store the map token -> client id
	        	Map<String, Long> idByToken = new HashMap<String, Long>(clients.size());
				for (PushNotificationClient client: clients) {
					idByToken.put(client.getToken(), client.getClientId());
				}
				
				List<String> devices = new ArrayList<String>(idByToken.keySet());
				
	            List<Result> results = sender.send(message, devices, maxRetries).getResults();
	            
	            List<BaseResult> failed = new ArrayList<BaseResult>();
	            List<BaseResult> successful = new ArrayList<BaseResult>();
	            
	            for (int j=0; j<devices.size();j++) {
	            	Result r = results.get(j);
	            	
	            	updateToken(devices.get(j),r.getCanonicalRegistrationId());
	            	if (r.getMessageId()!=null) {
	            		successful.add(new BaseResult(idByToken.get(devices.get(j)), "OK"));
	            	} else {
	            		failed.add(new BaseResult(idByToken.get(devices.get(j)), r.getErrorCodeName()));
	            	}
	            }
	            
	            result.add(new PushedNotificationsInfo(notification.getNotificationId(), failed, successful));
	            
	        } catch (IOException e) {
	        	e.printStackTrace();
	            result.add(new PushedNotificationsInfo(notification.getNotificationId(), e.getMessage()));
	        }*/
		}
		return result;
				
	}
	
	private void updateToken(String oldToken, String canonicalRegistrationId) {
		if (canonicalRegistrationId != null) {
			pushNotificationDao.updateToken(oldToken, canonicalRegistrationId);
		}		
	}

	@Override
	public OperationSystemTypes getOSType() {
		return OperationSystemTypes.ANDROID;
	}

}
