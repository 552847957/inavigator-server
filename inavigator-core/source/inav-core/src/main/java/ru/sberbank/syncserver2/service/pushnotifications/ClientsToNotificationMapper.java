package ru.sberbank.syncserver2.service.pushnotifications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotification;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotificationClient;

public class ClientsToNotificationMapper<T> {
	private Map<T, Bounds> map = new HashMap<T, ClientsToNotificationMapper.Bounds>();
	
	public void add(T tag, PushNotification notification, List<PushNotificationClient> clients) {
		getBounds(tag).add(notification, clients);
	}
	
	private Bounds getBounds(T tag) {
		Bounds bounds = map.get(tag);
		if (bounds == null) {
			bounds = new Bounds();
			map.put(tag, bounds);
		}
		return bounds;
	}
	
	public Set<T> getTypes() {
		return map.keySet();
	}
	
	public List<PushNotification> getNotificationsFor(T tag) {
		return getBounds(tag).notifications;
	}
	
	public List<List<PushNotificationClient>> getClientsFor(T tag) {
		return getBounds(tag).clients;
	}
	
	private static class Bounds {
		List<PushNotification> notifications = new ArrayList<PushNotification>();
		List<List<PushNotificationClient>> clients = new ArrayList<List<PushNotificationClient>>();
		
		public void add(PushNotification notification, List<PushNotificationClient> clients) {
			notifications.add(notification);
			this.clients.add(clients);
		}
	}

	public static ClientsToNotificationMapper<String> parse(List<PushNotification> allNotifications, List<List<PushNotificationClient>> allClients) {
		ClientsToNotificationMapper<String> mapper = new ClientsToNotificationMapper<String>();

		for (int i=0; i< allNotifications.size(); i++) {
			PushNotification notification = allNotifications.get(i);
			List<PushNotificationClient> clients = allClients.get(i);

			//делим на группы по app_code
			int index = 0; 								//индекс клиента, начиная с которого будет начинаться следующая группа
			String appCode = null;				//		для заданных ОС

			if (clients.size()>0) {
				appCode = clients.get(0).getApplicationCode().toUpperCase();
			}
			// в цикле выделяем часть клиентов с одинаковыми APP_CODE
			for (int j=0; j<clients.size(); j++) {

				if (!clients.get(j).getApplicationCode().toUpperCase().equals(appCode)) {
					//добавляем клиентов от index до j для этого уведомления
					mapper.add(appCode, notification, clients.subList(index, j));

					//обновляем параметры для следующей группы клиентов
					index = j;
					appCode = clients.get(j).getApplicationCode().toUpperCase();
				}
			}

			//добавляем оставшихся клиентов (последнюю группу) для этого уведомления
			mapper.add(appCode, notification, clients.subList(index, clients.size()));
		}

		return mapper;
	}

}
