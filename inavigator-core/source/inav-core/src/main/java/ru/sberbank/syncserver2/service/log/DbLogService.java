/**
 *
 */
package ru.sberbank.syncserver2.service.log;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.ServiceState;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;
import ru.sberbank.syncserver2.service.core.config.SQLiteConfigLoader;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Yuliya Solomina
 */
public class DbLogService extends SingleThreadBackgroundService {
    private static final String DRIVER_CLASSNAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    private volatile JdbcTemplate jdbcTemplate;
    protected ConcurrentLinkedQueue<LogMsg> queue = new ConcurrentLinkedQueue<LogMsg>();
    private ConcurrentLinkedQueue<String> sqlQueue = new ConcurrentLinkedQueue<String>();
    private int batchSize = 50;
    private boolean isLoggingEnabled;

    public DbLogService() {
        super(5); //waiting one second between writings
    }

    protected DbLogService(int waitSeconds) {
        super(waitSeconds);
    }

    @Override
    public void doInit() {
        /*
        Properties props = new Properties();
        props.put("driverClassName", DRIVER_CLASSNAME);
        props.put("url", logDbURL);
        props.put("username", logDbUser);
        props.put("password", logDbPassword);
        tagLogger.log(serviceTag, "Start MSSQLService with props: " + props);
        */

        //1. Creating database logger
        try {
            //1.1. Check if logging is enabled
            ServiceManager serviceManager = super.getServiceContainer().getServiceManager();
            ConfigLoader configLoader = serviceManager.getConfigLoader();
            if (this instanceof SynchronousDbLogService) {
                this.isLoggingEnabled = true;
            } else {
                String enabled = configLoader.getSyncConfigProperty("IS_DB_LOGGING_ENABLED");
                this.isLoggingEnabled = Boolean.parseBoolean(enabled);
            }

            //1.2. For
            if (!(configLoader instanceof SQLiteConfigLoader)) {
                DataSource dataSource = serviceManager.getConfigSource();//new DriverManagerDataSource(logDbURL, props);
                jdbcTemplate = getJdbcTemplate(dataSource);
            }
        } catch (Throwable th) {
            tagLogger.log("Error at starting DbLogService " + th.getMessage());
            throw new RuntimeException(th);
        }

        //2. It is required to set itself as dbLogService for itself so that protected methods start to work
        dbLogger = this;
        //tagLogger.log("DbLogService has been started");
    }

    protected JdbcTemplate getJdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Override
    public void doRun() {
        try {
            //1. Saving logs
            int currSize = queue.size();
            if (currSize > 0) {
                //tagLogger.log("DbLogService has found "+currSize+" records for writing to database");
                List<LogMsg> msgs = new ArrayList<LogMsg>(currSize);
                for (int i = 0; i < currSize; i++) {
                    LogMsg arg = queue.poll();
                    if (arg != null) {
                        msgs.add(arg);
                    }
                }

                int[][] result = jdbcTemplate.batchUpdate("EXEC SP_SYNC_STORE_LOGMSG ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?", msgs, batchSize, new ParameterizedPreparedStatementSetter<LogMsg>() {

                    @Override
                    public void setValues(PreparedStatement stat, LogMsg arg) throws SQLException {
                        stat.setString(1, arg.getServerEventId());
                        stat.setString(2, arg.getUserEmail());
                        stat.setString(3, arg.getClientEventId());
                        stat.setString(4, arg.getClientDeviceId());
                        stat.setString(5, arg.getEventType().toString());
                        stat.setString(6, arg.getStartServerEventId());
                        stat.setString(7, arg.getEventDesc());
                        stat.setString(8, arg.getClientIpAddress());
                        stat.setString(9, arg.getWebHostName());
                        stat.setString(10, arg.getWebAppName());
                        stat.setString(11, arg.getDistribServer());
                        stat.setString(12, arg.getEventInfo());
                        stat.setString(13, arg.getErrorStackTrace());
                    }
                });
                //tagLogger.log("Batch update result " + ArrayUtils.toString(result));
            }

            //3. Running queries
            currSize = sqlQueue.size();
            if (currSize > 0) {
                List<String> sqls = new ArrayList<String>(currSize);
                for (int i = 0; i < currSize; i++) {
                    sqls.add(sqlQueue.poll());
                }
                for (int i = 0; i < sqls.size(); i++) {
                    String sql = sqls.get(i);
                    jdbcTemplate.execute(sql);
                }
            }

        } catch (Throwable th) {
            logger.error("Exception during storing log data in database", th);
        }
    }

/*
    private Properties composeMSSQLProperties() {
        Properties props = new Properties();
        props.put("driverClassName", DRIVER_CLASSNAME);
        props.put("url","jdbc:sqlserver://localhost\\SQLEXPRESS:11433;databaseName=syncserver20");
        props.put("username","sa");
        props.put("password","987654321");
        return props;
    } */

    public void log(LogMsg... logMsgs) {
        if (jdbcTemplate != null && isLoggingEnabled) {
            ServiceContainer container = getServiceContainer();
            if (container.getState() == ServiceState.STARTED) {
                queue.addAll(Arrays.asList(logMsgs));

                //Temporary check for null
                for (int i = 0; i < logMsgs.length; i++) {
                    LogMsg logMsg = logMsgs[i];
                    if (logMsg == null) {
                        try {
                            throw new RuntimeException("Invalid log message: null - PLEASE FIX");
                        } catch (RuntimeException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public List<Long> getGenerationDates(String fileName) {
        final String sql = "SELECT EVENT_TIME FROM SYNC_LOGS WHERE EVENT_TYPE='" + LogEventType.GEN_QUEUED + "' AND EVENT_INFO='" + fileName + "' ORDER BY EVENT_ID DESC";
        try {
            return jdbcTemplate.query(sql, new RowMapper<Long>() {
                @Override
                public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
                    Date date = rs.getTimestamp("EVENT_TIME");
                    if (date != null)
                        return date.getTime();
                    else
                        return null;
                }
            });
        } catch (DataAccessException e) {
            return Collections.emptyList();
        }
    }
    
    /*
    public List<LogMsg> list(String where){
        final String sql = "SELECT * FROM SYNC_LOGS "+where+" ORDER BY EVENT_ID";
        return jdbcTemplate.query(sql, new RowMapper<LogMsg>() {
            @Override
            public LogMsg mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new LogMsg(rs);
            }
        });
    }
    */

    public void dropOldLogs(String filename, int storageTimes) {
        String dropOldLogsSQL = "IF (SELECT COUNT(*) FROM SYNC_LOGS WHERE EVENT_INFO='" + filename + "' AND EVENT_TYPE='" + LogEventType.OTHER + "' AND EVENT_DESC LIKE '%audit%')>" + storageTimes
                + " DELETE FROM SYNC_LOGS WHERE EVENT_ID< (SELECT TOP 1 * FROM (SELECT TOP " + storageTimes + " EVENT_ID FROM SYNC_LOGS WHERE EVENT_INFO='" + filename + "'"
                + " AND EVENT_TYPE='" + LogEventType.OTHER + "' AND EVENT_DESC LIKE '%audit%' ORDER BY EVENT_ID DESC) AS fromtop ORDER BY EVENT_ID ASC) AND EVENT_INFO='" + filename + "'";
        executeSQL(dropOldLogsSQL);
    }

    public void executeSQL(String sql) {
        jdbcTemplate.execute(sql);
    }

    public void scheduleSQL(String sql) {
        sqlQueue.add(sql);
    }
}
