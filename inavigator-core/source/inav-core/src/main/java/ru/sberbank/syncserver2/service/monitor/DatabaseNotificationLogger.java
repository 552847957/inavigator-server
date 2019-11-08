package ru.sberbank.syncserver2.service.monitor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.Folder;
import ru.sberbank.syncserver2.service.generator.single.data.ActionState;

import javax.sql.DataSource;

/**
 * Created by Admin on 20.05.14.
 */
public class DatabaseNotificationLogger extends AbstractService {
    private JdbcTemplate jdbcTemplate;
    private String databaseName;

    public DatabaseNotificationLogger() {
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    @Override
    protected void doStop() {
    }

    @Override
    protected void waitUntilStopped() {
    }

    public void addGeneration(String fileName){
        try {
            String sql = "exec "+databaseName+"..SP_ADD_FILE_GEN_NOTIFICATION ?,?,?";
            //logSQL(sql, LOCAL_HOST_NAME, getWebAppName(), fileName);
            jdbcTemplate.update(sql, LOCAL_HOST_NAME, getWebAppName(), fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void delGeneration(String fileName){
        try {
            String sql = "exec "+databaseName+"..SP_DEL_FILE_GEN_NOTIFICATION ?,?,?";
            //logSQL(sql, LOCAL_HOST_NAME, getWebAppName(), fileName);
            jdbcTemplate.update(sql,LOCAL_HOST_NAME,getWebAppName(),fileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addFPMove(String fileName, String fileMd5, String targetHost){
        try {
            String sql = "exec "+databaseName+"..SP_ADD_FILE_MOV_NOTIFICATION ?,?,?,?,?";
            //logSQL(sql,LOCAL_HOST_NAME,getWebAppName(),fileName,fileMd5,targetHost);
            jdbcTemplate.update(sql,LOCAL_HOST_NAME,getWebAppName(),fileName,fileMd5,targetHost);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ping(){
        if(jdbcTemplate!=null){
            try {
                String sql = "exec "+databaseName+"..SP_PING ?,?";
                //logSQL(sql, LOCAL_HOST_NAME, getWebAppName());
                jdbcTemplate.queryForMap(sql, LOCAL_HOST_NAME, getWebAppName());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            tagLogger.log("Skip ping before initialization");
        }
    }

    public void addError(String text){
        try {
            String sql = "exec "+databaseName+"..SP_ADD_FILE_NOTIFICATION ?,?,null,null,null,?,?";
            //logSQL(sql, LOCAL_HOST_NAME, getWebAppName(), text, String.valueOf(new java.sql.Timestamp(System.currentTimeMillis())));
            jdbcTemplate.update(sql, LOCAL_HOST_NAME, getWebAppName(), text, new java.sql.Timestamp(System.currentTimeMillis()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    
    public void addGenStaticFileEvent(String fileName,String phaseCode,String statusCode) {
    	addGenStaticFileEvent(fileName, phaseCode, statusCode, "", "");
    }

    /**
     * Добавление нового события-сигнала при генерации задачи
     * Метод вызывается из ALPHA генератора
     * @param fileName
     * @param phaseCode
     * @param statusCode
     * @param comment
     * @param host
     */
    public void addGenStaticFileEvent(String fileName,String phaseCode,String statusCode,String comment,String host) {
    	try {
    		String sql = "exec SP_SYNC_STATIC_FILES_GEN_ADD_STATE ?,?,?,?,?,?";
    		jdbcTemplate.update(sql,fileName,phaseCode,statusCode,comment,host,AbstractService.LOCAL_HOST_NAME);
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
    }



    /**
     * Обнуление всех сообщений при перед генерацией файла
     * Метод вызывается из ALPHA генератора
     * @param fileName
     * @param phaseCode
     * @param statusCode
     * @param comment
     * @param host
     */
    public void startGenStaticFileEvent(String fileName) {
    	try {
    		String sql = "exec SP_SYNC_STATIC_FILES_GEN_START ?";
    		jdbcTemplate.update(sql,fileName);
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
    }
    
    @Override
    public void doStart() {
        //System.out.println("DatabaseNotificationLogger.doStart - start");
        super.doStart();
        ServiceManager serviceManager = getServiceContainer().getServiceManager();
        try {
            DataSource dataSource = serviceManager.getConfigSource();//new DriverManagerDataSource(logDbURL, props);
            jdbcTemplate = new JdbcTemplate(dataSource);
        } catch (Throwable th) {
            th.printStackTrace();
            tagLogger.log("Error at starting Notification Service " + th.getMessage());
            throw new RuntimeException(th);
        }
        //System.out.println("DatabaseNotificationLogger.doStart - end");
    }

    private String getWebAppName(){
        return "Monitor"; //should be always monitor until we use separate JVMs in separate apps at Websphere Application Server ND
    }

    /*
    private static void logSQL(String...args){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if(i>1){
                sb.append(",");
            } else if(i==1){
                sb.append(" ");
            }
            if(i==0){
                sb.append(args[i]);
            } else {
                sb.append("'").append(args[i]).append("'");
            }
        }
        System.out.println(sb);
    }

    public static void main(String[] args) {
        logSQL("PROC","arg1","arg2","arg3");
    }
    */
}
