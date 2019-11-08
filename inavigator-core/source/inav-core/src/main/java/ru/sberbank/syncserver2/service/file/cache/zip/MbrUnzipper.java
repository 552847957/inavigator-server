package ru.sberbank.syncserver2.service.file.cache.zip;

import ru.sberbank.syncserver2.service.core.ServiceManagerHelper;
import ru.sberbank.syncserver2.service.file.cache.AbstractFileLoader;
import ru.sberbank.syncserver2.service.file.cache.FileCache;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;
import ru.sberbank.syncserver2.service.file.cache.list.DynamicFileLister;
import ru.sberbank.syncserver2.service.file.cache.list.ProxyFileLister;
import ru.sberbank.syncserver2.util.FileCopyHelper;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 17.03.14.
 */
public class MbrUnzipper extends AbstractFileLoader {
    private static final String ZIP_SUBFOLDER       = "zip";
    private static final String SINGLE_SUBFOLDER    = "single";
    private static final String CORRUPTED_SUBFOLDER = "corrupted";

	private File singleTempRoot;
	private File tempZipRoot;
    private File corruptedRoot;

    private ProxyFileLister fileLister = null;

    public DynamicFileLister getFileLister() {
        return null;
    }

    public void setFileLister(DynamicFileLister fileLister) {
    }


    @Override
    public void doInit() {
    	//1. Creating folders
        singleTempRoot = new File(tempFolder, SINGLE_SUBFOLDER);
        singleTempRoot.mkdirs();
        tempZipRoot = new File(tempFolder, ZIP_SUBFOLDER);
        tempZipRoot.mkdirs();
        corruptedRoot = new File(tempFolder,CORRUPTED_SUBFOLDER);
        corruptedRoot.mkdirs();

        //2. Do common init operations
        super.doInit();
    }

    @Override
    /**
     * On loading we should remove all files that are not in status list
     */
    public void doRun() {
        //1. Doing super
        boolean loading = super.loading;
        super.doRun();

        //2. Initializing proxy file lister on loading
        if(loading){
            fileLister = new ProxyFileLister(fileCache);
        }

        //2. Deleting files on loading
        /*
        if(loading){
            //2.1. Check that file lister exists
            if(fileLister==null){
                tagLogger.log("No file lister defined while cleaning old files on load");
            }

            //2. For every app we clean files
            List<String> appList = fileCache.getAppList();
            for (int i = 0; i < appList.size(); i++) {
                String app =  appList.get(i);
                FileInfoList fileList = fileCache.getFileList(app);
                List fileInfos = fileList==null ? Collections.EMPTY_LIST:fileList.getReportStatuses();
                for (int j = 0; j < (fileInfos==null ? 0: fileInfos.size()); j++) {
                    FileInfo fileInfo = (FileInfo) fileInfos.get(j);
                    String id =  fileInfo.getId();
                    FileInfo actualFileInfo = fileLister.getFileInfo(app, id);
                    if(actualFileInfo==null){
                        super.removeFileFromCache(app, id);
                    }
                }

            }
        }*/
    }

