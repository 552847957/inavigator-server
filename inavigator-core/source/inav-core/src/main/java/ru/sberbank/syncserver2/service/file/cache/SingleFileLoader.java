package ru.sberbank.syncserver2.service.file.cache;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.springframework.beans.factory.BeanInitializationException;
import ru.sberbank.syncserver2.service.core.ServiceManagerHelper;
import ru.sberbank.syncserver2.service.core.config.StaticFileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.list.FileLister;
import ru.sberbank.syncserver2.service.file.transport.LocalInflater;
import ru.sberbank.syncserver2.service.generator.single.data.ActionState;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;
import ru.sberbank.syncserver2.util.MD5Helper;

/**
 * Created by sbt-kozhinsky-lb on 21.02.14.
 */
public class SingleFileLoader extends AbstractFileLoader {
    private FileLister fileLister = null;

    public FileLister getFileLister() {
        return fileLister;
    }

    public void setFileLister(FileLister fileLister) {
        this.fileLister = fileLister;
    }


    @Override
    public void doInit() {
        //1. Check file lister instance
        if (fileLister == null) {
            throw new BeanInitializationException("fileLister cannot be null");
        }

        //2. Initializing
        super.doInit();
    }

    @Override
	protected void processFile(File file) {
    	//1. Check if file is known
        tagLogger.log(new String[]{serviceCode,file.getName()},"Split "+file.getName());
        StaticFileInfo staticFileInfo = fileLister.getFileInfo(file.getName());
        if(staticFileInfo==null){
            tagLogger.log(new String[]{serviceCode,file.getName()}, "Skip unknown file " + file.getName());
        }

        //2. If file is known then we split it
        if(staticFileInfo!=null){
            FileInfo fileInfo = new FileInfo(staticFileInfo.getApp(), staticFileInfo.getFileId(), staticFileInfo.getFileName());
            try {
                tagLogger.log(new String[]{serviceCode,file.getName()},"Start splitting "+file);
                fileInfo.setFileLength(file.length());
                boolean finished = splitAndLoad(fileInfo,file);
                if(!finished){
                    tagLogger.log("Cancelled while splitting "+file);
                    return;
                }
            } catch (Exception e) {
            	getDatapowerNotificationLogger().addGenStaticFileEvent(fileInfo.getName(), ActionState.STATUS_COMPLETED_ERROR, getLocalhostName());
                e.printStackTrace();
            } finally {
                tagLogger.log(new String[]{serviceCode,file.getName()},"Finish splitting "+file);
            }
        }

        //3. Anyway file shoud be moved to archive
        //System.out.println("Start archiving");
        File archiveFile = new File(archiveFolder, file.getName());
        tagLogger.log(new String[]{serviceCode,file.getName()},"Start moving "+file+" to "+archiveFile);
        FileCopyHelper.reliableMove(file, archiveFile);
        tagLogger.log(new String[]{serviceCode,file.getName()}, "Moved " + file + " to " + archiveFile);
	}

    /*
        Code for standalone testing below instead of previous function
    private Map<String, StaticFileInfo> loadStaticFileMap() {
        staticFileMap = new HashMap<String, StaticFileInfo>();
        StaticFileInfo info = new StaticFileInfo("default","phonebook_db","phonebook.sqlite");
        staticFileMap.put(info.getFileName(),info);
    }

    *
    private FileInfo getFileInfoByFileName(String name) {
        //1. TODO - we should change reading of it to the database
        String[][] ALL_FILES = new String[][]{
                {"default","RESERVED_01","reserved_01.sqlite"},
                {"default","RESERVED_02","reserved_02.sqlite"},
                {"iNavigator","mis_navigator_kpi","MIS_NAVIGATOR_KPI.sqlite"},
                {"iNavigator","mis_navigator_kpi_w","MIS_NAVIGATOR_KPI_W.sqlite"},
                {"iPassport","mis_prognoz_sb_data","MIS_PROGNOZ_SB_DATA.sqlite"},
                {"iup","mis_iup_kpi","mis_iup_kpi.sqlite"},
                {"default","mis_competitors","competitors.sqlite"},
                {"default","mis_balance","balance.sqlite"},
                {"default","rtce-minute","rtce-quotes-minute.xml"},
                {"default","rtce-day","rtce-quotes-day.xml"},
                {"default","phonebook","default-phonebook.xml"},
                {"default","phonebook_db","phonebook.sqlite"},
                {"default","phonebook_vip_db","phonebook_vip.sqlite"},
                {"MISMobile","RETAIL_CRED","retail_cred.sqlite"},
                {"MISMobile","RETAIL_CRED_PORTFOLIO","retail_cred_bag.sqlite"},
                {"MISMobile","RETAIL_CRED_ATM","mis_retail_atm_01.db"},
                {"MISMobile","DASHBOARD_PPR_MOBILE","DASHBOARD_PPR_MOBILE.sqlite"},
                {"MISMobile","ASSETS","assets.sqlite"},
                {"MISMobile","LIABILITIES","liabilities.sqlite"}
        };

        //2. Finding app by  file
        for(int i=0; i<ALL_FILES.length; i++){
            String fileName = ALL_FILES[i][2];
            if(fileName.equalsIgnoreCase(name)){
                String app      = ALL_FILES[i][0];
                String reportId = ALL_FILES[i][1];
                FileInfo info = new FileInfo(app, reportId, fileName);
                return info;
            }
        }
        return null;
    }*/

