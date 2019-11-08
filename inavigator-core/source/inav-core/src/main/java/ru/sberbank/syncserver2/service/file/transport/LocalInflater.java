package ru.sberbank.syncserver2.service.file.transport;

import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.event.impl.FileDeflateFinishedEventInfo;
import ru.sberbank.syncserver2.service.core.event.impl.FileInflateFinishedEventInfo;
import ru.sberbank.syncserver2.service.file.fragments.FileFragmentOperationResult;
import ru.sberbank.syncserver2.service.file.fragments.FileFragmentOperationResultTypes;
import ru.sberbank.syncserver2.service.file.fragments.FileFragmentsTransportHelper;
import ru.sberbank.syncserver2.service.generator.single.data.ActionState;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.MD5Helper;

import java.io.File;

/**
 * @author Leonid Kozhinskiy
 *
 */
public class LocalInflater extends LocalInflaterDeflater {
	@Override
    public boolean action(DeflaterInflaterRunParamHolder deflaterInflaterRunParams) {
        String dataFileName = deflaterInflaterRunParams.getSrc().getName();
        try {
            setLastActionComment("Start inflating " + deflaterInflaterRunParams.getSrc().getName());
            logObjectEvent(LogEventType.GEN_DEBUG,dataFileName, "Start inflating " + deflaterInflaterRunParams.getSrc().getName());
            try {
            	
            	// добавление очередного фрагмента к собираемому файлу 
            	FileFragmentOperationResult result = FileFragmentsTransportHelper.collectAllFragmentsAndCreateSourceFile(deflaterInflaterRunParams.getSrc(),deflaterInflaterRunParams.getDest().getParentFile());
            	
            	if (result != null && result.getResultType().equals(FileFragmentOperationResultTypes.WHOLE_FILE_SUCCESSFULLY_COLLECTED)) {
                    // вызываем обработчик события о том, что произошла генерация 
                    getServiceContainer().getServiceManager().getSystemEventHandler().performEvent(new FileInflateFinishedEventInfo(result.getFileName(), MD5Helper.getCheckSumAsString(new File(deflaterInflaterRunParams.getDest().getParentFile(),result.getFileName()).getAbsolutePath()), true));
            	}
            	return !result.getResultType().isError(); 
            } catch (Exception ex) {
                // вызываем обработчик события о том, что произошла генерация 
                getServiceContainer().getServiceManager().getSystemEventHandler().performEvent(new FileInflateFinishedEventInfo(dataFileName, null, false));
            	return false;
            }
        } finally {
            logObjectEvent(LogEventType.GEN_DEBUG,dataFileName, "Finish inflating " + deflaterInflaterRunParams.getSrc().getName());
            setLastActionComment("Finish inflating " + deflaterInflaterRunParams.getSrc().getName());
        }
    }
	
    @Override
	public void copyActionResultToDestination(File tempChanged, File dst) {
        File collectedFile = FileFragmentsTransportHelper.getResultFileAndCheckIfFileIsCollected(tempChanged);
        if (collectedFile != null)
        	FileCopyHelper.reliableRename(new File(tempChanged.getParent(),collectedFile.getName()),new File(dst,collectedFile.getName()));
	}

	@Override
    public String getActionVerb() {
        return "inflate";
    }

    @Override
    public String getActionGerund() {
        return "inflating";
    }
}
