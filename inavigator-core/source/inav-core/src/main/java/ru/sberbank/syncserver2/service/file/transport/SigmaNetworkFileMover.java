package ru.sberbank.syncserver2.service.file.transport;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

import ru.sberbank.syncserver2.service.core.FileServiceWithDpLogging;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger;
import ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger;
import ru.sberbank.syncserver2.util.ClusterHookProvider;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;

/**
 * @author Leonid Kozhinskiy
 *
 */
public abstract class SigmaNetworkFileMover extends FileServiceWithDpLogging {
    protected String networkSourceFolder;
    protected String localTempFolder;
    protected String localDestFolder;
    protected String serviceCode;
    private ThreadLocal datapowerNotificationLogger = new ThreadLocal();
    private String debugModeWithSMSOnDelivery = "false";
    protected String localHostName;

    public SigmaNetworkFileMover() {
        super(60); //10 seconds to waite between executions
        try {
            localHostName = InetAddress.getLocalHost().getHostName();
            if (ClusterHookProvider.isClusterHooked()) {
            	localHostName += "_"+ClusterHookProvider.getSuffixForHook();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } finally {
        }
    }

    public String getLocalDestFolder() {
        return localDestFolder;
    }

    public void setLocalDestFolder(String localDestFolder) {
        this.localDestFolder = localDestFolder;
    }

    public String getLocalTempFolder() {
        return localTempFolder;
    }

    public void setLocalTempFolder(String localTempFolder) {
        this.localTempFolder = localTempFolder;
    }

    public String getNetworkSourceFolder() {
        return networkSourceFolder;
    }

    public void setNetworkSourceFolder(String networkSourceFolder) {
        this.networkSourceFolder = networkSourceFolder;
    }

    public String getDebugModeWithSMSOnDelivery() {
        return debugModeWithSMSOnDelivery;
    }

    public void setDebugModeWithSMSOnDelivery(String debugModeWithSMSOnDelivery) {
        this.debugModeWithSMSOnDelivery = debugModeWithSMSOnDelivery;
    }

    @Override
    public String toString() {
        return "NetworkFileMover [localDestFolder=" + localDestFolder + ", networkSourceFolder="
                + networkSourceFolder + "]";
    }

    public void doInit() {
        this.serviceCode = getServiceBeanCode();
        FileHelper.createMissingFolders(networkSourceFolder, localTempFolder, localDestFolder);
    }

    public abstract void doRun();
    /*
    public void doRun() {
        try {
            //1. Check if new file is available, sleep if no files available
            List files = listWrittenFiles(networkSourceFolder);

            //2. Copy files to temp local folder and try to copy to target folder
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                //2.1. Declaring file names
                File networkFile = (File) iterator.next();
                File tempFile    = new File(localTempFolder  , networkFile.getName());
                File localFile   = new File(localDestFolder, networkFile.getName());

                if (networkFile.isDirectory()) {
                    return;
                }

                //2.2. Copy files to temp local folder and check whether MD5 matches
                String[] tags = new String[]{serviceCode,networkFile.getName(), localTempFolder, localDestFolder};
                logObjectEventToDataPower(LogEventType.GEN_DEBUG, networkFile.getName(), "Start moving file from network in Sigma to local disk: " + networkFile + " to " + tempFile);
                String md5 =FileCopyHelper.copyAndRemoveMD5(networkFile, tempFile);
                logNotification(tempFile.getName(), md5);
                if(md5!=null){
                    FileCopyHelper.reliableDelete(networkFile);
                }
                logObjectEventToDataPower(LogEventType.GEN_DEBUG, networkFile.getName(), "Finish moving file from network in Sigma to local disk: " + networkFile + " to " + tempFile);
                //logObjectEventToDataPower(LogEventType.GEN_TRANSFER_FINISH, networkFile.getName(), "File " + networkFile.getName() + " delivered to server in Sigma ");

                //2.3. If file with this name already exists in target folder, then file is pending in temp folder and will be processed later
                moveLocalFileFromTempToDest(tempFile, localFile, tags);

                //2.4. If service is stopped then we stop
                if(shouldInternalTaskStop()){
                    return;
                }
            }

            //3. Processing pending files
            movePendingLocalFiles();
        } finally {
        }
    }

    protected void moveLocalFileFromTempToDest(File tempFile, File localFile, String[] tags) {
        //If file with this name already exists in target folder, then file is pending in temp folder and will be processed later
        logObjectEventWithTags(LogEventType.GEN_DEBUG, tempFile.getName(),tags, "Start renaming from " + tempFile + " to " + localFile);
        
        //TODO: проверить корректность условия if ( на текущий момент вызов не используется)
        if(!localFile.exists()){
            tempFile.renameTo(localFile);
            logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Finish renaming from " + tempFile + " to " + localFile);
            logObjectEventToDataPower(LogEventType.GEN_TRANSFER_FINISH, tempFile.getName(), "File " + tempFile + " has been delivered to "+localHostName+" in Sigma");
        } else {
            logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Failed to finish renaming from "+tempFile+" to "+localFile);
        }
    }

    protected void movePendingLocalFiles() {
        String[] tags = new String[]{serviceCode,localTempFolder};
        tagLogger.log(tags, "Start listing files in " + localTempFolder);
        File[] pendingFiles = new File(localTempFolder).listFiles();
        if(pendingFiles==null){
            return;
        }
        for (int f=0; f<pendingFiles.length; f++) {
            //2.1. Skip temporary files in process
            File file = pendingFiles[f];
            String fileName = file.getName();
            if(fileName!=null && fileName.endsWith(SharedFileLocker.TEMP_FILE)){
                FileCopyHelper.reliableDelete(file);
                continue;
            }

            //2.2. Declaring file names
            File tempFile    = new File(localTempFolder  , file.getName());
            File localFile   = new File(localDestFolder, file.getName());

            //2.3. Try to rename
            moveLocalFileFromTempToDest(tempFile,localFile,new String[]{serviceCode,tempFile.getName()});
        }
    }*/

    public List<File> listWrittenFiles(String folder){
        //1. Declaring
        String[] tags = new String[]{serviceCode,folder};
		//tagLogger.log(tags, "Start listing files in "+folder);
        ArrayList<File> result = new ArrayList<File>(3);
        if(folder!=null && !(folder.endsWith("\\") || folder.endsWith("/"))){
            folder += "/";
        }

        //2. Listing all files older then one minute before now
        long maxLastModified = System.currentTimeMillis() - 60*1000;
        File fld = new File(folder);
        File[] files = fld.listFiles();
        if (files == null || files.length==0) {
            if(!fld.exists() || !fld.isDirectory()){
                String txt = "Check property networkSourceFolder or networkSharedFolder for "+serviceCode+". It is equal " + folder + " and folder is not accessible. It is "
                           + (fld.isDirectory() ? "a directory " : " not a directory")
                           + " and it "
                           + (fld.exists()      ? "exists"       : "does not exists");
                tagLogger.log(tags, txt);
            } else {
                String txt = "No files found in "+fld.getAbsolutePath();
                tagLogger.log(tags, txt);
            }
            return result;
        }
        Arrays.sort(files, new FileNameComparator());
        String txt = "found "+files.length+" new files in "+folder+". Start checking for LOK's";
        logServiceMessageWithTags(LogEventType.GEN_DEBUG, tags, txt);
        for(int i=0; i<files.length; i++){
            if(files[i]==null){
                txt = "unexpected error: file is null while listing files at network folder";
                logServiceMessageWithTags(LogEventType.ERROR, tags, txt);
                continue;
            }

            if(files[i].lastModified()>maxLastModified){
                txt = "file "+files[i].getAbsolutePath()+" is too new";
                logServiceMessageWithTags(LogEventType.GEN_DEBUG, tags, txt);
                continue;
            }

            if(files[i].getName().endsWith(".LOK")){
                //txt = "file "+files[i].getAbsolutePath()+" is a special LOK file";
                //logServiceMessageWithTags(LogEventType.FILE_MOVE_START, tags, txt);
                continue;
            }

            if(files[i].getName().endsWith(SharedMoveLock.CHECKSUM_LOCK_SUFFIX)){
                //txt = "file "+files[i].getAbsolutePath()+" is a special "+SharedMoveLock.CHECKSUM_LOCK_SUFFIX +" file";
                //logServiceMessageWithTags(LogEventType.FILE_MOVE_START, tags, txt);
                continue;
            }

            if(files[i].getName().endsWith(SharedMoveLock.CONFIRM_LOCK_SUFFIX)){
                //txt = "file "+files[i].getAbsolutePath()+" is a special "+SharedMoveLock.CONFIRM_LOCK_SUFFIX +" file";
                //logServiceMessageWithTags(LogEventType.FILE_MOVE_START, tags, txt);
                continue;
            }

            if(files[i].getName().endsWith(SharedFileLocker.START_LOCK)){
                //txt = "file "+files[i].getAbsolutePath()+" is a special "+SharedFileLocker.START_LOCK +" file";
                //logServiceMessageWithTags(LogEventType.FILE_MOVE_START, tags, txt);
                continue;
            }

            if(files[i].getName().endsWith(SharedFileLocker.FINISH_LOCK)){
                //txt = "file "+files[i].getAbsolutePath()+" is a special "+SharedFileLocker.FINISH_LOCK +" file";
                //logServiceMessageWithTags(LogEventType.FILE_MOVE_START, tags, txt);
                continue;
            }

            if(new File(files[i].getAbsolutePath()+".LOK").exists()){
                //txt = "file "+files[i].getAbsolutePath()+" is locked by a LOK file";
                //logServiceMessageWithTags(LogEventType.FILE_MOVE_START, tags, txt);
                continue;
            }
            
            if(files[i].isDirectory()){
                //txt = "file "+files[i].getAbsolutePath()+" is locked by a LOK file";
                //logServiceMessageWithTags(LogEventType.FILE_MOVE_START, tags, txt);
                continue;
            }
            
            result.add(files[i]);
            txt = "file " + files[i].getAbsolutePath() + " is added to the list for move ";
            logServiceMessageWithTags(LogEventType.GEN_DEBUG, tags, txt);
        }
        txt = "found "+result.size()+" new files for move in "+folder;
        logServiceMessageWithTags(LogEventType.GEN_DEBUG, tags, txt);
        return result;
    }

    public void logNotification(String fileName, String md5) {
        ServiceManager serviceManager = getServiceContainer().getServiceManager();
        DataPowerNotificationLogger notificationLogger = (DataPowerNotificationLogger) serviceManager.findFirstServiceByClassCode(DataPowerNotificationLogger.class, datapowerNotificationLogger);
        notificationLogger.delFPMove(fileName, md5);
        if("true".equalsIgnoreCase(debugModeWithSMSOnDelivery)){
            notificationLogger.addError("Debug: file "+fileName+" was delivered to "+localHostName+" in Sigma");
        }
    }

    public void logNotificationError(String txt) {
        ServiceManager serviceManager = getServiceContainer().getServiceManager();
        DataPowerNotificationLogger notificationLogger = (DataPowerNotificationLogger) serviceManager.findFirstServiceByClassCode(DataPowerNotificationLogger.class, datapowerNotificationLogger);
        if(notificationLogger!=null){
            notificationLogger.addError(txt);
        }
    }

    private static class FileNameComparator implements Comparator  {
        @Override
        public int compare(Object o1, Object o2) {
            File file1 = (File) o1;
            File file2 = (File) o2;
            String name1 = file1.getName();
            String name2 = file2.getName();
            return name1.compareTo(name2);
        }
    }

    public static void main(String[] args) {
        File[] files = new File("C:\\Локальные документы\\projects\\i-Navigator\\code\\sync_server2").listFiles();
        Arrays.sort(files, new FileNameComparator());
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            System.out.println(file.getName());
        }
    }
}
