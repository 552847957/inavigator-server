package ru.sberbank.syncserver2.service.file.transport;

import org.apache.commons.lang3.StringUtils;

import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.StaticFileInfo;
import ru.sberbank.syncserver2.service.file.cache.list.DatabaseFileLister;
import ru.sberbank.syncserver2.service.file.cache.list.FileLister;
import ru.sberbank.syncserver2.service.file.fragments.FileFragmentOperationResultTypes;
import ru.sberbank.syncserver2.service.file.fragments.FileFragmentsTransportHelper;
import ru.sberbank.syncserver2.service.generator.ClusterManager;
import ru.sberbank.syncserver2.service.generator.single.data.ActionState;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;
import ru.sberbank.syncserver2.util.MD5Helper;

import java.io.File;
import java.util.*;

import javax.activation.FileTypeMap;

/**
 * @author Sergey Erin
 *
 */
public class AlphaNetworkFileMover extends SingleThreadBackgroundService {
    private String localSourceFolder;
    private String localTempFolder;
    private String networkTempFolder;
    private String networkTargetFolder;
    private String localArchiveFolder;
    private String serviceCode;
    private ThreadLocal databaseNotificationLogger = new ThreadLocal();

    private String staticSharedHosts;
    private String sharedHostsListerCode;
    private FileLister sharedHostsLister;

    private String duplicateFolder;

    public AlphaNetworkFileMover() {
        super(60); //10 seconds to waite between executions
    }

    public String getNetworkTargetFolder() {
        return networkTargetFolder;
    }

    public void setNetworkTargetFolder(String networkTargetFolder) {
        this.networkTargetFolder = networkTargetFolder;
    }

    public String getLocalTempFolder() {
        return localTempFolder;
    }

    public void setLocalTempFolder(String localTempFolder) {
        this.localTempFolder = localTempFolder;
    }

    public String getLocalSourceFolder() {
        return localSourceFolder;
    }

