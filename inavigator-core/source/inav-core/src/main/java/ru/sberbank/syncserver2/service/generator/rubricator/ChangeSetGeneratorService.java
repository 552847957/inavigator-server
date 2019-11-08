package ru.sberbank.syncserver2.service.generator.rubricator;

import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.sberbank.syncserver2.gui.util.SQLHelper;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;
import ru.sberbank.syncserver2.service.file.transport.AlphaNetworkFileMover;
import ru.sberbank.syncserver2.service.generator.ClusterManager;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;

import javax.sql.DataSource;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * @author Leonid Kozhinskiy
 *
 */
public class ChangeSetGeneratorService extends SingleThreadBackgroundService {
    private String localFolder;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String dbDriverClassname = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private String loadUpdatesSQL;
    //private String duplicateFolder;

    private FileInfoList generatedList;
    private DataSource dataSource;
    private NamedParameterJdbcTemplate jdbcTemplate;

    private static final String GENERATED_FOLDER      = "generated";
    private static final String TEMP_CHANGESET_FOLDER = "tempChangeSet";
    private static final String TEMP_CONTENT_FOLDER   = "tempContent";

    protected static final String INSERTED = "i";// - добавление файла
    protected static final String UPDATED = "u"; // - обновление файла
    protected static final String DELETED = "d"; //- удаление файла

    protected static final String COL_RESOURCE_ID           = "res";
    protected static final String COL_CHANGELOG_RESOURCE_ID = "id";
    protected static final String COL_OPERATION             = "operation";
    protected static final String COL_DATA                  = "data";
    protected static final String COL_CHANGE_DATE           = "change_date";
    protected static final String COL_AUTOINCREMENT_ID      = "autoincrement_id";

    public static final String RUBRICATOR_APP = "rubricator";

    public ChangeSetGeneratorService() {
        super(60);
    }

    public String getLocalFolder() {
        return localFolder;
    }

    public void setLocalFolder(String localFolder) {
        this.localFolder = localFolder;
    }

