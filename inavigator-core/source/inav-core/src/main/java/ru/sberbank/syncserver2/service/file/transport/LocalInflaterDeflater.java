package ru.sberbank.syncserver2.service.file.transport;

import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;

import java.io.File;

/**
 * @author Leonid Kozhinskiy
 *
 */
public abstract class LocalInflaterDeflater extends SingleThreadBackgroundService {
    private String localSourceFolder;
    private String localTempFolder1;
    private String localTempFolder2;
    private String localDestFolder;
    private String serviceCode;

    public LocalInflaterDeflater() {
        super(60); //10 seconds to waite between executions
    }

    public String getLocalSourceFolder() {
        return localSourceFolder;
    }

    public void setLocalSourceFolder(String localSourceFolder) {
        this.localSourceFolder = localSourceFolder;
    }

    public String getLocalTempFolder1() {
        return localTempFolder1;
    }

    public void setLocalTempFolder1(String localTempFolder1) {
        this.localTempFolder1 = localTempFolder1;
    }

    public String getLocalTempFolder2() {
        return localTempFolder2;
    }

    public void setLocalTempFolder2(String localTempFolder2) {
        this.localTempFolder2 = localTempFolder2;
    }

    public String getLocalDestFolder() {
        return localDestFolder;
    }

    public void setLocalDestFolder(String localDestFolder) {
        this.localDestFolder = localDestFolder;
    }


    @Override
    public void doInit() {
        //1. Getting serviceBeanCode
        this.serviceCode = getServiceBeanCode();
        FileHelper.createMissingFolders(localSourceFolder, localTempFolder1, localTempFolder2, localDestFolder);
    }

    public void doRun() {
        //1. Check if new file is available, sleep if no files available
        File[] files = new File(localSourceFolder).listFiles();
        if(files==null){
            files = new File[0];
        }

        //2. Move file to temp folder
        for (int i = 0; i < files.length; i++) {
            File src = files[i];
            File tempOriginal = new File(localTempFolder1, src.getName());
            tagLogger.log(src.getName(),"Start moving file from "+src.getAbsolutePath()+" to "+tempOriginal.getAbsolutePath());
            FileCopyHelper.reliableDelete(tempOriginal);
            long srcTime = src.lastModified();
            if(src.renameTo(tempOriginal)){
            	tempOriginal.setLastModified(srcTime);
                FileCopyHelper.reliableDelete(src);
            }
        }

        //3. Unzip all files in temp folder1 to tempFolder2 and move them to destination
        files = new File(localTempFolder1).listFiles();
        if(files!=null && files.length>0){
            for (int i = 0; i < files.length; i++) {
                //3.1. Unpacking every file
                File tempOriginal = files[i];
                String fileName = tempOriginal.getName();
                final File tempChanged = new File(localTempFolder2, tempOriginal.getName());
                File dst = new File(localDestFolder, fileName);
                tagLogger.log(tempOriginal.getName(),"Start "+getActionGerund()+" "+tempOriginal.getAbsolutePath()+" to "+tempChanged.getAbsolutePath());
                // готовим параметры для передачи в класс наследник
                DeflaterInflaterRunParamHolder deflaterInflaterRunParam = new DeflaterInflaterRunParamHolder(tempOriginal,tempChanged);

                // вызываем метод класса наследника и все дальнейшие действия выполняем предполагая что параметры внутри метода action могли изменится
                if(action(deflaterInflaterRunParam)){
                    FileCopyHelper.reliableDelete(deflaterInflaterRunParam.getSrc());
                    copyActionResultToDestination(deflaterInflaterRunParam.getDest(),new File(localDestFolder));
                    tagLogger.log(deflaterInflaterRunParam.getSrc().getName(),"Finish "+getActionGerund()+" "+deflaterInflaterRunParam.getSrc().getAbsolutePath()+" to "+deflaterInflaterRunParam.getDest().getAbsolutePath()+" with success and moved to "+dst.getAbsolutePath());
                } else {
                    tagLogger.log(deflaterInflaterRunParam.getSrc().getName(),"Finish "+getActionGerund()+" "+deflaterInflaterRunParam.getSrc().getAbsolutePath()+" to "+" "+deflaterInflaterRunParam.getDest().getAbsolutePath()+" with error");
                    logObjectEvent(LogEventType.ERROR, fileName, "Failed to "+getActionVerb()+" " + dst.getName());
                }

                //3.2. Check if we should stop
                if(shouldInternalTaskStop()){
                    return;
                }
            }
        }
    }

    public abstract boolean action(DeflaterInflaterRunParamHolder defalterInflaterRunParam);

    public abstract String getActionVerb();

    public abstract String getActionGerund();
    
    public abstract void copyActionResultToDestination(File tempChanged, File dst);
    
    protected DataPowerNotificationLogger getDatapowerNotificationLogger() {
    	return (DataPowerNotificationLogger) getServiceContainer().getServiceManager().findFirstServiceByClassCode(DataPowerNotificationLogger.class);
    }    

}
