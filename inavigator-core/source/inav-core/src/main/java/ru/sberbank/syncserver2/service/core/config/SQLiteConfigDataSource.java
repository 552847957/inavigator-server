package ru.sberbank.syncserver2.service.core.config;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.sberbank.syncserver2.service.log.TagLogger;
import ru.sberbank.syncserver2.util.FileCopyHelper;

import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class SQLiteConfigDataSource extends DriverManagerDataSource implements ApplicationContextAware {
	private final static String GET_ROOT_FOLDER_SQL = "SELECT * FROM SYNC_CONFIG WHERE PROPERTY_KEY='ROOT_FOLDER'";
	private final Logger logger = Logger.getLogger(SQLiteConfigDataSource.class);
	private final TagLogger tagLogger = TagLogger.getTagLogger(SQLiteConfigDataSource.class);

	private ApplicationContext appContext;

	private String dbFilename;
	private File   workDbFile;
	private String driverClassName;

    private static SQLiteConfigDataSource instance;

	public SQLiteConfigDataSource() {
		super();
	}

    public void init() {
        synchronized (SQLiteConfigDataSource.class){
            if(instance==null){
                doInit();
                instance = this;
            }
        }
    }

	private void doInit() {
		try {
            //1. Copy sqlite to a temporary folder
            String dbName = new File(dbFilename).getName();
            System.out.println("dbName = "+dbName);
            File tempFile = null;
            if(appContext!=null){//call from Spring
                System.out.println("APP CONTEXT CALL");
                Resource r = appContext.getResource("classpath:"+dbFilename);
                if (r == null) {
                    tagLogger.log(dbFilename, "Cannot load database resource");
                }
                dbName = r.getFile().getName();
                tempFile = File.createTempFile(dbFilename, "");
                FileOutputStream tempOS = null;
                try {
                    tempOS = new FileOutputStream(tempFile);
                    System.out.println("CONTEXT COPY FROM RESOURCE TO "+tempFile);
                    IOUtils.copy(r.getInputStream(), tempOS);
                } finally {
                    IOUtils.closeQuietly(tempOS);
                }
            } else {
                System.out.println("SYNCDISPATCHSERVLET CALL");
                File sourceFile = new File(dbFilename);
                tempFile = File.createTempFile(dbName, "");
                System.out.println("SYNCDISPATCHSERVLET COPY FROM "+sourceFile+" TO "+tempFile);
                copy(sourceFile, tempFile);
            }

            //2. Loading a root folder from table SYNC_CONFIG
            String tempUrl = "jdbc:sqlite:" + tempFile.getAbsolutePath();
			tagLogger.log(dbFilename, "Reading file "+ dbFilename +" as "+tempUrl);
			getClass().getClassLoader().loadClass(driverClassName);
			DriverManagerDataSource tempDs = new DriverManagerDataSource(tempUrl, getUsername(), getPassword());
			JdbcTemplate jdbcTemplate = new JdbcTemplate(tempDs);
			String rootFolder = jdbcTemplate.query(GET_ROOT_FOLDER_SQL, new ResultSetExtractor<String>(){

				@Override
				public String extractData(ResultSet rs) throws SQLException,
						DataAccessException {
					return rs.getString(2);
				}});


            //3. Copy a file to permanent folder if necessary
			File permanentConfigFolder = new File(rootFolder, "config.sqlite");
            if (!permanentConfigFolder.exists() && !permanentConfigFolder.mkdirs()) {
                tagLogger.log(dbFilename, "Cannot create work folder for database " + permanentConfigFolder.getAbsolutePath());
            }
			workDbFile = new File(permanentConfigFolder, dbName);
			if (!workDbFile.exists()) {
                tagLogger.log(dbFilename, "Creating work file for database " + workDbFile.getAbsolutePath()+" as copy from "+tempFile);
                copy(tempFile, workDbFile);
                tagLogger.log(dbFilename, "Created file for database " + workDbFile.getAbsolutePath());
			} else {
				tagLogger.log(dbFilename, "Using existing work file for database " + workDbFile.getAbsolutePath());
			}

            //4. Changing url to a final
			String finalUrl = "jdbc:sqlite:" + workDbFile.getAbsolutePath();
			setUrl(finalUrl);
            tagLogger.log(dbFilename, "Set url to " + finalUrl);
		} catch(Exception e) {
			tagLogger.log(new String[] {dbFilename, "ERROR"}, "Cannot load database resource " + e.getMessage());
			logger.error("Cannot load database resource " + e.getMessage(), e);
		}
	}

    private void copy(File src,File dst) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(src);
            os = new FileOutputStream(dst);
            IOUtils.copy(is, os);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    public String getDbFilename() {
        return dbFilename;
    }

    public void setDbFilename(String dbFilename) {
        this.dbFilename = dbFilename;
    }

    @Override
	public void setApplicationContext(ApplicationContext appContext)
			throws BeansException {
		this.appContext = appContext;
	}

	@Override
	public void setDriverClassName(String driverClassName) {
		super.setDriverClassName(driverClassName);
		this.driverClassName = driverClassName;
	}

    @Override
    protected Connection getConnectionFromDriver(Properties props) throws SQLException {
        synchronized (SQLiteConfigDataSource.class){
            if(instance==this){
                return super.getConnectionFromDriver(props);
            } else {
                return instance.getConnectionFromDriver(props);
            }
        }
    }

    @Override
    protected Connection getConnectionFromDriverManager(String url, Properties props) throws SQLException {
        if(instance==this){
            return super.getConnectionFromDriverManager(url, props);
        } else {
            return instance.getConnectionFromDriverManager(url, props);
        }
    }
}
