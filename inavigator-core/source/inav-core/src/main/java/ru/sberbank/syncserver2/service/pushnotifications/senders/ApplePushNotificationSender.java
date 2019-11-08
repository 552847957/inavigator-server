package ru.sberbank.syncserver2.service.pushnotifications.senders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import javapns.devices.Device;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.Payload;
import javapns.notification.PushNotificationBigPayload;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;

import org.springframework.beans.factory.annotation.Autowired;

import ru.sberbank.syncserver2.gui.db.dao.PushNotificationDao;
import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.pushnotifications.ClientsToNotificationMapper;
import ru.sberbank.syncserver2.service.pushnotifications.model.OperationSystemTypes;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotification;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotificationClient;
import ru.sberbank.syncserver2.util.FileHelper;

/**
 * Сендер для Apple
 * @author sbt-Shakhov-IN
 *
 */
public class ApplePushNotificationSender extends AbstractService implements BaseNotificationSender {
	/**
	 * Расширение файла для хранения сертфиката
	 */
	public static final String CERTIFICATE_LOCAL_EXTENSION = ".p12";
	
	public OperationSystemTypes getOSType() {
		return OperationSystemTypes.IOS;
	}

	
	private Map<String, CertificateInfo> certificates = new HashMap<String, CertificateInfo>();
	
    private PushNotificationDao pushNotificationDao;
    
    @Autowired
    public void setPushNotificationDao(PushNotificationDao pushNotificationDao) {
    	this.pushNotificationDao = pushNotificationDao;
    }
    
    
    /**
     * обрезать сообщения, если длина отправляемого сообщения > 256 байт (2 kb)
     */
    private boolean cutMessage = true;
    
    private int maxPayloadSize = 2048;
    
	/**
	 * Папка с настройками сертификатов 
	 */
	private String certificatesFolder;
    
	@Override
	protected void doStart() {
		super.doStart();
		initConfig();
	}
	
	/**
	 * Перечитать конфигурацию сертификатов
	 */
	private void initConfig() {
		if (certificates != null)
			certificates.clear();		
		
		File iosConfigFolder = new File(certificatesFolder);
		if (!iosConfigFolder.exists()) {
			tagLogger.log("Не найдена папка с сертификатами для IOS. Смотри "+certificatesFolder);
			return;
		}
		
		File[] applications = iosConfigFolder.listFiles();
		
		for(File application:applications) {
			// пропускаем все что не является папкой
			if (!application.isDirectory())
				continue;
			
			CertificateInfo certInfo = getPushSSLCertificateInfoFromFolder(application);
			if (certInfo != null) {
				certificates.put(application.getName().toUpperCase(),certInfo);
				tagLogger.log("Найден сертификат "+application);
			}
		}
	}		
			