    public void setLocalSourceFolder(String localSourceFolder) {
        this.localSourceFolder = localSourceFolder;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getLocalArchiveFolder() {
        return localArchiveFolder;
    }

    public void setLocalArchiveFolder(String localArchiveFolder) {
        this.localArchiveFolder = localArchiveFolder;
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

    public String getNetworkTempFolder() {
        return networkTempFolder;
    }

    public void setNetworkTempFolder(String networkTempFolder) {
        this.networkTempFolder = networkTempFolder;
    }

    public String getDuplicateFolder() {
        return duplicateFolder;
    }

    public void setDuplicateFolder(String duplicateFolder) {
        this.duplicateFolder = duplicateFolder;
    }

    public void addDuplicateFolder(String duplicateFolder) {
        this.duplicateFolder = duplicateFolder;
    }

    @Override
    public String toString() {
        return "AlphaNetworkFileMover{" +
                "localSourceFolder='" + localSourceFolder + '\'' +
                ", localTempFolder='" + localTempFolder + '\'' +
                ", networkTempFolder='" + networkTempFolder + '\'' +
                ", networkTargetFolder='" + networkTargetFolder + '\'' +
                ", localArchiveFolder='" + localArchiveFolder + '\'' +
                ", serviceCode='" + serviceCode + '\'' +
                ", staticSharedHosts='" + staticSharedHosts + '\'' +
                ", sharedHostsListerCode='" + sharedHostsListerCode + '\'' +
                '}';
    }

    public void doInit() {
        this.serviceCode = getServiceBeanCode();
        FileHelper.createMissingFolders(localTempFolder,localArchiveFolder);
        if (duplicateFolder != null && !"".equals(duplicateFolder)) {
            FileHelper.createMissingFolders(duplicateFolder);
        }
        if(sharedHostsListerCode!=null){
            try {
                ServiceManager serviceManager = ServiceManager.getInstance();
                ServiceContainer container = serviceManager==null ? null:serviceManager.findServiceByBeanCode(sharedHostsListerCode);
                sharedHostsLister = container==null ? null:(FileLister) container.getService();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void doRun() {
        try {
            //1. Listing files and moving one by one
            ClusterManager clusterManager = (ClusterManager) ServiceManager.getInstance().findFirstServiceByClassCode(ClusterManager.class);
            String[] tags = new String[]{serviceCode,localTempFolder, localSourceFolder, networkTempFolder, networkTargetFolder};
            List files = listWrittenFiles(localSourceFolder);
            for (Iterator iterator = files==null ? Collections.EMPTY_LIST.iterator():files.iterator(); iterator.hasNext();) {
                //1.1. Declaring file names
                File sourceFile        = (File) iterator.next();
                File tempFile          = new File(localTempFolder    , sourceFile.getName());
                File networkTempFile   = new File(networkTempFolder  , sourceFile.getName());
                File networkTargetFile = new File(networkTargetFolder, sourceFile.getName());

                moveFileFromTempToDuplicateFolder(sourceFile);

                //1.2. If file is directory then we don't move anything
                if (networkTempFile.isDirectory() || networkTargetFile.isDirectory()) {
                    continue;
                }

                //1.3. Copy files to temp local folder
                logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Start moving file from " + sourceFile + " to " + tempFile + " at " + LOCAL_HOST_NAME);
                boolean success = sourceFile.renameTo(tempFile);
                logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Finish moving file from " + sourceFile + " to " + tempFile+" with "+(success ? "success":"error")+" at "+LOCAL_HOST_NAME);

                //1.4. If cluster in a passive mode then we drop file
                if(clusterManager!=null && !clusterManager.isActive()){
                    logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Removing temp file " + tempFile.getName() + " at passive node "+LOCAL_HOST_NAME);
                    FileCopyHelper.reliableDelete(tempFile);
                    continue;
                }

                //1.5. If file with this name already exists in target network folder, then file is pending in temp folder and will be processed later
                moveFileFromTempToNetwork(tempFile, networkTempFile, networkTargetFile);
            }
            if(shouldInternalTaskStop()){
                return;
            }

            //3. Processing pending files
            File[] pendingFiles = new File(localTempFolder).listFiles();
            if(pendingFiles==null){
                return;
            }
            for (int f=0; f<pendingFiles.length; f++) {
                //2.1. Declaring file names
                File file = pendingFiles[f];
                File tempFile         = new File(localTempFolder     , file.getName());
                File networkTempFile  = new File(networkTempFolder   , file.getName());
                File networkTargetFile = new File(networkTargetFolder, file.getName());

                //2.2. Check if we should stop because service is stopped
                if(shouldInternalTaskStop()){
                    return;
                }

                //2.3. If cluster in a passive mode then we drop temporary file
                if(clusterManager!=null && !clusterManager.isActive()){
                    logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Removing pending file " + tempFile.getName() + " at passive node "+LOCAL_HOST_NAME);
                    FileCopyHelper.reliableDelete(tempFile);
                    continue;
                }

                //2.4. Try to move to network
                moveFileFromTempToNetwork(tempFile, networkTempFile, networkTargetFile);
            }
        } finally {
        }
    }

    public String[] getSharedHosts(String fileName){
        //1. Finding host names
        String hostnames = null;
        if(sharedHostsLister==null && StringUtils.isNotBlank(sharedHostsListerCode)){
            ServiceManager serviceManager = ServiceManager.getInstance();
            try {
                ServiceContainer container = serviceManager.findServiceByBeanCode(sharedHostsListerCode);
                if(container!=null){
                    sharedHostsLister = (FileLister) container.getService();
                //} else {
                    //System.out.println("Failed to find container for "+sharedHostsListerCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(sharedHostsLister!=null){
            StaticFileInfo fileInfo = sharedHostsLister.getFileInfo(fileName);
            hostnames = fileInfo==null ? null:fileInfo.getHostnames();
            if(hostnames!=null){
                hostnames = hostnames.trim().toLowerCase();
            }
        } else if(staticSharedHosts!=null){
            hostnames = staticSharedHosts.trim().toLowerCase();
        }

        //2. Splitting host names
        return hostnames==null || hostnames.length()==0 ? new String[0]:hostnames.split(";");
    }

    class MoveFileFromTempToNetworkException extends Exception {
		private static final long serialVersionUID = 1L;
		public MoveFileFromTempToNetworkException(String message) {
			super(message);
		}
    }

    private boolean moveFileFromTempToDuplicateFolder(File tempFile) {
        boolean result = false;
        String[] tags = new String[]{serviceCode, tempFile.getName()};
        if (duplicateFolder != null && !"".equals(duplicateFolder)) {
            //logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Copy file " + tempFile.getName() + " to duplicate folder " + duplicateFolder);
            tagLogger.log(tags, String.format("Copy file %s to duplicate folder %s", tempFile.getName(), duplicateFolder));
            File dst = new File(duplicateFolder, tempFile.getName());
            String fileMD5 = null;
            try {
                fileMD5 = FileCopyHelper.copyAndAddMD5(tempFile, dst);
                if(fileMD5!=null){
                    //logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Copied file from " + tempFile + " to duplicate folder " + dst + " at " + LOCAL_HOST_NAME + " with success ");
                    tagLogger.log(tags, String.format("Copied file from %s to duplicate folder %s at %s with success", tempFile, dst, LOCAL_HOST_NAME));
                    result = true;
                } else {
                    throw new Exception("Failed to calculate MD5 for " + tempFile.getAbsolutePath() + " Please check the disk at " + LOCAL_HOST_NAME + " , file transported mapped to it and user " + System.getProperty("user.name"));
                }
            } catch (Exception e) {
                String txt = e.getMessage();
                FileCopyHelper.reliableDelete(dst);
                getDatabaseNotificationLogger().addGenStaticFileEvent(tempFile.getName(), ActionState.PHASE_SENDING_TO_SIGMA, ActionState.STATUS_COMPLETED_ERROR);
                logObjectEvent(LogEventType.ERROR, tempFile.getName(), txt);
                logNotificationError(txt);
            }
        }
        return result;
    }
    
    private void moveFileFromTempToNetwork(File tempFile, File networkTempFile, File networkTargetFile) {
        /**
         * The work flow is:
         * 1) File should be copied from tempFile to networkTempFile (networkTempFile is not in OUT folder
         * 2) File should be renamed from networkTempFile to networkTargetFile
         * 3) File transporter should move networkTargetFile to Sigma
         */
    	try {

    		//1. Skip moving file if file with this name is already at file transporter
	        logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Start renaming from " + tempFile + " to " + networkTargetFile+" at "+LOCAL_HOST_NAME);
	        if(networkTempFile.exists()){
	            deleteFileWithLogging(networkTempFile, "Failed to delete " + networkTempFile + " at "+LOCAL_HOST_NAME);
	        }
	        if(networkTargetFile.exists() || networkTempFile.exists()){
	            throw new MoveFileFromTempToNetworkException("Failed to finish renaming from " + tempFile + " to " + networkTempFile +" and then to "+networkTargetFile+" because one of files is already at file transporter at "+LOCAL_HOST_NAME);
	        }
	          
	        //2. Copy and calc md5
	        String fileName = tempFile.getName();
	        String fileMD5 = null;
	        try {
	        	if (!FileFragmentsTransportHelper.isFileFragment(tempFile)) {
		        	fileMD5 = FileCopyHelper.copyAndAddMD5(tempFile, networkTempFile);
		            if(fileMD5!=null){
		                logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Copied file from "+tempFile+" to "+networkTempFile+" at "+LOCAL_HOST_NAME+" with success ");
		            } else {
		                throw new MoveFileFromTempToNetworkException("Failed to calculate MD5 for " + tempFile.getAbsolutePath()+" Please check the disk at "+LOCAL_HOST_NAME+" , file transported mapped to it and user "+System.getProperty("user.name"));
		            }
	        	} else {
	        		// отдельная ветка для фрагментов
	        		FileCopyHelper.reliableCopy(tempFile, networkTempFile);
	        	}
	        } catch (Exception e) {
	            FileCopyHelper.reliableDelete(networkTempFile);
	            throw new MoveFileFromTempToNetworkException("Failed to copy " + tempFile + " to " + networkTempFile + " . Please check permissions at " + LOCAL_HOST_NAME + " for user " + System.getProperty("user.name"));
	        }
	
	        //3. Make future notification on failure for every affected SIGMA host
	        /** Уведомление отправляется только в случае, если это не фрагмент **/
	        if (!FileFragmentsTransportHelper.isFileFragment(tempFile)) {
	        	String[] hosts = getSharedHosts(FileFragmentsTransportHelper.getSourceFileNameFromFragmentFile(fileName));
	        	logNotification(tempFile.getName(), fileMD5, hosts);
	        }
	
	        //4. Renaming to target file
	        if(!networkTempFile.renameTo(networkTargetFile)){
	            FileCopyHelper.reliableDelete(networkTempFile);
	            throw new MoveFileFromTempToNetworkException("Failed to rename " + networkTempFile+" to "+networkTargetFile+" at "+LOCAL_HOST_NAME+" . Please check file transporter and user "+System.getProperty("user.name"));
	        }
	
	        //5. Archiving and dropping temp file
	        logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Finish renaming from " + tempFile + " to " + networkTargetFile+" with success");
	       /** Установка сигнала о том, что передачу в сигму завершена - перенесено в LocalInflater (после успешной сборки файла ищз фрагментов ) */  
	        if(StringUtils.isNotBlank(localArchiveFolder)) {
	            //5.1. Compose archive name
	            File archiveFolder = new File(localArchiveFolder);
	            FileCopyHelper.loggableMkdirs(archiveFolder);

	            if (FileFragmentsTransportHelper.isFileFragment(tempFile)) {
	            	// создаем копию в архиве
		        	FileFragmentOperationResultTypes resultTypes = FileFragmentsTransportHelper.addedFileFragmentToArchive(tempFile, archiveFolder);
		        	// удаляем копию во временной папке 
	                FileCopyHelper.reliableDelete(tempFile);
		        	if (resultTypes.isError())
		        		logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Finish archiving " + tempFile + " to " + new File(archiveFolder,tempFile.getName()) + " with success at "+LOCAL_HOST_NAME);
		            else
			           logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Finish archiving " + tempFile + " to " + new File(archiveFolder,tempFile.getName()) + " with error at "+LOCAL_HOST_NAME);
		        	
		        } else { 
		            File duplicate = new File(archiveFolder, tempFile.getName());
		
		            //5.2. Archiving
		            logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Start archiving " + tempFile + " to "+duplicate+" at "+LOCAL_HOST_NAME);
		            FileCopyHelper.reliableDelete(duplicate);
		            if (!tempFile.renameTo(duplicate)) {
		                FileCopyHelper.reliableDelete(tempFile);
		                logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Finish archiving " + tempFile + " to " + duplicate+" with error at "+LOCAL_HOST_NAME);
		            } else {
		                logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Finish archiving " + tempFile + " to " + duplicate+" with success at "+LOCAL_HOST_NAME);
		            }
		        }
	        } else {
	            FileCopyHelper.reliableDelete(tempFile);
	            logObjectEvent(LogEventType.GEN_DEBUG, tempFile.getName(), "Skip archiving " + tempFile +" at "+LOCAL_HOST_NAME);
	        }

    	} catch (MoveFileFromTempToNetworkException ex) {
    		String txt = ex.getMessage();
            getDatabaseNotificationLogger().addGenStaticFileEvent(tempFile.getName(), ActionState.PHASE_SENDING_TO_SIGMA, ActionState.STATUS_COMPLETED_ERROR);            
            logObjectEvent(LogEventType.ERROR, tempFile.getName(), txt);
            logNotificationError(txt);
            
    	} catch (RuntimeException ex) {
    		String txt = "The process of move File From To Alpha Network failed. (" + ex.getMessage()  + ")";
    		logObjectEvent(LogEventType.ERROR, tempFile.getName(),txt);
    		getDatabaseNotificationLogger().addGenStaticFileEvent(tempFile.getName(), ActionState.PHASE_SENDING_TO_SIGMA, ActionState.STATUS_COMPLETED_ERROR);
    		logNotificationError(txt);

    		// если при работе метода вываливалось неопознанное исключение, то мы фиксируем это, уведомляем и пробрасываем исключение дальше
    		// TODO: Верно ли пробрасывать его дальше и ломать процесс на верхнем уровне, или же достаточно уведомить о возникшей проблеме и забыть? 
    		throw ex;
    	}
    }

    private void deleteFileWithLogging(File file, String txt) {
        try {
            if(file.exists()){
                file.delete();
            }
        } catch (Exception e) {
            logObjectEvent(LogEventType.GEN_DEBUG, file.getName(), txt + " : "+e.getMessage());
            e.printStackTrace();
        }
    }

    public List<File> listWrittenFiles(String folder){
        //1. Declaring
        String[] tags = new String[]{serviceCode,folder};
		tagLogger.log(tags, "Start listing files in "+folder);
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
                String txt = "Check property localSourceFolder . It is equal " + folder + " and folder is not accessible. It is "
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
        String txt = "found "+files.length+" new files in "+folder;
        logServiceMessageWithTags(LogEventType.GEN_DEBUG, tags, txt);
        for(int i=0; i<files.length; i++){
            if(files[i]==null){
                txt = "unexpected error: file is null while listing files at local folder";
                logServiceMessageWithTags(LogEventType.ERROR, tags, txt);
                continue;
            }

            if(files[i].lastModified()>maxLastModified){
                txt = "file "+files[i].getAbsolutePath()+" is too new";
                logServiceMessageWithTags(LogEventType.GEN_DEBUG, tags, txt);
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

    public void logNotification(String fileName, String md5, String[] targetHosts) {
        DatabaseNotificationLogger notificationLogger = getDatabaseNotificationLogger();
        for (int i = 0; i < targetHosts.length; i++) {
            String targetHost = targetHosts[i];
            notificationLogger.addFPMove(fileName, md5, targetHost);
        }
    }

    
    
    protected void logNotificationError(String txt) {
        DatabaseNotificationLogger notificationLogger = getDatabaseNotificationLogger();
        if(notificationLogger!=null){
            notificationLogger.addError(txt);
        }
    }
    
    protected DatabaseNotificationLogger getDatabaseNotificationLogger() {
        DatabaseNotificationLogger dbNotificationLoffer = ((DatabaseNotificationLogger) getServiceContainer().getServiceManager().findFirstServiceByClassCode(DatabaseNotificationLogger.class, databaseNotificationLogger));
        return dbNotificationLoffer;
    	
    }
    
}