    public static void main(String[] args) throws InterruptedException {
        //1. Configuring
        SingleFileLoader loader = new SingleFileLoader();
        loader.setCacheFolder("C:\\usr\\cache\\dev\\syncserver\\fileCache\\");
        loader.setTempFolder("C:\\usr\\cache\\dev\\syncserver\\loaderTemp\\");
        loader.setInboxFolder("C:\\usr\\cache\\dev\\syncserver\\networkInbox\\");
        loader.setArchiveFolder("C:\\usr\\cache\\dev\\syncserver\\loaderArchive\\");
        loader.setFileLister(new ExampleFileLister());
        ServiceManagerHelper.setTagLoggerForUnitTest(loader);

        //2. Creating file cache
        FileCache cache = new FileCache();
        cache.addLoader(loader);

        //3. Inlating deflated file
        File src = new File("C:\\usr\\cache\\dev\\syncserver\\MIS_PROGNOZ_SB_DATA.sqlite");
        File dst = new File("C:\\usr\\cache\\dev\\syncserver\\MIS_PROGNOZ_SB_DATA2.sqlite");
        FileHelper.inflate(src, dst);

        //4. Initializing loader
        loader.doInit();

        //5. Running 1000 times with printing amount of memory every time
        for(int i=0; i<1000; i++){
            //File dst3 = new File("C:\\usr\\cache\\dev\\syncserver\\MIS_PROGNOZ_SB_DATA3.sqlite");
            //File dst4 = new File(loader.getInboxFolder(),dst3.getName());
            //FileCopyHelper.reliableCopy(dst, dst3);
            //FileCopyHelper.simpleMove(dst3,dst4);
            loader.setLoading(true);
            loader.doRun();
            long usedMemory = Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory();
            System.out.println("USED MEMORY: "+usedMemory+" AFTER "+(i+1)+" EXECUTIONS");
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

    private static class ExampleFileLister extends FileLister {
        final StaticFileInfo info = new StaticFileInfo("default","MIS_PROGNOZ_SB_DATA3","MIS_PROGNOZ_SB_DATA3.sqlite");

        @Override
        protected Map<String, StaticFileInfo> doLoadAll() {
            return Collections.singletonMap(info.getFileName(), info);
        }
    }
}

/*
        //1. Configuring
        SingleFileLoader loader = new SingleFileLoader();
        loader.setCacheFolder("C:\\usr\\cache\\dev\\syncserver\\fileCache\\");
        loader.setTempFolder("C:\\usr\\cache\\dev\\syncserver\\loaderTemp\\");
        loader.setInboxFolder("C:\\usr\\cache\\dev\\syncserver\\networkInbox\\");
        loader.setArchiveFolder("C:\\usr\\cache\\dev\\syncserver\\loaderArchive\\");

        //2. Inidializing
        loader.doInit();

        //3. Running first
        loader.doRun();
        loader.doRun();

        //5. Trying to inflate first chunk
        byte[] firstChunk=loader.loadSingleChunk("phonebook_db",0);
        System.out.println("Chunk length = "+firstChunk.length);

        //6. Trying to inflate
        byte[] buffer = new byte[100*1000*1000];;
        try {
            Inflater inflater = new Inflater();
            inflater.setInput(firstChunk);
            int unziplen = inflater.inflate(buffer);
            System.out.println("unziplen = "+unziplen);
            inflater.finished();
            System.out.println("MD5 = "+MD5Helper.getCheckSumAsString(firstChunk));
        } catch (DataFormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("FINISHED INFLATING");
 */
