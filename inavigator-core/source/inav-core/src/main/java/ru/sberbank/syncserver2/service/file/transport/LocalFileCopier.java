package ru.sberbank.syncserver2.service.file.transport;

import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.util.FileCopyHelper;

import java.io.File;

import ru.sberbank.syncserver2.util.FileHelper;

/**
 * @author Sergey Erin
 * @deprecated It is unclear when we should stop copying so this class should not be used
 */
public class LocalFileCopier extends SingleThreadBackgroundService {
    private String localSourceFolder;
    private String localTempFolder;
    private String localDestFolder1;
    private String localDestFolder2;
    private String localDestFolder3;
    private String localDestFolder4;
    private String localDestFolder5;

    public LocalFileCopier() {
        super(60); //10 seconds to waite between executions
    }

    public String getLocalSourceFolder() {
        return localSourceFolder;
    }

    public void setLocalSourceFolder(String localSourceFolder) {
        this.localSourceFolder = localSourceFolder;
    }

    public String getLocalTempFolder() {
        return localTempFolder;
    }

    public void setLocalTempFolder(String localTempFolder) {
        this.localTempFolder = localTempFolder;
    }

    public String getLocalDestFolder1() {
        return localDestFolder1;
    }

    public void setLocalDestFolder1(String localDestFolder1) {
        this.localDestFolder1 = localDestFolder1;
    }

    public String getLocalDestFolder2() {
        return localDestFolder2;
    }

    public void setLocalDestFolder2(String localDestFolder2) {
        this.localDestFolder2 = localDestFolder2;
    }

    public String getLocalDestFolder3() {
        return localDestFolder3;
    }

    public void setLocalDestFolder3(String localDestFolder3) {
        this.localDestFolder3 = localDestFolder3;
    }

    public String getLocalDestFolder4() {
        return localDestFolder4;
    }

    public void setLocalDestFolder4(String localDestFolder4) {
        this.localDestFolder4 = localDestFolder4;
    }

    public String getLocalDestFolder5() {
        return localDestFolder5;
    }

    public void setLocalDestFolder5(String localDestFolder5) {
        this.localDestFolder5 = localDestFolder5;
    }

    @Override
    public String toString() {
        return "LocalFileMover{" +
                "localSourceFolder='" + localSourceFolder + '\'' +
                ", localDestFolder1='" + localDestFolder1 + '\'' +
                ", localDestFolder2='" + localDestFolder2 + '\'' +
                ", localDestFolder3='" + localDestFolder3 + '\'' +
                ", localDestFolder4='" + localDestFolder4 + '\'' +
                ", localDestFolder5='" + localDestFolder5 + '\'' +
                '}';
    }

    public void doInit() {
        FileHelper.createMissingFolders(localSourceFolder,localTempFolder,localDestFolder1,localDestFolder2,localDestFolder3,localDestFolder4,localDestFolder5);
    }

    public void doRun() {
        //1. Check if new file is available, sleep if no files available
        File[] files = new File(localSourceFolder).listFiles();
        if(files ==null || files.length==0){
            return;
        }

        //2. Copy file to
        File tempFolderFile = new File(localTempFolder);
        for (int i = 0; i < files.length; i++) {
            File src = files[i];
            copy(src, tempFolderFile , new File(localDestFolder1));
            copy(src, tempFolderFile , new File(localDestFolder2));
            copy(src, tempFolderFile , new File(localDestFolder3));
            copy(src, tempFolderFile , new File(localDestFolder4));
            copy(src, tempFolderFile , new File(localDestFolder5));
        }
    }

    private void copy(File src, File tmpFolder, File dstFolder) {
        //1. Check existense
        if(dstFolder==null){
            return;
        }
        if(!dstFolder.exists() || !dstFolder.isDirectory()){
            log(LogEventType.ERROR, "ConfigLoader " + dstFolder + " does not exists or not directory");
            return;
        }

        //2. Copy
        File dst = new File(dstFolder, src.getName());
        log(LogEventType.GEN_DEBUG, "Start copy file from " + src + " to " + dst);
        File tmp = new File(tmpFolder, src.getName());
        try {
            FileCopyHelper.reliableCopy(src,tmp);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //3. Move to target folder
        FileCopyHelper.reliableDelete(dst);
        tmp.renameTo(dst);
        log(LogEventType.GEN_DEBUG, "Finish copy file from " + src + " to " + dst);
    }

    public void log(LogEventType eventType, String txt) {
        logger.info(txt);
        logServiceMessage(eventType, txt);
    }
}
