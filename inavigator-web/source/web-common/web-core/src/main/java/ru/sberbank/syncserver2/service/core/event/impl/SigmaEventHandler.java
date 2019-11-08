package ru.sberbank.syncserver2.service.core.event.impl;

import java.text.SimpleDateFormat;

import org.springframework.beans.factory.annotation.Autowired;

import ru.sberbank.syncserver2.gui.db.dao.PushNotificationDao;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.event.AbstractEventInfo;
import ru.sberbank.syncserver2.service.core.event.BaseEventPerformResult;
import ru.sberbank.syncserver2.service.core.event.AbstractSystemEventHandler;
import ru.sberbank.syncserver2.service.core.event.NotFoundEventHandlerException;
import ru.sberbank.syncserver2.service.file.fragments.FileFragmentsTransportHelper;
import ru.sberbank.syncserver2.service.file.transport.SharedSigmaNetworkFileMover;
import ru.sberbank.syncserver2.service.generator.single.data.ActionState;
import ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger;
import ru.sberbank.syncserver2.service.pushnotifications.model.OperationSystemTypes;

public class SigmaEventHandler extends AbstractSystemEventHandler {

	private PushNotificationDao pushNotificationDao;
	
	@Autowired
	public void setPushNotificationDao(PushNotificationDao pushNotificationDao) {
		this.pushNotificationDao = pushNotificationDao;
	}

	public SigmaEventHandler() {
	}
	
	/**
	 * Отправка PUSH уведомления если загружена новая версия файла 
	 */
	private String sendPushNotificationWhenFileLoaded = "false";
	
	public String getSendPushNotificationWhenFileLoaded() {
		return sendPushNotificationWhenFileLoaded;
	}

	public void setSendPushNotificationWhenFileLoaded(
			String sendPushNotificationWhenFileLoaded) {
		this.sendPushNotificationWhenFileLoaded = sendPushNotificationWhenFileLoaded;
	}

	@Override
	public BaseEventPerformResult performEvent(AbstractEventInfo eventInfo) {
		if (eventInfo instanceof FileInflateFinishedEventInfo)
			return fileInflaterFinishedEvent((FileInflateFinishedEventInfo)eventInfo);
		else if (eventInfo instanceof FileLoadedToFileCacheEventInfo) {
			return fileLoadedToCacheEvent((FileLoadedToFileCacheEventInfo)eventInfo);
		} else
			throw new NotFoundEventHandlerException(eventInfo != null?eventInfo.toString():null);
	}
	
	public BaseEventPerformResult fileInflaterFinishedEvent(FileInflateFinishedEventInfo eventInfo) {
		
		if (eventInfo.isSuccesfull()) { 
			// Выставляем лампочку этапа передача файла в сигму в "УСПЕШНО"
			DataPowerNotificationLogger datapowerNotificationLogger = (DataPowerNotificationLogger) getServiceContainer().getServiceManager().findFirstServiceByClassCode(DataPowerNotificationLogger.class);
			datapowerNotificationLogger.addGenStaticFileEvent(eventInfo.getFileName(), ActionState.PHASE_SENDING_TO_SIGMA, ActionState.STATUS_COMPLETED_SUCCESSFULLY,"");
			// Сбрасываем уведомление о том, что файл не доставлен в сигму
	        SharedSigmaNetworkFileMover sharedSigmaNetworkFileMover = ((SharedSigmaNetworkFileMover)ServiceManager.getInstance().findFirstServiceByClassCode(SharedSigmaNetworkFileMover.class)); 
	        sharedSigmaNetworkFileMover.logNotification(eventInfo.getFileName(),eventInfo.getFileMd5());

		} else {
    		// Уведомляем альфу о том, что файл поврежден и доставлен не будет
            SharedSigmaNetworkFileMover sharedSigmaNetworkFileMover = ((SharedSigmaNetworkFileMover)ServiceManager.getInstance().findFirstServiceByClassCode(SharedSigmaNetworkFileMover.class)); 
            sharedSigmaNetworkFileMover.logNotificationError("File " + FileFragmentsTransportHelper.getSourceFileNameFromFragmentFile(eventInfo.getFileName()) + " is corrupted and we failed to deliver it to Sigma.");
		}
		
		return new BaseEventPerformResult(true);
	}

	public BaseEventPerformResult fileLoadedToCacheEvent(FileLoadedToFileCacheEventInfo eventInfo) {
		// если отправка уведолмений отключена, то выходим из метода 
		if (!sendPushNotificationWhenFileLoaded.equals("true"))
			return new BaseEventPerformResult(true);
		
		
		if (eventInfo == null || eventInfo.getFileInfo() == null) 
			return new BaseEventPerformResult(false);
			
		if (!eventInfo.getFileInfo().isDraft()) {
			try {
				pushNotificationDao.addPushNotificationToQueue(
						"Внимание! На сервере появились новые данные. Дата актуальности " + eventInfo.getFileInfo().getLastModified(),
						OperationSystemTypes.IOS.toString(),
						eventInfo.getFileInfo().getApp(),
						null,
						new String[] {},
						new String[] {}
				);
			} catch (Exception e) {
				tagLogger.log("Ошибка при создании push уведомления о новых данных: "+e.toString());
				logger.error(e, e);
			}
		}
			
		
		return new BaseEventPerformResult(true);
	}
	
	
}