	@Override
	public List<PushedNotificationInfo> sendNotification(List<PushNotification> allNotifications, List<List<PushNotificationClient>> allClients) {
		// 1. Выделить уведомления с одинаковым app_code
		ClientsToNotificationMapper<String> mapper = ClientsToNotificationMapper.parse(allNotifications, allClients);
		
		List<PushedNotificationInfo> result = new ArrayList<PushedNotificationInfo>();
		
		// 2. Для каждой группы с одинаковым appCode отправить всю группу уведомлений за 1 соединение
		for (String appName: mapper.getTypes()) {
			
			List<PushNotification> notifications = mapper.getNotificationsFor(appName);
			List<List<PushNotificationClient>> clientsList = mapper.getClientsFor(appName);
			
			CertificateInfo certInfo = certificates.get(appName);
			if (certInfo == null) {
				String msg = "Не загружен сертификат для "+appName;
				tagLogger.log(msg);
				addErrorToAllClients(result, notifications, clientsList, msg);
				continue;
			}
			
			List<List<Device>> devices = new ArrayList<List<Device>>();
			List<Payload> payloads = new ArrayList<Payload>();
			int maxSize = 0;
			
			// 2.1 Подготовка данных для отправки
			for (int i=0; i < notifications.size(); i++) {
				PushNotification notification = notifications.get(i);
				List<PushNotificationClient> clients = clientsList.get(i);
				try {
					Payload payload = new CustomPayload(notification);
					List<Device> deviceList = new ArrayList<Device>();
					for (PushNotificationClient client: clients) {						
						deviceList.add(new CustomDevice(client));
					}
					
					maxSize += deviceList.size();
					devices.add(deviceList);
					payloads.add(payload);
				} catch (Exception e) {
					logger.error(e, e);
					tagLogger.log("Ошибка при построении объекта сообщения с id="+notification.getNotificationId()+": "+e.toString());
					addErrorToAllClients(result, notification, clients, e.toString());
				}
			}
			
			// 2.2 Отправка
			PushedNotifications pushed = null;
			try {
				pushed = send(certInfo, devices, payloads, maxSize);
			} catch (Exception e) {
				logger.error(e, e);
				tagLogger.log("Ошибка при отправке уведомлений для приложений "+appName+": "+e.toString());
				addErrorToAllClients(result, notifications, clientsList, e.toString());
			}
			
			// 2.3 Запись результата
			if (pushed != null) {
				for (PushedNotification push: pushed) {
					CustomDevice device = (CustomDevice) push.getDevice();
					CustomPayload payload = (CustomPayload) push.getPayload();
					String error = push.getException() == null ? null : push.getException().getMessage();
					checkInvalidToken(push);
					result.add(new PushedNotificationInfo(payload.linkedNotification, device.linkedClient, error));
				}					
			}
		}
		return result;
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
	
	private PushedNotifications send(CertificateInfo cert, List<List<Device>> devices, List<Payload> payloads, int maxSize) throws CommunicationException, KeystoreException {
		PushedNotifications notifications = new PushedNotifications();
		PushNotificationManager pushManager = new PushNotificationManager();
		try {
			AppleNotificationServer server = new AppleNotificationServerBasicImpl(cert.certificateData, cert.certificatePassword, cert.productionMode);
			pushManager.initializeConnection(server);
			notifications.setMaxRetained(maxSize);
			for (int i=0; i<payloads.size(); i++) {
				List<Device> deviceList = devices.get(i);
				Payload payload = payloads.get(i);				
				for (Device device : deviceList) {
					PushedNotification notification = pushManager.sendNotification(device, payload, false);
					notifications.add(notification);
				}
			}
		} finally {
			try {
				pushManager.stopConnection();
			} catch (Exception e) {
			}
		}
		return notifications;
	}
	
	private void checkInvalidToken(PushedNotification notification) {
		if ((notification.getResponse()!=null && notification.getResponse().getStatus() == 8) || notification.getException() instanceof InvalidDeviceTokenFormatException) {
			// delete invalid token
			CustomDevice device = (CustomDevice) notification.getDevice();
			if (logger.isDebugEnabled()) {
				logger.debug("Удаление неверного push токена клиента "+device.linkedClient.getClientId());
			}
			try {
				pushNotificationDao.deleteInvalidToken(device.linkedClient.getClientId());
			} catch (Exception e) {
				logger.error(e, e);
				tagLogger.log("Не удалось удалить неверный токен из БД для пользователя с id="+device.linkedClient.getClientId()+": "+e.toString());
			}
		}
	}
	
	/**
	 * Прочитать сертфикат из папки с данными приложения
	 * @param applicationFolder
	 * @return
	 */
	private CertificateInfo getPushSSLCertificateInfoFromFolder(File applicationFolder) {
		if (!applicationFolder.isDirectory()) 
			return null;
		
		File certificates[] = applicationFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.toLowerCase().endsWith(CERTIFICATE_LOCAL_EXTENSION);
			}
		});		
		File passwords[] = applicationFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.equals("settings.properties");
			}
		});
		if (certificates.length < 1 || passwords.length < 1)
			return null;
		
		File passwordFile = passwords[0];
		File certificateFile =  certificates[0];
		BufferedReader br = null;
		FileInputStream fis = null;
		try {
			br = new BufferedReader(new FileReader(passwordFile));			
			Properties properties = new Properties();
			properties.load(br);
			
			String password = properties.getProperty("password");
			String productionMode = properties.getProperty("productionMode");

			fis = new FileInputStream(certificateFile);
			byte[] binaryData = new byte[(int)certificateFile.length()];
			fis.read(binaryData);
			return new CertificateInfo(binaryData,password,Boolean.parseBoolean(productionMode));
			
		} catch (Exception ex) {
			logger.error(ex, ex);
			tagLogger.log(ex.toString());
		} finally {
			FileHelper.close(fis);
			FileHelper.close(br);
		}
		
		return null;
	}
	
	private static class CertificateInfo {
		private byte[] certificateData;
		private String certificatePassword;
		private boolean productionMode;
		public CertificateInfo(byte[] certificateData,
				String certificatePassword, boolean productionMode) {
			super();
			this.certificateData = certificateData;
			this.certificatePassword = certificatePassword;
			this.productionMode = productionMode;
		}
	}
	
	private class CustomPayload extends PushNotificationBigPayload {
		private PushNotification linkedNotification;
		public CustomPayload(PushNotification notification) throws Exception {
			super();
			linkedNotification = notification;
			this.addAlert(notification.getMessageText());
			
			if (notification.getSoundName() != null && !notification.getSoundName().equals(""))
				this.addSound(notification.getSoundName());

			if (notification.getBadgeNumber() > 0)
				this.addBadge(notification.getBadgeNumber());
			
			this.addCustomDictionary("eventCode", notification.getEventType().toString());	
			
			for (String parameter: notification.getCustomParameters().split(";")) {
				String[] keyValue = parameter.split(":");
				if (keyValue.length==2) {
					this.addCustomDictionary(keyValue[0].trim(),keyValue[1].trim());
				}
			}	
			this.setCharacterEncoding("UTF-8");
			int maxPayloadSize = this.getMaximumPayloadSize() <= ApplePushNotificationSender.this.maxPayloadSize ? 
									this.getMaximumPayloadSize() : ApplePushNotificationSender.this.maxPayloadSize;
			
			if (cutMessage && this.getPayloadSize()> maxPayloadSize) {
				int d = this.getPayloadSize() - maxPayloadSize;
				byte[] text = notification.getMessageText().getBytes("UTF-8");
				
				String str1 = new String(text, 0, text.length - d, "UTF-8");
				String str2 = new String(text, 0, text.length - d -1, "UTF-8");
				if (str1.length() <= str2.length()) {
					this.addAlert(str1); // str2 обрывает массив байтов в середине символа
				} else {
					this.addAlert(str2); // str1 обрывает массив байтов в середине символа
				}
			}
		}
	}
	
	private static class CustomDevice extends BasicDevice {
		private PushNotificationClient linkedClient;
		public CustomDevice(PushNotificationClient linkedClient) {
			super();
			//this.setDeviceId(linkedClient.getToken());
			this.setToken(linkedClient.getToken());
			this.linkedClient = linkedClient;			
		}
	}

	@Override
	protected void doStop() {
	}

	@Override
	protected void waitUntilStopped() {
	}

	public String getCertificatesFolder() {
		return certificatesFolder;
	}

	public void setCertificatesFolder(String certificatesFolder) {
		this.certificatesFolder = certificatesFolder;
	}

	public int getMaxPayloadSize() {
		return maxPayloadSize;
	}

	public void setMaxPayloadSize(String maxPayloadSize) {
		this.maxPayloadSize = Integer.valueOf(maxPayloadSize);
	}

}