    protected void processFile(File changeSetFile) {
        //1. Ignoring directories and all files not ending with zip since ZIP_SUBFOLDER and other are here
        File possibleCorrupted = new File(corruptedRoot, changeSetFile.getName());
        if (changeSetFile.isDirectory() || !changeSetFile.getName().endsWith(".zip"))  {
            tagLogger.log("File not processed:" +  changeSetFile.getAbsolutePath() + " ends with '.zip'");
            FileCopyHelper.localMove(changeSetFile,possibleCorrupted);
            return;
        }

        //2. For every file we update list of files in file lister and extract files to SingleFileLoader's inbox
        MbrZippedFile zmf = null;
        FileInfoList  zmfList = null;
        try {
            //2.1. Extracting file info list
            try {
                zmf = new MbrZippedFile(changeSetFile);
                zmfList = zmf.getFileInfoList();
            } catch (IOException e) {
                tagLogger.log(new String[]{changeSetFile.getName()},"File "+changeSetFile+" is corrupted - please see exception below");
                e.printStackTrace();
                FileCopyHelper.localMove(changeSetFile, possibleCorrupted);
                return;
            } catch (JAXBException e) {
                tagLogger.log(new String[]{changeSetFile.getName()},"File "+changeSetFile+" has a corrupted XML - please see exception below");
                FileCopyHelper.localMove(changeSetFile, possibleCorrupted);
                e.printStackTrace();
                return;
            }

            //2.2. Extracting file by file and add them to cache or remove them
            List<FileInfo> fileInfos = zmfList.getReportStatuses();
            for (int i = 0; i < fileInfos.size(); i++) {
                //2.2.1. Removing file if necessary
                FileInfo alphaFileInfo =  fileInfos.get(i);
                String app = alphaFileInfo.getApp();
                String alphaCaption = alphaFileInfo.getCaption();
                if( alphaFileInfo.isRemoved() ){ //3.1. Removing
                    FileInfo oldFileInfo = fileLister.getFileInfo(app, alphaCaption);
                    if(oldFileInfo!=null){
                        tagLogger.log(new String[]{oldFileInfo.getId(),oldFileInfo.getCaption()},"Removing file " + oldFileInfo.getId() + " for app " + app + " known in Alpha as " + alphaCaption);
                        super.removeFileFromCache(app, oldFileInfo.getId());
                        fileLister.removeFileFromLister(app, alphaCaption);
                    } else {
                        tagLogger.log(new String[]{alphaCaption},"Failed to find file for removing in cache for app "+app+" known in Alpha as "+alphaCaption);
                    }
                    continue;
                }

                //2.2.2. Adding file if necessary
                FileInfo sigmaFileInfo = fileLister.getOrAddFileInfo(alphaFileInfo);
                String newFileId = sigmaFileInfo.getId();
                File tempFile = new File(singleTempRoot, newFileId);
                tagLogger.log(new String[]{alphaCaption, newFileId}, "Start inflating file " + newFileId + " for app " + app + " known in Alpha as " + alphaCaption + " from archive " + changeSetFile.getName());
                if(!zmf.extractTo(alphaFileInfo.getId(), tempFile)){
                    tagLogger.log(new String[]{alphaCaption,newFileId},"Failed to extract file known in Alpha as "+alphaCaption+" to "+tempFile.getAbsolutePath());
                    continue;
                }

                //2.2.3. Splitting and load
                tagLogger.log(new String[]{alphaCaption,newFileId},"Start splitting and loading file "+newFileId+" for app "+app+" known in Alpha as "+alphaCaption+" from archive "+changeSetFile.getName());
                try {
                    boolean finished = splitAndLoad(sigmaFileInfo, tempFile);
                    if(!finished){
                        tagLogger.log("Cancelled while splitting "+changeSetFile);
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //2.2.4. Droping temp file
                FileCopyHelper.reliableDelete(tempFile);
            }
        } finally {
            if(zmf!=null){
                try {
                    zmf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        //3 . Moving changeset file to archive
        tagLogger.log("Start archiving file "+changeSetFile);
        File archiveFile = new File(archiveFolder, changeSetFile.getName());
        FileCopyHelper.reliableMove(changeSetFile, archiveFile);
        tagLogger.log("Archived file "+changeSetFile);
	}

    public static void main(String[] args) throws InterruptedException {
        //1. Configuring
        MbrUnzipper loader = new MbrUnzipper();
        loader.setCacheFolder("C:\\usr\\cache\\dev\\rubricator\\fileCache\\");
        loader.setTempFolder("C:\\usr\\cache\\dev\\rubricator\\loaderTemp\\");
        loader.setInboxFolder("C:\\usr\\cache\\dev\\rubricator\\networkInbox\\");
        loader.setArchiveFolder("C:\\usr\\cache\\dev\\rubricator\\loaderArchive\\");
        DynamicFileLister lister = new DynamicFileLister();
        lister.setStatusFile("C:\\usr\\cache\\dev\\rubricator\\mbrList\\list.bin");
        ServiceManagerHelper.setTagLoggerForUnitTest(lister);

        loader.setFileLister(lister);
        ServiceManagerHelper.setTagLoggerForUnitTest(loader);

        //2. Creating file cache
        FileCache cache = new FileCache();
        cache.addLoader(loader);

        //3. Initializing loader
        System.out.println("Initializing loader");
        lister.doStart();
        loader.doInit();

        //4. Running 1000 times with printing amount of memory every time
        File etalonFolder = new File("C:\\usr\\cache\\dev\\rubricator\\etalon\\");
        File inboxFolder = new File(loader.getInboxFolder());
        for(int i=0; i<4; i++){
            //4.1. Moving all files to inbox
            if(i>0){
                System.out.println("Moving all files to inbox");
                File[] etalonFiles = etalonFolder.listFiles();
                for (int j = 0; j < etalonFiles.length; j++) {
                    File etalonFile = etalonFiles[j];
                    FileCopyHelper.reliableCopy(etalonFile, new File(inboxFolder, etalonFile.getName()));
                }
            }

            //4.2. Loading files to cache
            System.out.println("Make a run");
            loader.doRun();

            //4.3. Printing information about fileInfo
            long usedMemory = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
            System.out.println("USED MEMORY: "+format(usedMemory)+" AFTER "+(i+1)+" EXECUTIONS");
            Thread.sleep(1000);
            System.gc();
            cache.doStop();
            System.gc();
            Thread.sleep(10000);
        }

        //4. Existing
        System.out.println("EXIT");
        System.exit(0);
    }

    private static String format(long memory){
        DecimalFormat format = new DecimalFormat();
        format.setGroupingSize(3);
        return format.format(memory);
    }
}
