package ru.sberbank.syncserver2.service.pushnotifications.model;

import java.util.List;

import ru.sberbank.syncserver2.service.pushnotifications.senders.BaseNotificationSender.PushedNotificationInfo;

public interface PushNotificationListener {
	/**
	 * Уведомление было отправлено. Уведомление может отправляться разным группам пользователей, 
	 * поэтому этот метод может вызываться несколько раз подряд для одних и тех же notification_id 
	 * @param notifications
	 */
	void pushNotificationsWasSent(PushResult notifications);
	
	public static class PushResult {
		private final List<PushedNotificationInfo> pushed;
		private final boolean failed;
		private final List<PushNotification> notifications;
		private final List<List<PushNotificationClient>> clients;
		private final String error;
		public PushResult(List<PushNotification> notifications, List<List<PushNotificationClient>> clients, String error) {
			this.pushed = null;
			this.failed = true;
			this.notifications = notifications;
			this.clients = clients;
			this.error = error;
		}
		
		public PushResult(List<PushedNotificationInfo> pushed) {
			this.pushed = pushed;
			this.failed = false;
			this.notifications = null;
			this.clients = null;
			this.error = null;
		}
		public List<PushedNotificationInfo> getPushed() {
			return pushed;
		}
		public boolean isFailed() {
			return failed;
		}
		public List<PushNotification> getNotifications() {
			return notifications;
		}	
		public String getError() {
			return error;
		}
		public List<List<PushNotificationClient>> getClients() {
			return clients;
		}
	}
}
