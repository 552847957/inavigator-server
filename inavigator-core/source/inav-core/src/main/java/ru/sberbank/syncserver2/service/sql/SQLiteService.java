package ru.sberbank.syncserver2.service.sql;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.log.TagLogger;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.DataSourceDescriptor;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.service.sql.query.SwitchingDataSourceDAO;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;

import javax.sql.DataSource;
import java.io.File;
import java.io.FileFilter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * Created by sbt-kozhinsky-lb on 03.03.14.
 */
public class SQLiteService extends SingleThreadBackgroundService {
	TagLogger tagLogger = TagLogger.getTagLogger(SQLiteService.class);
    private SwitchingDataSourceDAO localStorageDAO;
    private String localIncomingFile;
    private String localWorkFolder;
    private String serviceCode;

    public SQLiteService() {
        super(60);
    }

    private static ThreadLocal<DateFormat> dateFormatter = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyMMddHHmmssSSS");
        };
    };

    public String getLocalIncomingFile() {
        return localIncomingFile;
    }

    public void setLocalIncomingFile(String localIncomingFile) {
        this.localIncomingFile = localIncomingFile;
    }

    public String getLocalWorkFolder() {
        return localWorkFolder;
    }

    public void setLocalWorkFolder(String localWorkFolder) {
        this.localWorkFolder = localWorkFolder;
    }

    @Override
    public void doInit() {
        //1. Creating folders
        this.serviceCode = getServiceBeanCode();
        if(localIncomingFile!=null){
            String incomingFolder = new File(localIncomingFile).getParent();
            FileHelper.createMissingFolders(incomingFolder);
        }
        FileHelper.createMissingFolders(localWorkFolder);

        //2. Creating datasource descriptor
        File[] files = new File(localWorkFolder).listFiles();
        if(files!=null && files.length>0){
            resetDataSource(files[0]);
        }
    }

    /**
     * Checks if new database available in inbox
     */
    public void doRun() {
        //1. Check if new file is available, sleep if no files available
        File incomingFile = new File(localIncomingFile);
        if(!incomingFile.exists()){
            tagLogger.log(new String[]{serviceCode,localIncomingFile}, "Nothing to load since file " + localIncomingFile+" was not found");
            return;
        }

        //2. Move file to working folder under new name
        File fileInWorkFolder;
        while(true){
            //2.1. Find unique non-existing file name
            String uniqueSuffix = dateFormatter.get().format(new Date());
            fileInWorkFolder = new File(localWorkFolder, incomingFile.getName()+"."+uniqueSuffix);
            if(fileInWorkFolder.exists()){
               continue;
            }

            //2. Renaming
            String[] tags = new String[]{serviceCode,incomingFile.getName()};
			String text = "Move file from "+incomingFile+" to "+fileInWorkFolder;
			tagLogger.log(tags, text);
            FileCopyHelper.unreliableMove(incomingFile, fileInWorkFolder);
            if(incomingFile.exists()){ //move didn't happenned because target file exists or new incoming file arrived. Continue moving
                continue;
            }
            break;
        }

        //3. Create new datasource descriptor and notify localStorageDao about change
        if (!resetDataSource(fileInWorkFolder)) {
            return;
        }

        //4. Drop all other files
        final String newFilePath = fileInWorkFolder.getAbsolutePath();
        try {
            File workFolderFile = new File(localWorkFolder);
            while (true) {
                //4.1. Listing files different from new file
                File[] files = workFolderFile.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File pathname) {
                        String oldFile = pathname.getAbsolutePath();
                        return !newFilePath.equals(oldFile);
                    }
                });
                if(files==null || files.length==0){
                    return;
                }

                //4.2 Deleting file
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    file.delete();
                }

                //4.3. Sleeping
                Thread.sleep(10*1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doStop() {
        super.doStop();
        if(localStorageDAO != null) {
            localStorageDAO.setDataSourceDescriptor(null);
        }
    }

    private boolean resetDataSource(File fileInWorkFolder) {
        Properties props = composeSQLiteProperties(fileInWorkFolder.getAbsolutePath());
        DataSource dataSource = createDataSource(props);
        if(dataSource!=null){
            DataSourceDescriptor descriptor = new DataSourceDescriptor();
            descriptor.setDataSource(dataSource);
            descriptor.setKey(fileInWorkFolder.getName());
            descriptor.setName(fileInWorkFolder.getName());

            if(localStorageDAO==null){
                localStorageDAO = new SwitchingDataSourceDAO();
            }

            localStorageDAO.setDataSourceDescriptor(descriptor);
        } else { //we fail to open file as SQLite so we drop it
            FileCopyHelper.reliableDelete(fileInWorkFolder);
            return false;
        }
        return true;
    }

    private DataSource createDataSource(Properties properties) {
        DataSource dataSource = null;
        try {
            dataSource = BasicDataSourceFactory.createDataSource(properties);
        } catch (Exception e) {
            tagLogger.log(serviceCode, "Can't create data source with properties " + properties + ": " + e.getMessage());
        }
        return dataSource;
    }

    public DataResponse request(OnlineRequest request) {
        //1. Check if db in initialized state
        if(localStorageDAO==null){
            return null;
        }

        //2. Processing request
        DataResponse response = localStorageDAO.query(request);
        return response;
        /*
	        long startTime = System.nanoTime();
	        log.info("Online request processing start");
	        if (log.isDebugEnabled()) {
	            log.debug(request);
	        }

	        DataResponse response = localStorageDAO.query(request);

	        log.info("Online request has been proceed in " + ((System.nanoTime() - startTime) / 1000000) + " ms");

	        return response;
         */

    }

    private Properties composeSQLiteProperties(String fileName) {
        Properties props = new Properties();
        props.put("driverClassName","org.sqlite.JDBC");
        props.put("url","jdbc:sqlite:"+fileName);
        props.put("initialSize","2");
        props.put("maxActive","20");
        props.put("maxIdle", "5");
        props.put("poolPreparedStatements", "true");
        return props;
    }

    public static void main(String[] args) {
        //1. Creating service
        SQLiteService service = new SQLiteService();
        service.setLocalIncomingFile("C:\\usr\\cache\\dev\\syncserver\\sqliteInbox\\phonebook.sqlite");
        service.setLocalWorkFolder("C:\\usr\\cache\\dev\\syncserver\\sqliteWork\\");
        service.doInit();
        service.doRun();

        //2. Creating requiest
        OnlineRequest or = new OnlineRequest();
        or.setStoredProcedure("SELECT * FROM 'phonebook.sectors'");
        or.setProvider("SQLITE");
        or.setService("finik1");

        //3. Runing onlune request
        System.out.println("REQUEST: "+or);
        DataResponse dr = service.request(or);
        System.out.println("RESULT: " + dr);
    }
}
