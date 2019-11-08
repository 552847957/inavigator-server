package ru.sberbank.syncserver2.service.file.transport;

import ru.sberbank.syncserver2.gui.db.DatabaseManager;
import ru.sberbank.syncserver2.gui.web.DatabaseController;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.event.impl.FileDeflateFinishedEventInfo;
import ru.sberbank.syncserver2.service.file.fragments.FileFragmentsTransportHelper;
import ru.sberbank.syncserver2.service.generator.single.AbstractGenerator;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.MD5Helper;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author Leonid Kozhinskiy
 *
 */
public class LocalDeflater extends LocalInflaterDeflater {
	
	/**
	 * Поле сервиса - максимальный размер фрагмента файла передаваемого через ФП
	 */
	private String maxFragmentFileSizeMb;
	
    public String getMaxFragmentFileSizeMb() {
		return maxFragmentFileSizeMb;
	}

	public void setMaxFragmentFileSizeMb(String maxFragmentFileSizeMb) {
		this.maxFragmentFileSizeMb = maxFragmentFileSizeMb;
	}

	@Override
    public boolean action(DeflaterInflaterRunParamHolder deflaterInflaterRunParams) {
        String dataFileName = deflaterInflaterRunParams.getSrc().getName();
        try {
        	
            logObjectEvent(LogEventType.GEN_TRANSFER_START,dataFileName, "Start transferring " + deflaterInflaterRunParams.getSrc().getName()+" to Sigma");
            logObjectEvent(LogEventType.GEN_DEBUG,dataFileName, "Start deflating " + deflaterInflaterRunParams.getSrc().getName());
            
            // вызываем обработчик события о том, что произошла генерация 
            getServiceContainer().getServiceManager().getSystemEventHandler().performEvent(new FileDeflateFinishedEventInfo(deflaterInflaterRunParams, dataFileName));
            
            FileFragmentsTransportHelper.divideIntpufileIntoFragments(deflaterInflaterRunParams.getSrc(), deflaterInflaterRunParams.getDest().getParentFile(),Integer.valueOf(maxFragmentFileSizeMb));
            
            
            return true;
        } finally {
            logObjectEvent(LogEventType.GEN_DEBUG,dataFileName, "Finish deflating " + deflaterInflaterRunParams.getSrc().getName());
        }
    }
	
    @Override
	public void copyActionResultToDestination(final File tempChanged, File dst) {
        // Ищем все фрагменты на которые был разделен файл
        File[] changedFileList = tempChanged.getParentFile().listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(tempChanged.getName());
			}
		});
        // переносим все фрагменты файла в выходную папку
        for(File currentChangedFileList:changedFileList) {
        	File dstChangedFile = new File(dst, currentChangedFileList.getName());
        	FileCopyHelper.reliableRename(currentChangedFileList,dstChangedFile);
        }
	}

	@Override
    public String getActionVerb() {
        return "deflate";
    }

    @Override
    public String getActionGerund() {
        return "deflating";
    }

}
