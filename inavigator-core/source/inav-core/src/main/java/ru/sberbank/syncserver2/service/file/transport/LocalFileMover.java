package ru.sberbank.syncserver2.service.file.transport;

import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;

import java.io.File;

import ru.sberbank.syncserver2.util.FileHelper;

/**
 * @author Sergey Erin
 *
 */
public class LocalFileMover extends SingleThreadBackgroundService {
    private String localSourceFolder;
    private String localDestFolder;
    private String serviceCode;

    public LocalFileMover() {
        super(60); //10 seconds to waite between executions
    }

    public String getLocalSourceFolder() {
        return localSourceFolder;
    }

    public void setLocalSourceFolder(String localSourceFolder) {
        this.localSourceFolder = localSourceFolder;
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
        FileHelper.createMissingFolders(localSourceFolder, localDestFolder);
    }

    public void doRun() {
        //1. Check if new file is available, sleep if no files available
        File[] files = new File(localSourceFolder).listFiles();
        if(files ==null || files.length==0){
            tagLogger.log(new String[]{serviceCode,localSourceFolder}, "Listing files in " + localSourceFolder);
            return;
        }

        //2. Copy file to
        for (int i = 0; i < files.length; i++) {
            //2.1. Copying
            File src = files[i];
            File dst= new File(localDestFolder, src.getName());
            String msg;
            if(!dst.exists()){
                msg = "Move file from "+src+" to "+dst;
				logObjectEventWithTags(LogEventType.GEN_DEBUG, src.getName(), new String[]{serviceCode,src.getName()},msg);
				src.renameTo(dst);

            } else {
            	msg = "Skipped moving file from "+src+" to "+dst+" because destination exists";
                tagLogger.log(new String[]{serviceCode,src.getName()},msg);
            }
            logObjectEvent(LogEventType.GEN_DEBUG, src.getName(),msg);

            //2.2. Check if we should stop
            if(shouldInternalTaskStop()){
                return;
            }
        }
    }

}
