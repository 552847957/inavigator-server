package ru.sberbank.syncserver2.service.file.transport;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.util.StringUtils;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.StaticFileInfo;
import ru.sberbank.syncserver2.service.file.cache.list.FileLister;
import ru.sberbank.syncserver2.service.file.fragments.FileFragmentsTransportHelper;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Leonid Kozhinskiy
 *
 */
public class SharedSigmaNetworkFileMover extends SigmaNetworkFileMover {
    private String networkSharedFolder;
    private String staticSharedHosts;
    private String sharedHostsListerCode;
    private FileLister sharedHostsLister;

    public SharedSigmaNetworkFileMover() {
    }

    public String getStaticSharedHosts() {
        return staticSharedHosts;
    }

    public void setStaticSharedHosts(String staticSharedHosts) {
        this.staticSharedHosts = staticSharedHosts;
    }

    public String getSharedHostsListerCode() {
        return sharedHostsListerCode;
    }

    public void setSharedHostsListerCode(String sharedHostsListerCode) {
        this.sharedHostsListerCode = sharedHostsListerCode;
    }

    public String getDebugModeWithSMSOnDelivery() {
        return super.getDebugModeWithSMSOnDelivery();
    }

    public void setDebugModeWithSMSOnDelivery(String debugModeWithSMSOnDelivery) {
        super.setDebugModeWithSMSOnDelivery(debugModeWithSMSOnDelivery);
    }

    /**
     * Lists hosts the file should be copied to.
     * It also replaces special value localhost by local host name
     * @param fileName
     * @return
     */
    private String[] getSharedHosts(String fileName){
        //1. Finding host names
        String hostnames = null;
        if(sharedHostsLister!=null){
            StaticFileInfo fileInfo = sharedHostsLister.getFileInfo(fileName);
            hostnames = fileInfo==null ? null:fileInfo.getHostnames();
            if(hostnames!=null){
                hostnames = hostnames.trim().toLowerCase();
            }
        } else if(staticSharedHosts!=null){
            hostnames = staticSharedHosts.trim().toLowerCase();
        }

            //2. Splitting host names and replacing localhost by local host name
        String[] hostArray = hostnames==null || hostnames.length()==0 ? new String[0]:hostnames.split(";");
        for (int i = 0; i < hostArray.length; i++) {
            if("localhost".equalsIgnoreCase(hostArray[i])){
                hostArray[i]=localHostName.toLowerCase();
            }
        }
        return hostArray;
    }

    public String getLocalHostName() {
        return localHostName;
    }

    public void setLocalHostName(String localHostName) {
        this.localHostName = localHostName;
    }

    public String getNetworkSharedFolder() {
        return networkSharedFolder;
    }

    public void setNetworkSharedFolder(String networkSharedFolder) {
        this.networkSharedFolder = networkSharedFolder;
    }

