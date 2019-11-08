package ru.sberbank.syncserver2.service.core;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.ResourceHelper;

import javax.naming.NamingException;
import javax.sql.DataSource;

import java.util.Properties;

/**
 * Created by sbt-kozhinsky-lb on 18.02.15.
 */
public class DataSourceFactory extends SingleThreadBackgroundService {
    private String driver;
    private String url;
    private String user;
    private String password;
    private String jndi;
    private DataSource dataSource = null;

    protected DataSourceFactory() {
        super(3600);
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getJndi() {
        return jndi;
    }

    public void setJndi(String jndi) {
        this.jndi = jndi;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void doInit() {
        if(jndi!=null){
            try {
                this.dataSource = ResourceHelper.lookup(jndi);
                logger.info("Start "+getClass().getName()+" finished with successfull connection to "+jndi);
            } catch (NamingException e) {
                e.printStackTrace();
                logError(LogEventType.SERV_START, "exception during starting "+serviceBeanCode, e);
            } finally {
                logServiceMessage(LogEventType.SERV_START, "started service");
            }
            return;
        } else {
            //1. Prepare properties
            Properties props = new Properties();
            props.put("driverClassName", driver);
            props.put("url", url);
            props.put("username", user);
            props.put("password", password);
            logger.info("Start "+getClass().getName()+" with properties: " + props);

            //2. Connecting again
            try {
                this.dataSource = new DriverManagerDataSource(driver, url, user, password);
            } catch (Throwable th) {
                logger.error("Start MSSQLService ERROR ", th);
                logError(LogEventType.SERV_START, "exception during starting "+serviceBeanCode, th);
                throw new RuntimeException(th);
            }

            //3. Indicate successfull connection
            logger.info("Start "+getClass().getName()+" finished with successfull connection to "+url);
            logServiceMessage(LogEventType.SERV_START, "started service");
        }
    }

    @Override
    public void doRun() {
    }

}
