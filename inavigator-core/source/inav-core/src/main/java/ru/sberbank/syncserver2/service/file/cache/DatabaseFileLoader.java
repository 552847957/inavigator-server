package ru.sberbank.syncserver2.service.file.cache;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import ru.sberbank.syncserver2.service.core.DataSourceFactory;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.file.cache.data.ChunkInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.FormatHelper;

/**
 * Created by sbt-kozhinsky-lb on 19.02.15.
 */
public class DatabaseFileLoader extends SingleThreadBackgroundService implements FileLoader {
    private String dataSourceFactoryBean;
    private DataSourceFactory dataSourceFactory;
    private FileCache fileCache;
    private String sqlLoadAll      = "SELECT 'quotes' as APP, DOCUMENT_ID as ID, LAST_MODIFIED, DOCUMENT_CONTENT as CONTENT FROM DOCUMENTS ORDER BY SORT_ORDER";
    private String sqlLoadModified = "SELECT 'quotes' as APP, DOCUMENT_ID as ID, LAST_MODIFIED, DOCUMENT_CONTENT as CONTENT FROM DOCUMENTS WHERE LAST_MODIFIED > ? ORDER BY SORT_ORDER";
    private String sqlLoadById = "SELECT 'quotes' as APP, DOCUMENT_ID as ID, LAST_MODIFIED, DOCUMENT_CONTENT as CONTENT FROM DOCUMENTS WHERE ID = ?";
    private Timestamp maxLastModified;
    private RowMapper<DatabaseInfo> rowMapper = new RowMapper<DatabaseInfo>() {
        @Override
        public DatabaseInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
            String app = rs.getString("APP");
            String id = rs.getString("ID");
            Timestamp lastModified = rs.getTimestamp("LAST_MODIFIED");
            byte[] content = rs.getBytes("CONTENT");
            return new DatabaseInfo(app,id,lastModified, content);
        }
    };

    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate;

    public DatabaseFileLoader() {
        super(15);
    }

    public String getSqlLoadAll() {
        return sqlLoadAll;
    }

    public void setSqlLoadAll(String sqlLoadAll) {
        this.sqlLoadAll = sqlLoadAll;
    }

    public String getSqlLoadModified() {
        return sqlLoadModified;
    }

    public void setSqlLoadModified(String sqlLoadModified) {
        this.sqlLoadModified = sqlLoadModified;
    }

    public String getDataSourceFactoryBean() {
        return dataSourceFactoryBean;
    }

    public void setDataSourceFactoryBean(String dataSourceFactoryBean) {
        this.dataSourceFactoryBean = dataSourceFactoryBean;
    }

    @Override
    public void doInit() {
        //1. Getting datasource factory
        ServiceManager sm = ServiceManager.getInstance();
        ServiceContainer sc = sm.findServiceByBeanCode(dataSourceFactoryBean);
        if(sc==null){
            logError(LogEventType.ERROR,"Can not find DataSourceFactory : wrong bean name "+dataSourceFactoryBean,null);
            return;
        } else {
            dataSourceFactory = (DataSourceFactory) sc.getService();
        }

        //2. Getting file cache
        fileCache = (FileCache) sm.findFirstServiceByClassCode(FileCache.class);
        if(fileCache==null){
            logError(LogEventType.ERROR,"Can not find FileCache by class name",null);
            return;
        }

        //2. Getting datasource
        loadFilesFromDatabaseAndUpdateCache();
    }

    private synchronized void loadFilesFromDatabaseAndUpdateCache() {
        //1. Initializing datasource and jdbcTemplate
        //System.out.println("START loadFilesFromDatabaseAndUpdateCache from "+maxLastModified);
        logger.info("Loading documents from the database starting with maxLastModified = "+maxLastModified);
        if(dataSource==null){
            //System.out.println("Step 1");
            dataSource = dataSourceFactory.getDataSource();
            if(dataSource==null){
                tagLogger.log("DataSource is not ready, skip loading documents");
                return;
            }
            //System.out.println("Step 2");
        }
        if(jdbcTemplate==null){
            //System.out.println("Step 3");
            jdbcTemplate = new JdbcTemplate(dataSource);
        }
        if(fileCache==null){
            logError(LogEventType.ERROR,"Can not find FileCache by class name",null);
            return;
        }
        //System.out.println("Step 4");

        //2. Load list of changed files
        //System.out.println("START loadFilesFromDatabaseAndUpdateCache  - loadFilesFrom");
        //System.out.println("Step 5");
        List<DatabaseInfo> databaseInfos = null;
        if(maxLastModified==null){
            //System.out.println("Running "+sqlLoadAll);
            databaseInfos = jdbcTemplate.query(sqlLoadAll, rowMapper );
        } else {
            //System.out.println("Step 6");
            //System.out.println("Running "+sqlLoadModified+" with "+maxLastModified);
            databaseInfos = jdbcTemplate.query(sqlLoadModified, new Object[]{maxLastModified}, new int[]{Types.TIMESTAMP}, rowMapper );
        }

        //3. For all changed files we load content and put it to cache
        //System.out.println("Step 7");
        boolean noMissing = true;
        for (int i = 0; i < databaseInfos.size(); i++) {
            //3.1. Extract values
            //System.out.println("Step 8");
            DatabaseInfo databaseInfo =  databaseInfos.get(i);
            String app = databaseInfo.getApp();
            String id = databaseInfo.getId();
            Timestamp lastModified = databaseInfo.getLastModified();
            byte[] content = databaseInfo.getContent();
            if(content==null){
                noMissing = false;
                continue;
            }
            if(lastModified!=null && noMissing){
                Timestamp copyLastModified = new Timestamp(lastModified.getTime());
                updateMaxLastModified(copyLastModified);
            }

            //3.2. Prepare fileInfo and chunkInfo
            //System.out.println("Step 9");
            FileCacheHelper.SingleChunkInfo singleChunkInfo = FileCacheHelper.composeSingleChunkInfo(app, id, content, this);
            FileInfo fileInfo = singleChunkInfo.getFileInfo();
            ChunkInfo chunkInfo = singleChunkInfo.getChunkInfo();
            String lastModifiedAsString = FormatHelper.formatDateTimeWithTimeZone(lastModified);
            fileInfo.setLastModified(lastModifiedAsString);

            //3.3. Update cache
            //System.out.println("Step 10");
            fileCache.putFile(fileInfo, chunkInfo);
            tagLogger.log("Loaded file with id = "+id);
        }
        //System.out.println("FINISH loadFilesFromDatabaseAndUpdateCache");
    }

    private synchronized void updateMaxLastModified(Timestamp lastModified) {
        if (lastModified == null) {
            return;
        }
        if (maxLastModified == null) {
            maxLastModified = lastModified;
            return;
        }
        long max = maxLastModified.getTime();
        long last = lastModified.getTime();
        if (last > max) {
            maxLastModified = lastModified;
        }
    }

    @Override
    public void doRun() {
        loadFilesFromDatabaseAndUpdateCache();
    }

    @Override
    public byte[] loadSingleChunk(String folder, int chunkIndex) {
        List<DatabaseInfo> databaseInfos = jdbcTemplate.query(sqlLoadById, new Object[]{folder}, rowMapper);
        DatabaseInfo databaseInfo =  databaseInfos.get(0);
        if (databaseInfo != null) {
            byte[] content = databaseInfo.getContent();
            if (content != null) {
                return content;
            }
        }
        return new byte[0];
    }

    private static class DatabaseInfo {
        private String app;
        private String id;
        private     Timestamp lastModified;
        private byte[] content;

        private DatabaseInfo(String app, String id, Timestamp lastModified, byte[] content) {
            this.app = app;
            this.id = id;
            this.lastModified = lastModified;
            this.content = content;
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public Timestamp getLastModified() {
            return lastModified;
        }

        public void setLastModified(Timestamp lastModified) {
            this.lastModified = lastModified;
        }

        public byte[] getContent() {
            return content;
        }

        public void setContent(byte[] content) {
            this.content = content;
        }
    }
}