    @Override
    public void doInit() {
        super.doInit();
        FileHelper.createMissingFolders(networkSharedFolder);
        if(sharedHostsListerCode!=null && sharedHostsListerCode.trim().length()>0){
            try {
                ServiceContainer container = ServiceManager.getInstance().findServiceByBeanCode(sharedHostsListerCode);
                sharedHostsLister = (FileLister) container.getService();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * получить исходное имя файла.
     * 1. Если на вход подается имя файла фрагмента - то вернется название файла
     * 2. Если на вход подается имя обычного файла - то вернется оно же
     * @param filename
     * @return
     */
    private String getBaseFileName(String filename) {
    	return FileFragmentsTransportHelper.getSourceFileNameFromFragmentFile(filename);
    }
    
    public void doRun() {
    	// The workflow is:
    	// 1. Files from networkSourceFolder are moved to networkSharedFolder (to prevent overriding)
    	// 2. Listing files in networkSharedFolder and remove locks for all fully copied files
    	// 3. Files from networkSharedFolder are copied to tempFileInProcess until MD5 matches (it may never match for corrupted files)
    	// 4. File tempFileInProcess renamed to tempFileCompleted (both files are operated by SharedSigmaNetworkMover only
    	// 5. File tempFileCompleted renamed to localFile (localFile is monitored by other job that renames it and may be overwritten)
    	
    	//Below is the code
        try {
        	// 1. Files from networkSourceFolder are moved to networkSharedFolder (to prevent overriding by another file coming from Alpha)
            setLastActionComment("Listing files in "+networkSourceFolder+" and moving it to "+networkSharedFolder);
            List files = listWrittenFiles(networkSourceFolder);
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                File networkFile        = (File) iterator.next();
                File sharedNetworkFile  = new File(networkSharedFolder, networkFile.getName());
                if(!sharedNetworkFile.exists()){
                	long srcTime = networkFile.lastModified();
                    networkFile.renameTo(sharedNetworkFile); //no problem in race condition here
                	sharedNetworkFile.setLastModified(srcTime);
                }
            }
            
            //2. Fully copied files from networkSharedFolder should be removed with locks
            files = listWrittenFiles(networkSharedFolder);
            for (Iterator iterator = files.iterator(); iterator.hasNext();) {
                //2.1. Declaring file names
                File networkFile       = (File) iterator.next();
                setLastActionComment("Processing "+networkFile);
                String[] tags         = new String[]{serviceCode  , networkFile.getName(), localTempFolder, localDestFolder};

                //2.2. Check if we should move this file to our server
                //2.2.1. Check if static information defined
                setLastActionComment("Checking hosts for "+networkFile);
                String[] targetHosts = getSharedHosts(getBaseFileName(networkFile.getName()));
                String thisServer = localHostName.toLowerCase();
                if(!ArrayUtils.contains(targetHosts, thisServer)){
                    logStep(tags, networkFile.getName(), "File " + networkFile+" should not be loaded to "+thisServer, DATABASE_AND_TAG);
                    continue; //skip file if current host name is not in the list
                }
                
                //2.3. Creating file lock and check if all was moved
                //     If file was moved to every server then we drop file and locks and start processing next file
                SharedFileLocker moveLock = new SharedFileLocker(networkFile.getAbsolutePath(),targetHosts, thisServer);
                ArrayList errorsFromOtherServers = new ArrayList();
                if(moveLock.wasMovedByAll(errorsFromOtherServers)){
                    logStep(tags, networkFile.getName(), "Deleting lock files for " + networkFile, DATABASE_AND_TAG);
                    FileCopyHelper.reliableDelete(networkFile);
                    moveLock.deleteLockFiles();
                    if(errorsFromOtherServers.size()>0){
                        for (int i = 0; i < errorsFromOtherServers.size(); i++) {
                            String txt = (String) errorsFromOtherServers.get(i);
                            logNotificationError(txt);
                        }
                    }
                    continue;
                }
                
                //2.4. Check if file was already copied to this server
                if(moveLock.wasMovedByThisServer()){
                    //all we could do is to move to local folder if it was fully copied
                    //and remove incompleted temp file (normally should not happen)
                    logStep(tags, networkFile.getName(), "File " + networkFile+" was fully copied before. We ignore it ", DATABASE_AND_TAG);
                    continue;
                }
                
                //2.5. Start moving - repeating the copy 5 times or until success
                String txt = "Start moving "+networkFile+" to local disk";
                setLastActionComment(txt);
                logStep(tags, networkFile.getName(), txt, DATABASE_AND_TAG);
                copyNetworkFileToLocalDisk(networkFile, moveLock, tags);
            }
            
        } catch(RuntimeException e){
            e.printStackTrace();
            throw e;
        } finally {
        }
    }
    
    private void copyNetworkFileToLocalDisk(File networkFile, SharedFileLocker moveLock, String[] tags){
    	//1. Declaring
        File tempFileInProcess = new File(localTempFolder, networkFile.getName()+"."+SharedFileLocker.TEMP_FILE);
        File tempFileCompleted = new File(localTempFolder, networkFile.getName());
        File localFile         = new File(localDestFolder , networkFile.getName());
    	
    	//2. Make a start lock
    	moveLock.start();
    	
    	//3. Copy to temporary file
        int repeatCount = 5;
        String md5 = null;
        // флаг является ли файл фрагментом
        boolean isFileFragment = FileFragmentsTransportHelper.isFileFragment(networkFile);
        // Вычисляем исходное имя файла ( на случай если передается фрагмент файла), если передается не фрагмент, то значение будет равно имени исходного файла
        String sourceNetworkFileName =  getBaseFileName(networkFile.getName());
        for(int i=0; i<repeatCount; i++){
            //3.1. Dropping old
            FileCopyHelper.reliableDelete(tempFileInProcess);
            FileCopyHelper.reliableDelete(tempFileCompleted);

            //3.2. Make a copy. On success MD5 is not null, on failure MD5 is null
            try {
            	long srcTime = networkFile.lastModified();
            	if (!isFileFragment)
            		md5 = FileCopyHelper.copyAndRemoveMD5(networkFile, tempFileInProcess);
            	else
            		FileCopyHelper.reliableCopy(networkFile, tempFileInProcess);
                tempFileInProcess.setLastModified(srcTime);
            } catch (Exception e) {
                logObjectEventToDataPower(LogEventType.ERROR, sourceNetworkFileName, "Error at moving file from " + networkFile+" to " + tempFileInProcess + " : "+e.getMessage());
            }
            if(md5!=null || isFileFragment){ //file was copied successfully and we rename it to permanent location
                logObjectEventToDataPower(LogEventType.GEN_DEBUG, sourceNetworkFileName, "Start renaming from " + tempFileInProcess + " to " + tempFileCompleted);
            	long srcTime = tempFileInProcess.lastModified();
                FileCopyHelper.reliableRename(tempFileInProcess, tempFileCompleted);
                tempFileCompleted.setLastModified(srcTime);
                logObjectEventToDataPower(LogEventType.GEN_DEBUG, sourceNetworkFileName, "Finish renaming from " + tempFileInProcess + " to " + tempFileCompleted);
                break;
            } else {
                int count = repeatCount-i-1;
                String txt = "Failed to move file from " + networkFile + " to " + tempFileCompleted;
                if(count>0){
                    txt = txt + " . The server will try to repeat it "+count+" time(s) in "+(i+1)+" minute(s)";
                } else {
                    txt = txt + " . It was last attempt and server will not retry";
                }
                logObjectEventToDataPower(LogEventType.ERROR, sourceNetworkFileName, txt);
                logNotificationError(txt);
                try {
                    Thread.sleep(60*1000*(i+1));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        //4 After five attempts we finish copy even if file corrupted and copy failed
        if((md5!=null || isFileFragment) && tempFileCompleted.exists()){ //if copy was successfull and tempFileCompleted exists then we move file to localFile
            FileCopyHelper.reliableRename(tempFileCompleted, localFile);
            logObjectEventWithTags(LogEventType.GEN_DEBUG, networkFile.getName(), tags, "Finish moving file from " + networkFile + " to " + localFile);
            logObjectEventToDataPower(LogEventType.GEN_DEBUG, sourceNetworkFileName, "Finish moving file from " + networkFile + " to " + localFile+" at host "+localHostName);
            
            // Уведолмение и доставке файла на сигму отправляет в случае, если данный файл не является фрагментом, т.к для фрагментов событие успешной передачи в сигму можно 
            // вычислить только в точке склейки файла, а она находится в LocalInflater
            if (!isFileFragment) {
            	logNotification(tempFileCompleted.getName(), md5);
            }
            
        } else {
            logObjectEventWithTags(LogEventType.ERROR, networkFile.getName(), tags, "Fail moving file from " + networkFile + " to " + tempFileInProcess);
            logObjectEventToDataPower(LogEventType.ERROR, sourceNetworkFileName, "File " + networkFile + " is corrupted and we failed to deliver it to Sigma");
            
            if (!isFileFragment) {
            	logNotificationError("File " + networkFile + " is corrupted and we failed to deliver it to Sigma");
            }
        }
    	moveLock.finish();
    }
    
    private static final int DATABASE          = 1;
    private static final int DATABASE_AND_TAG  = 3;
    private static final int DATAPOWER         = 4;
    
    
    private void logStep(String[] tags, String fileName, String text, int appenders){
    	if( (appenders & DATABASE_AND_TAG) == DATABASE_AND_TAG){
    		logObjectEventWithTags(LogEventType.GEN_DEBUG, fileName, tags, text);
        } else if( (appenders & DATABASE) == DATABASE){
    		logObjectEvent(LogEventType.GEN_DEBUG, fileName, text);
        }
    	if( (appenders & DATAPOWER)!=0){
            logObjectEventToDataPower(LogEventType.GEN_DEBUG, getBaseFileName(fileName), text);
    	}
    	
    }

    public static void main(String[] args) {
    }
}
