package ru.sberbank.syncserver2.service.rubricator;

import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.log.LogEventType;

import javax.sql.DataSource;
import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class DatabaseFileReader extends SingleThreadBackgroundService {
    private static final String DRIVER_CLASSNAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

	public static final String INSERTED = "i";// - добавление файла
    public static final String UPDATED = "u"; // - обновление файла
    public static final String DELETED = "d"; //- удаление файла

    public static final String DATE_PARAMETER = "dt";
    public static final String COL_RESOURCE_ID = "res";
    public static final String COL_OPERATION = "operation";
    public static final String COL_DATA = "data";
    public static final String COL_CHANGE_DATE = "change_date";

    public static final String UPDATE_DATE_FILE = "update_time.txt";

    private final String folderDelimiter = "/";
    protected String folder;

    private String timeFormat = "yyyy-MM-dd HH:mm:ss ";
    private UpdateProcessor updateProcessor;
    private DataSource dataSource;
    private NamedParameterJdbcTemplate jdbcTemplate;

    private long lastUpdateRun = 0;

    /**
     * Service properties block
     */
    /**
     * Example query
     * select b.resource_id res, 'u' operation, a.id , r.file_name, a.change_date, r.data
  	   from report a ,report_resource b,resource r
	   where a.change_date>=:dt and a.report_Type in
		(select id from report_type where ident in ('mis_report', 'doc')) and a.id= b.report_id and b.resource_id = r.id
	union
        select c.resourceId res, c.operation, c.resourceId id, filename_ file_name, c.date_ change_date, null data
           from changelog c, (select resourceId, max(date_) date_
                                from changelog where date_>=:dt group by resourceId) l
           where c.date_=l.date_ and c.resourceId=l.resourceId and c.operation='d';
     *
     */
    private String loadUpdatesSQL;
    private String dbUrl;
	private String dbUser;
	private String dbPassword;
	private String dbDriverClassname;
	private String workFolder;
	private String resultFolder;

    public DatabaseFileReader() {
		super(60);
	}

	@Override
	public void doInit() {
		updateProcessor = new UpdateProcessor();
		logServiceMessage(LogEventType.SERV_START, "getting connection to database. The service name is ");
		try {
			dataSource = new DriverManagerDataSource(DRIVER_CLASSNAME, dbUrl, dbUser, dbPassword);
			jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		} catch (Exception e) {
			logServiceMessage(LogEventType.ERROR, "Cannot get connection to database " + e.getMessage()+" The service name is ");
		}

	}

	@Override
	public void doRun() {
		try {
            if (lastUpdateRun + getWaitSeconds() * 1000 < System.currentTimeMillis()) {
                if (updateFiles(null)) {
                    lastUpdateRun = System.currentTimeMillis();
                }
            }
        } catch (Exception ex) {
            logger.error("Error ", ex);
        }
	}

	public boolean updateFiles(Date date) {
		if (date == null) {
			date = getLastDate();
		}
		logger.error("Update files from database started, start date is "
				+ date);
		Map<String, Date> map = new HashMap<String, Date>();
		map.put(DATE_PARAMETER, date);
		final AtomicInteger i = new AtomicInteger(0);
		final AtomicReference<Date> dateAtomicReference = new AtomicReference<Date>(
				date);
		String sql = getLoadUpdatesSQL();
		jdbcTemplate.query(sql, map, new RowCallbackHandler() {
			@Override
			public void processRow(ResultSet resultSet) throws SQLException {
				i.incrementAndGet();
				Date date1 = processChange(resultSet);
				if (date1.after(dateAtomicReference.get())) {
					dateAtomicReference.set(date1);
				}
			}
		});
		if (!date.equals(dateAtomicReference.get())) {
			Date updateDate = new Date(
					dateAtomicReference.get().getTime() + 1000);
			updateLastDate(updateDate);
			logger.debug("Update files from database finished, " + i.get()
					+ " files updated, new update timestamp is " + updateDate);
		} else {
			logger.debug("Update files from database finished, no files updated");
		}
		return true;

	}

	private Date processChange(ResultSet resultSet) throws SQLException {
        try {
            Object o = resultSet.getObject(COL_RESOURCE_ID);
            String fileName = o.toString();
            if (fileName != null && fileName.trim().length() > 0) {
                String oper = resultSet.getString(COL_OPERATION);
                if (DELETED.equals(oper)) {
                    updateProcessor.processDelete(getFilePath(fileName));
                } else {
                    try {
                        updateProcessor.processUpdate(getFilePath(fileName), resultSet);
                    } catch (IOException e) {
                        logger.error("Error updating file " + fileName);
                    }
                }
            }
            Timestamp date = resultSet.getTimestamp(COL_CHANGE_DATE);
            return date;
        } catch (Throwable t) {
            logger.error("Error processing record", t);
        }
        return null;
    }

    private Date getLastDate() {
        File file = new File(getServiceFilePath(UPDATE_DATE_FILE));
        if (file.exists()) {
            try {
                BufferedReader fs = new BufferedReader(new FileReader(file));
                Date parse = new SimpleDateFormat(timeFormat).parse(fs.readLine());
                fs.close();
                return parse;
            } catch (Exception e) {
                logger.error("Error reading data from file", e);
            }
        }
        return new Date(0);
    }

    private void updateLastDate(Date date) {
        logger.debug("updateLastDate to " + date);
        File file = new File(getServiceFilePath(UPDATE_DATE_FILE));
        try {
        	if(!file.getParentFile().exists()) {
        		file.getParentFile().mkdirs();
        	}
            file.createNewFile();
            BufferedWriter fos = new BufferedWriter(new FileWriter(file));
            fos.write(new SimpleDateFormat(timeFormat).format(date));
            fos.flush();
            fos.close();
        } catch (IOException e) {
            logger.error("Unable update time", e);
        }
        logger.debug("updateLastDate to " + date + " DONE");
    }

    public String getServiceFilePath(String fileName) {
        return getWorkFolder() + folderDelimiter + fileName;
    }

    public String getFilePath(String fileName) {
        return getResultFolder() + folderDelimiter + fileName;
    }

	@Override
	protected void doStop() {
		super.doStop();

		dataSource = null;
		jdbcTemplate = null;
	}

	public String getLoadUpdatesSQL() {
		return loadUpdatesSQL;
	}

	public void setLoadUpdatesSQL(String loadUpdatesSQL) {
		this.loadUpdatesSQL = loadUpdatesSQL;
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

	public String getWorkFolder() {
		return workFolder;
	}

	public void setWorkFolder(String workFolder) {
		this.workFolder = workFolder;
	}

	public String getResultFolder() {
		return resultFolder;
	}

	public void setResultFolder(String resultFolder) {
		this.resultFolder = resultFolder;
	}

	public static void main(String[] args) {
		DatabaseFileReader service = new DatabaseFileReader();
		service.setDbDriverClassname("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		service.setDbPassword("987654321");
		service.setDbUser("sa");
		service.setDbUrl("jdbc:sqlserver://localhost\\SQLEXPRESS:11433;databaseName=mis_rubricator");
		service.setWorkFolder("C:\\usr\\test\\rubricator\\work");
		service.setResultFolder("C:\\usr\\test\\rubricator\\result");
		service.setLoadUpdatesSQL("select b.resource_id res, 'u' operation, a.id , r.file_name, a.change_date, r.data"
				+ "\n from report a ,report_resource b,resource r"
				+ "\n where a.change_date>=:dt and a.report_Type in"
				+ "\n (select id from report_type where ident in ('mis_report', 'doc')) and a.id= b.report_id and b.resource_id = r.id"
				+ "\n union"
				+ "\n select c.resourceId res, c.operation, c.resourceId id, filename_ file_name, c.date_ change_date, null data"
				+ "\n from changelog c, (select resourceId, max(date_) date_"
				+ "\n from changelog where date_>=:dt group by resourceId) l"
				+ "\n where c.date_=l.date_ and c.resourceId=l.resourceId and c.operation='d'");

		service.doStart();
		try {
			Thread.sleep(1000 * 60 * 60);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
