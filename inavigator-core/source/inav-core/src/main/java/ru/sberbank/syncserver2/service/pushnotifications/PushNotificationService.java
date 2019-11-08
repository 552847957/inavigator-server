package ru.sberbank.syncserver2.service.pushnotifications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import ru.sberbank.syncserver2.gui.db.dao.PushNotificationDao;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.generator.ClusterManager;
import ru.sberbank.syncserver2.service.pushnotifications.model.OperationSystemTypes;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotification;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotificationClient;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotificationListener;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotificationListener.PushResult;
import ru.sberbank.syncserver2.service.pushnotifications.senders.BaseNotificationSender;
import ru.sberbank.syncserver2.service.pushnotifications.senders.BaseNotificationSender.PushedNotificationInfo;
import ru.sberbank.syncserver2.util.ExecutionTimeProfiler;

public class PushNotificationService extends SingleThreadBackgroundService {
	/**
	 * максимальное количество уведомлений, получаемое из БД и отправляемое в Apple за раз
	 */
	private Integer amount = 0;
		
	/**
	 * Сендеры для различных систем
	 */
	private Map<OperationSystemTypes,BaseNotificationSender> sendersRegistry = new HashMap<OperationSystemTypes, BaseNotificationSender>();
	
	private List<PushNotificationListener> listeners = new ArrayList<PushNotificationListener>();
	
    private PushNotificationDao pushNotificationDao;
    
    @Autowired
    public void setPushNotificationDao(PushNotificationDao pushNotificationDao) {
    	this.pushNotificationDao = pushNotificationDao;
    }
	
	ClusterManager clusterManager;
	
	/**
	 * Метод добавления слушателя отправленых уведомлений
	 */
	public void addListener(PushNotificationListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}
	
	public PushNotificationService() {
		super(15);
	}
	
	private void tagLog(String message) {
		tagLogger.log(message);
	}
	
	public void addSender(BaseNotificationSender sender) {
		sendersRegistry.put(sender.getOSType(), sender);
	}
	
	
	@Override
	public void doInit() {				
		clusterManager = (ClusterManager) ServiceManager.getInstance().findFirstServiceByClassCode(ClusterManager.class);
		if (pushNotificationDao.getPushDataSource() != null)
			tagLog("Используется отдельный источник данных для пользователей и сообщений");
	}

	/**
	 * получить сендер
	 * @param OS
	 * @return
	 */
	private BaseNotificationSender getNotificationSender(OperationSystemTypes OS) {
		return sendersRegistry.get(OS);
	}
	
	private boolean isHostActive() {
		return (clusterManager == null || clusterManager.isActive());
	}
	
	@Override
	public void doRun() {
		// продолжаем только если являемся активным узлом кластера
		if (!isHostActive())
			return;
		ExecutionTimeProfiler.start("PushNotificationService.forAllClients");
		
		List<PushNotification> notifications = null;
		do {
			// получаем общий список уведомлений (максимум amount уведомлений)
			notifications = pushNotificationDao.getNotificationsForSend(amount);
			 
			ClientsToNotificationMapper<OperationSystemTypes> mapper = new ClientsToNotificationMapper<OperationSystemTypes>();
			List<PushNotification> noClients = new ArrayList<PushNotification>(); // лист с уведомлениями без клиентов
			
			// пробегаем по всем найденным к отправке уведомлениям
			for(PushNotification notification:notifications) {
				
				// подгружаем список клиентов, для которых должно быть отослано данное увеомление (упорядоченны по OS и ApplicationCode)
				List<PushNotificationClient> clients = pushNotificationDao.getClientsByNotification(notification.getNotificationId(),0,-1);
				
				//делим на группы по OS
				int index = 0; 								//индекс клиента, начиная с которого будет начинаться следующая группа  
				OperationSystemTypes os = null;				//		для заданных ОС
				
				if (clients.size()>0) {
					os = clients.get(0).getOsCode();
				} else {
					noClients.add(notification);
					continue;
				}
				// в цикле выделяем часть клиентов с одинаковыми ОС
				for (int i=0;i<clients.size();i++) {
					
					if (!clients.get(i).getOsCode().equals(os)) {
						//добавляем клиентов от index до i для этого уведомления
						mapper.add(os, notification, clients.subList(index, i));
						
						//обновляем параметры для следующей группы клиентов
						index = i;
						os = clients.get(i).getOsCode();
					}	
				}
				
				//добавляем оставшихся клиентов (последнюю группу) для этого уведомления
				mapper.add(os, notification, clients.subList(index, clients.size()));
			}
			
			//отправляем уведомления по различным сендерам
			for (OperationSystemTypes os: mapper.getTypes()) {
				sendNotification(os, mapper.getNotificationsFor(os), mapper.getClientsFor(os));
			}
			if (noClients.size() > 0) {
				// не было найдено ни 1 клиента (возможно, токен клиента инвалидировался)
				saveResult(noClients, null, "Invalid token");
			}
			
			// в любом случае считаем уведомления отосланными, чтоьбы они не копились в случае чего.
			pushNotificationDao.notificationWasSent(notifications);
		} while (notifications.size() > 0 && !shouldInternalTaskStop());
		
		ExecutionTimeProfiler.finish("PushNotificationService.forAllClients");
	}
	
	private void sendNotification(OperationSystemTypes os, List<PushNotification> notifications, List<List<PushNotificationClient>> clients) {
		BaseNotificationSender sender = getNotificationSender(os);
		if (sender!=null) {
			List<PushedNotificationInfo> info = sender.sendNotification(notifications, clients);
			saveResult(info);			
		} else {
			String msg = "Не найден сендер для OS " + os;
			tagLog(msg);
			saveResult(notifications, clients, msg);
		}
	}
	
	private void saveResult(List<PushedNotificationInfo> notifications) {
		//уведомляем слушателей об отправленых уведомлениях
		fireNotificationSend(notifications);
		// обновляем статусы
		pushNotificationDao.setClientStatusForNotification(notifications);
	}
	
	private void saveResult(List<PushNotification> notifications, List<List<PushNotificationClient>> clients, String error) {
		//уведомляем слушателей об ошибке отправки уведомления
		fireNotificationSend(notifications, clients, error);
		// обновляем статусы
		if (clients==null) 
			pushNotificationDao.setClientStatusForNotification(notifications, error);
		else
			pushNotificationDao.setClientStatusForNotification(notifications, clients, error);
	}
	
	private void fireNotificationSend(List<PushedNotificationInfo> notifications) {
		for (PushNotificationListener listener: listeners) {
			listener.pushNotificationsWasSent(new PushResult(notifications));			
		}
	}
	
	private void fireNotificationSend(List<PushNotification> notifications, List<List<PushNotificationClient>> clients, String error) {
		for (PushNotificationListener listener: listeners) {
			listener.pushNotificationsWasSent(new PushResult(notifications, clients, error));			
		}
	}
	
	public void setAmount(String amount) {
		this.amount = Integer.valueOf(amount);
	}
	
}
