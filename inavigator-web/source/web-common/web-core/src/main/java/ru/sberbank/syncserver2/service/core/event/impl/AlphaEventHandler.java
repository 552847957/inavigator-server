package ru.sberbank.syncserver2.service.core.event.impl;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;

import ru.sberbank.syncserver2.gui.db.DatabaseManager;
import ru.sberbank.syncserver2.gui.db.dao.GeneratorDao;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.event.AbstractEventInfo;
import ru.sberbank.syncserver2.service.core.event.BaseEventPerformResult;
import ru.sberbank.syncserver2.service.core.event.AbstractSystemEventHandler;
import ru.sberbank.syncserver2.service.core.event.NotFoundEventHandlerException;
import ru.sberbank.syncserver2.service.file.transport.AlphaNetworkFileMover;
import ru.sberbank.syncserver2.service.file.transport.DeflaterInflaterRunParamHolder;
import ru.sberbank.syncserver2.service.generator.single.AbstractGenerator;
import ru.sberbank.syncserver2.util.MD5Helper;

public class AlphaEventHandler extends AbstractSystemEventHandler {

	private GeneratorDao generatorDao;
	
	@Autowired
	public void setGeneratorDao(GeneratorDao generatorDao) {
		this.generatorDao = generatorDao;
	}

	@Override
	public BaseEventPerformResult performEvent(AbstractEventInfo eventInfo) {
		if (eventInfo instanceof FileDeflateFinishedEventInfo)
			return fileDeflateFinishedEvent((FileDeflateFinishedEventInfo) eventInfo);
		else
			throw new NotFoundEventHandlerException(eventInfo!=null?eventInfo.toString():null);
	}
	
	/**
	 * Событие - завершен процесс разбиения на фрагменты
	 * @param eventInfo
	 */
	public BaseEventPerformResult fileDeflateFinishedEvent(FileDeflateFinishedEventInfo eventInfo) {
		String dataFileName = eventInfo.getDataFileName();
		DeflaterInflaterRunParamHolder deflaterInflaterRunParams = eventInfo.getDeflaterInflaterRunParamHolder();
		
		// Определяем force- публикацию
    	boolean forcePublish = false; 
    	if (dataFileName.endsWith(AbstractGenerator.FORCE_PUBLISH_EXT)) {
    		// выставляем флаг что публикация в чистовик
    		forcePublish = true;
    		// убираем расширение из имени файла. оно нам больше не потребуется
    		dataFileName = dataFileName.substring(0, dataFileName.length()-AbstractGenerator.FORCE_PUBLISH_EXT.length());
    		File newSrc = new File(deflaterInflaterRunParams.getSrc().getParent(),dataFileName); 
    		File newDest = new File(deflaterInflaterRunParams.getDest().getParent(),dataFileName); 
    		deflaterInflaterRunParams.getSrc().renameTo(newSrc);
    		deflaterInflaterRunParams.setSrc(newSrc);
    		deflaterInflaterRunParams.setDest(newDest);
    	}
    	
        try {
            // уведомление о том, что файл не доставлен в сигму
            AlphaNetworkFileMover alphaNetworkFileMover = ((AlphaNetworkFileMover)ServiceManager.getInstance().findFirstServiceByClassCode(AlphaNetworkFileMover.class)); 
            String hosts[] = alphaNetworkFileMover.getSharedHosts(dataFileName);
            String fileMd5 = MD5Helper.getCheckSumAsString(deflaterInflaterRunParams.getSrc().getAbsolutePath());
            alphaNetworkFileMover.logNotification(dataFileName, fileMd5, hosts);
            
            // ОБновление информации о сгенерированном файле
           DatabaseManager db = new DatabaseManager(ServiceManager.getInstance().getConfigSource());
           if (!forcePublish) {
        	   boolean isDraftMode = generatorDao.getDraftGenertionModeForFile(null, dataFileName);
        	   generatorDao.changeStaticFileStatus(null,dataFileName, isDraftMode?null:fileMd5, isDraftMode?fileMd5:null);
           } else 
        	   generatorDao.changeStaticFileStatus(null,dataFileName, fileMd5, null);
           
        } catch (Exception ex) {
        	ex.printStackTrace();
        }

        return new BaseEventPerformResult(true);
	}
}