    public String getDbUrl() {
        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public String getDbPassword() {
        return dbPassword;
    }

    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public String getDbDriverClassname() {
        return dbDriverClassname;
    }

    public void setDbDriverClassname(String dbDriverClassname) {
        this.dbDriverClassname = dbDriverClassname;
    }

    public String getLoadUpdatesSQL() {
        return loadUpdatesSQL;
    }

    public void setLoadUpdatesSQL(String loadUpdatesSQL) {
        this.loadUpdatesSQL = loadUpdatesSQL;
    }

    @Override
    public void doInit() {
        //1. Creating folders
        File tempChangeSetFolder = new File(localFolder, TEMP_CHANGESET_FOLDER);
        File tempContentFolder   = new File(localFolder, TEMP_CONTENT_FOLDER);
        File localDestFolder     = new File(localFolder, GENERATED_FOLDER);
        FileHelper.createMissingFolders(tempChangeSetFolder.getAbsolutePath(), tempContentFolder.getAbsolutePath(), localDestFolder.getAbsolutePath());

        //2. Cleaning content from previous starts in temporary folder
        FileCopyHelper.reliableDeleteFolderContent(tempChangeSetFolder);
        FileCopyHelper.reliableDeleteFolderContent(tempContentFolder);

        //3. Loading generated list
        File statusFile = new File(localFolder,"list.xml");
        generatedList = FileInfoList.loadFrom(statusFile);
        if(generatedList==null){
            generatedList = new FileInfoList();
        }

        //4. Getting connection
        logServiceMessage(LogEventType.SERV_START, "getting connection to database");
        try {
            dataSource = createNewDataSource();
            jdbcTemplate = createNewNamedParameterJdbcTemplate(dataSource);
        } catch (Exception e) {
            logServiceMessage(LogEventType.ERROR, "Failed to connect to database " + e.getMessage());
        }

    }

    protected DataSource createNewDataSource() {
        return new DriverManagerDataSource(dbDriverClassname, dbUrl, dbUser, dbPassword);
    }

    protected NamedParameterJdbcTemplate createNewNamedParameterJdbcTemplate(DataSource theDataSource) {
        return new NamedParameterJdbcTemplate(theDataSource);
    }

    @Override
    public void doRun() {

        //0. Check if AlphaNetworkFileMover on this node has duplicateFolder property, and if has, not consider cluster
        //AlphaNetworkFileMover moverService = (AlphaNetworkFileMover) ServiceManager.getInstance().findFirstServiceByClassCode(AlphaNetworkFileMover.class);
        //if (!(moverService != null && moverService.getDuplicateFolder() != null && !"".equals(moverService.getDuplicateFolder()))) {

            //1. Check if node is active
            ClusterManager clusterManager = (ClusterManager) ServiceManager.getInstance().findFirstServiceByClassCode(ClusterManager.class);
            if(clusterManager!=null && !clusterManager.isActive()){
                return;
            }

        //}

        //2. Generating
        int addedRowCount = 0;
        boolean dublicateFound = false;
        LastDateAndId dai = null;
        do {
            //2.1. If there are more then 5 changesets in generated folder then we do nothing and
            File folder = new File(localFolder, GENERATED_FOLDER);
            File[] pendingGeneratedFiles = folder.listFiles();
            final int MAX_PENDING_COUNT = 5;
            if(pendingGeneratedFiles!=null && pendingGeneratedFiles.length>=MAX_PENDING_COUNT){
                tagLogger.log("More than "+MAX_PENDING_COUNT+" generated changeset files in folder "+folder.getAbsolutePath()+" stop further generation");
                return;
            }

            //2.2. For every five changes we generate a changeset file and move it to generator
            //2.2.1. Declaring and calc parameters
            if(dai==null){
                dai = new LastDateAndId();
                dai.load(new File(localFolder));
            }
            tagLogger.log("Started extracting files from the database, " + dai);

            //2.2.2. Main Processing
            //2.2.2.1. Initialising composer
            File tempFolder = new File(localFolder,TEMP_CHANGESET_FOLDER);
            ChangeSetComposer composer = new ChangeSetComposer(tempFolder);
            try {
                composer.start();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }

            //2.2.2.2. Fetching file one by one - IT IS REQUIRED BECAUSE GARBAGE COLLECTOR
            addedRowCount = 0;
            final int MAX_CHANGES_IN_CHANGESET = 25;
            Connection conn = null;
            dublicateFound = false;
            try {
                for(int s=0; s<MAX_CHANGES_IN_CHANGESET;s++){
                    tagLogger.log("Generating: s="+s);
                    PreparedStatement ps = null;
                    ResultSet rs = null;
                    try {
                        //2.2.1. Prepare
                        if(conn==null){
                            conn = dataSource.getConnection();
                        }

                        tagLogger.log("Prepare statement");
                        ps = conn.prepareCall(loadUpdatesSQL);
                        ps.setInt(1, dai.lastId);
                        rs = ps.executeQuery();

                        //2.2.2. Fetching row by row
                        if(rs.next()){
                            //2.2.2.1. Check if we should cancel processing
                            tagLogger.log("Found next change");
                            if (shouldInternalTaskStop()) {
                                tagLogger.log("Processing was stopped");
                                return;
                            }

                            //2.2.2.2. Adding changed content
                            dai = processChange(rs, composer, dai);
                            addedRowCount++;
                        } else {
                            break;
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        composer.finishOnCancel(" SQL exception");
                        tagLogger.log("Found exception: "+e.getMessage());
                    } finally {
                        SQLHelper.closeResultSet(rs);
                        SQLHelper.closeStatement(ps);
                        if(shouldInternalTaskStop()){
                            composer.finishOnCancel(" stopping of web server");
                            break;
                        }
                        System.gc();
                        try {
                            Thread.sleep(5*1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Runtime runtime = Runtime.getRuntime();
                        String memoryMsg = "ChangeSetGeneratorService memory statistics: Total="+(runtime.totalMemory()/1024/1024)+"M Free="+runtime.freeMemory()/1024/1024+"M";
                        tagLogger.log(memoryMsg);
                    }
                }
            } catch (DublicateException de){
                tagLogger.log("Found dublicate exception: "+de.getMessage());
                dublicateFound = true;
            } catch (Throwable t){
                tagLogger.log("Found unexpected exception: "+t.getMessage());
            } finally {
                SQLHelper.closeConnection(conn);
            }

            //2.2.2.3. Finishing composing
            if(addedRowCount>0){
                try {
                    composer.finish();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
                moveTempFileToGenerated(composer, dai);
                tagLogger.log("Update files from database finished with "+addedRowCount+" files, finish date is " + dai);
            } else {
                composer.finishOnCancel(" no new files");
                tagLogger.log("Update files from database finished without changes, finish date is " + dai);
            }
        } while(addedRowCount>0 || dublicateFound);
    }

    protected void moveTempFileToGenerated(ChangeSetComposer composer, LastDateAndId dai) {
        //1. Moving file
        File tempFile = composer.getTempFile();

        File generatedFile = new File(new File(localFolder, GENERATED_FOLDER), tempFile.getName());
        FileCopyHelper.reliableMove(tempFile, generatedFile);

        //2. Extract changes and merge it with main FileInfoList
        FileInfoList changes = composer.getStatusList();
        generatedList.update(changes);
        generatedList.saveTo(new File(localFolder, "list.xml"));

        //3. Writing down the generated date
        dai.save(new File(localFolder));
    }

    private LastDateAndId processChange(ResultSet resultSet, ChangeSetComposer composer, LastDateAndId dai) throws SQLException {
        //System.out.println("processChange: inputDai = "+dai);
        try {
            String fileName = resultSet.getString(COL_RESOURCE_ID);
            String operation = resultSet.getString(COL_OPERATION);
            Timestamp changeDate = resultSet.getTimestamp(COL_CHANGE_DATE);
            int id = resultSet.getInt(COL_CHANGELOG_RESOURCE_ID);
            int autoincrementId = resultSet.getInt(COL_AUTOINCREMENT_ID);
            //System.out.println("Start processing file "+fileName+" for changeset : changeDate = "+changeDate+", id = "+id);
            if (fileName != null && fileName.trim().length() > 0) {
                FileInfo fileInfo = new FileInfo(RUBRICATOR_APP,String.valueOf(autoincrementId),null,"/"+fileName,null);
                fileInfo.setCaption("/"+fileName);
                fileInfo.setName(fileName);
                fileInfo.setLastModified(String.valueOf(changeDate));
                if (DELETED.equals(operation)) {
                    fileInfo.setLastModified(FileInfo.REMOVED_FLAG);
                    composer.addFileInfo(fileInfo);
                } else {
                    File tempFile = new File(new File(localFolder,TEMP_CONTENT_FOLDER), fileName);
                    writeBlobTofile(tempFile, resultSet);
                    //if(composer.containsSameCaption(fileInfo)){
                    //    throw new DublicateException();
                    //}
                    composer.addContent(fileInfo, tempFile);
                    FileCopyHelper.reliableDelete(tempFile);
                }
            }
            dai = new LastDateAndId(autoincrementId);
            //System.out.println("processChange: outputDai = "+dai);
        } catch(DublicateException de){
            throw de;
        } catch (Throwable t) {
            t.printStackTrace();
            tagLogger.log("Error while adding file to changeset: "+t);
        }
        return dai;
    }

    private void writeBlobTofile(File f, ResultSet resultSet) throws SQLException, IOException {
        Blob blob = resultSet.getBlob(COL_DATA);
        OutputStream os = null;
        InputStream is = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(f));
            is = new BufferedInputStream(blob.getBinaryStream());
            IOUtils.copyLarge(is, os, new byte[1024]);
            os.flush();
        } finally {
            FileHelper.close(is);
            FileHelper.close(os);
        }
    }

    //protected void logEvent(String changeSetFileName, String event){
    //    System.out.printf("rubricator"+ changeSetFileName + " : " + event);
    //}

    protected static class LastDateAndId {
        private int  lastId;
        private static final String FILE_NAME = "lastChangeLogId.txt";
        private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss ";


        public LastDateAndId() {
            this.lastId = -1;
        }

        public LastDateAndId(int id) {
            this.lastId = id;
        }

        public void save(File localFolder){
            //1. Composing properties
            Properties props = new Properties();
            props.put("last.changelog.autoincrement.id"  ,String.valueOf(lastId));

            //2. Saving
            FileWriter writer = null;
            File file = new File(localFolder, FILE_NAME);
            try {
                if(!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                file.createNewFile();
                writer = new FileWriter(file);
                String comment = "#Last date is "+new SimpleDateFormat(TIME_FORMAT).format(new Date());
                props.store(writer, comment);
                writer.flush();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(writer);
            }
        }

        public void load(File localFolder){
            File file = new File(localFolder, FILE_NAME);
            if (file.exists()) {
                Properties props = new Properties();
                FileReader reader = null;
                try {
                    reader = new FileReader(file);
                    props.load(reader);
                    if (props.getProperty("last.changelog.autoincrement.id") == null) {
                        return;
                    }
                    lastId = Integer.parseInt(props.getProperty("last.changelog.autoincrement.id"));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    IOUtils.closeQuietly(reader);
                }
            }
        }

        @Override
        public String toString() {
            return "lastId="+lastId;
        }
    }

    private static class DublicateException extends RuntimeException {
    }

    public static void main(String[] args) {
        //WRITING
        LastDateAndId l = new LastDateAndId();
        l.lastId = 10;
        System.out.println("BEFORE WRITING: "+l);
        l.save(new File("C:/Локальные документы"));
        l.lastId = 30;

        System.out.println("BETWEEN READING and WRITING : "+l);

        //READING
        l = new LastDateAndId();
        l.load(new File("C:/Локальные документы"));
        System.out.println("AFTER READING : "+l);
    }
}
