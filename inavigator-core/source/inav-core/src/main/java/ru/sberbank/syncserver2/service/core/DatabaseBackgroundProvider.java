package ru.sberbank.syncserver2.service.core;

import java.io.PrintWriter;
import java.sql.Connection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.sql.DataSource;

import org.apache.commons.io.output.StringBuilderWriter;
import ru.sberbank.syncserver2.gui.util.SQLHelper;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.monitor.NotificationService;

/**
 * Created by sbt-kozhinsky-lb on 10.02.15.
 */
public abstract class   DatabaseBackgroundProvider extends SingleThreadBackgroundService {
    private String dataSourceFactoryBean;
    private DataSourceFactory dataSourceFactory;
    private boolean notifyOnFailure;
	private boolean deleteFileIfErrorsInData;
    protected AtomicLong lastDoRunFinish = new AtomicLong();
    protected AtomicBoolean lastConnectionStatus = new AtomicBoolean();

    public DatabaseBackgroundProvider(int waitSeconds) {
        super(waitSeconds);
    }

    public String getDataSourceFactoryBean() {
        return dataSourceFactoryBean;
    }

    public void setDataSourceFactoryBean(String dataSourceFactoryBean) {
        this.dataSourceFactoryBean = dataSourceFactoryBean;
    }

    @Override
    public void doInit() {
        ServiceManager sm = ServiceManager.getInstance();
        ServiceContainer sc = sm.findServiceByBeanCode(dataSourceFactoryBean);
        if(sc==null){
            logError(LogEventType.ERROR,"Can not find DataSourceFactory : wrong bean name "+dataSourceFactoryBean,null);
        } else {
            dataSourceFactory = (DataSourceFactory) sc.getService();
        }
    }

    public AtomicLong getLastDoRunFinish() {
        return lastDoRunFinish;
    }

    public AtomicBoolean getLastConnectionStatus() {
        return lastConnectionStatus;
    }

    public boolean isNotifyOnFailure() {
        return notifyOnFailure;
    }

    public void setNotifyOnFailure(boolean notifyOnFailure) {
        this.notifyOnFailure = notifyOnFailure;
    }

    public void setNotifyOnFailure(String notifyOnFailure) {
    	setNotifyOnFailure(Boolean.parseBoolean(notifyOnFailure));
    }

    public DataSource getDataSource(){
        return dataSourceFactory.getDataSource();
    }

    @Override
    public void doRun() {
        Connection conn = null;
        boolean connectionError = true;
        try {
            //1. Getting datasource factory
            if(dataSourceFactory==null){
                ServiceManager sm = ServiceManager.getInstance();
                ServiceContainer sc = sm.findServiceByBeanCode(dataSourceFactoryBean);
                dataSourceFactory = sc==null ?  null: (DataSourceFactory) sc.getService();
                String txt = "Can not find DataSourceFactory : probably wrong bean name "+dataSourceFactoryBean+" . Skip running doDatabaseRun";
                logError(LogEventType.ERROR,txt,null);
                tagLogger.log(txt);
                return;
            }

            //2. Get datasource and connection
            DataSource dataSource = dataSourceFactory.getDataSource();
            if(dataSource==null){
                String txt = "DataSource has not been connected. Skip running doDatabaseRun, will rerun in "+getWaitSeconds()+" seconds";
                tagLogger.log(txt);
                return;
            }
            conn = dataSource.getConnection();
            lastConnectionStatus.set(true);
            connectionError = false;

            //3. Running
            doDatabaseRun(conn);

            //4. Set last successfull finish
            lastDoRunFinish.set(System.currentTimeMillis());
        } catch (Exception e) {
            e.printStackTrace();
            String txt = "Connection database error: " + e.toString();
        	logError(LogEventType.ERROR, txt, e);
            tagLogger.log(txt);
            try {
                StringBuilder sb = new StringBuilder();
                e.printStackTrace(new PrintWriter(new StringBuilderWriter(sb)));
                tagLogger.log(sb.toString());
            } catch (Exception e1) {
                e1.printStackTrace();
            }
            if(connectionError){
                lastConnectionStatus.set(false);
                notify("Failed to connect "+getServiceBeanCode()+" to "+dataSourceFactory.getUrl());
            } else {
                notify("Unexpected error at calling "+dataSourceFactory.getUrl()+" from "+getServiceBeanCode()+" : "+e.getMessage());
            }
        } finally {
            //3. Return connection to the pool
            SQLHelper.closeConnection(conn);
        }
    }

	public boolean isDeleteFileIfErrorsInData() {
		return deleteFileIfErrorsInData;
	}

	public void setDeleteFileIfErrorsInData(boolean deleteFileIfErrorsInData) {
		this.deleteFileIfErrorsInData = deleteFileIfErrorsInData;
	}

	public void setDeleteFileIfErrorsInData(String deleteFileIfErrorsInData) {
		setDeleteFileIfErrorsInData(Boolean.parseBoolean(deleteFileIfErrorsInData));
	}

    public abstract void doDatabaseRun(Connection connection);

    protected void notify(String text){
        if(notifyOnFailure){
            NotificationService service = (NotificationService) ServiceManager.getInstance().findFirstServiceByClassCode(NotificationService.class);
            service.notify(text);
        }
    }
}
