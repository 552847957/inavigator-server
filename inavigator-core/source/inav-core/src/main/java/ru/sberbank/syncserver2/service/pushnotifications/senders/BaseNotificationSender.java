package ru.sberbank.syncserver2.service.pushnotifications.senders;

import java.util.List;

import ru.sberbank.syncserver2.service.pushnotifications.model.OperationSystemTypes;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotification;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotificationClient;

/**
 * Базовый класс для отправки уведолмений
 * @author sbt-gordienko-mv
 *
 */
public interface BaseNotificationSender {
	
	/**
	 * Метод отправки уведомлений
	 */
	List<PushedNotificationInfo> sendNotification(List<PushNotification> notifications, List<List<PushNotificationClient>> clients);
	
	/**
	 * получить ОС для которой поддерживается отправка
	 * @return
	 */
	OperationSystemTypes getOSType();
		
	
	/**
	 * Класс, содержащий информацию об отправленных уведомлениях
	 */
	public static class PushedNotificationInfo {
		private final PushNotification notification;
		private final PushNotificationClient client;
		private final String error;
		
		public PushedNotificationInfo(PushNotification notification, PushNotificationClient client, String error) {
			this.notification = notification;
			this.client = client;
			this.error = error;
		}

		public boolean isFailed() {
			return error != null;
		}
		public PushNotification getNotification() {
			return notification;
		}
		public PushNotificationClient getClient() {
			return client;
		}
		public String getError() {
			return error;
		}
		@Override
		public String toString() {
			String msg = " notification "+notification.getNotificationId()+" with source id="+notification.getSourceId();
			return isFailed() ? ("Failed"+msg+": "+error) : ("Success"+msg);
		}
	}
}
