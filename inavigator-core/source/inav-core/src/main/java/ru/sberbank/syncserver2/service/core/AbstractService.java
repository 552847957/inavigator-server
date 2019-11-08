package ru.sberbank.syncserver2.service.core;

import org.apache.log4j.Logger;
import ru.sberbank.syncserver2.service.log.DbLogService;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.log.LogMsgComposer;
import ru.sberbank.syncserver2.service.log.TagLogger;
import ru.sberbank.syncserver2.service.core.config.Bean;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by sbt-kozhinsky-lb on 21.02.14.
 */
public abstract class AbstractService {
    private   ServiceContainer serviceContainer;
    protected Logger          logger;
    protected TagLogger       tagLogger;
    protected DbLogService    dbLogger;
    protected String          serviceBeanCode;
    private String            lastActionComment;

    public final static String LOCAL_HOST_NAME;
    static {
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } finally {
        }
        LOCAL_HOST_NAME = hostName;
    }

    protected AbstractService() {
        logger = Logger.getLogger(getClass());
    }

    public void setServiceContainer(ServiceContainer serviceContainer) {
        this.serviceContainer = serviceContainer;
        Bean config = serviceContainer.getConfig();
        tagLogger = TagLogger.getTagLogger(getClass(),logger,config.getCode());
        serviceBeanCode = serviceContainer.getConfig().getCode();
    }

    public ServiceContainer getServiceContainer() {
        return serviceContainer;
    }

    public DbLogService getDbLogger() {
        return dbLogger;
    }

    public void setDbLogger(DbLogService dbLogger) {
        this.dbLogger = dbLogger;
    }

    public TagLogger getTagLogger() {
        return tagLogger;
    }

    protected abstract void doStop();

    protected abstract void waitUntilStopped();

    protected void doStart(){
    	logServiceMessage(LogEventType.SERV_START, "starting service");
        logServiceMessage(LogEventType.SERV_START, "started service");
    }

    protected String getServiceBeanCode(){
    	if (serviceContainer == null) {
    		logger.error("Service container is null");
    		return "";
    	}
        Bean config = serviceContainer.getConfig();
        return config.getCode();
    }

    public synchronized String getLastActionComment() {
        return lastActionComment;
    }

    public synchronized void setLastActionComment(String lastActionComment) {
        this.lastActionComment = lastActionComment;
    }

    public void logServiceMessage(LogEventType eventType, String msg) {
        if (dbLogger != null) {
            dbLogger.log(LogMsgComposer.composeInternalServiceLogMsg(eventType,  serviceBeanCode+": "+msg));
        } else {
            logNoDatabaseLogger();
        }
    }

    public void logServiceMessageWithTags(LogEventType eventType, String[] tags, String msg) {
        tagLogger.log(tags, msg);
        if (dbLogger != null) {
            dbLogger.log(LogMsgComposer.composeInternalServiceLogMsg(eventType,  serviceBeanCode+": "+msg));
        } else {
            logNoDatabaseLogger();
        }
    }

    public void logError(LogEventType eventType, String msg, Throwable th) {
    	if (dbLogger != null) {
    		dbLogger.log(LogMsgComposer.composeInternalServiceLogMsg(eventType, serviceBeanCode + ": " + msg, null, th));
        } else {
            logNoDatabaseLogger();
    	}
    }

    public void logUserEvent(LogEventType eventType, String userEmail, String userIp, String eventDesc, String msg, Throwable th) {
    	if (dbLogger != null) {
    		dbLogger.log(LogMsgComposer.composeUserEventLogMsg(userEmail, eventType, eventDesc, userIp, msg, th));
        } else {
            logNoDatabaseLogger();
    	}
    }

    public void logUserEvent(LogEventType eventType, String userEmail, String userIp, String eventDesc, String msg) {
    	logUserEvent(eventType, userEmail, userIp, eventDesc, msg, null);
    }

    public void logObjectEvent(LogEventType eventType, String objectName, String eventDesc) {
        if (dbLogger != null) {
            dbLogger.log(LogMsgComposer.composeInternalServiceLogMsg(eventType, eventDesc, objectName));
        } else {
            logNoDatabaseLogger();
        }
    }

    public void logObjectEventWithTags(LogEventType eventType, String objectName, String[] tags, String msg) {
        tagLogger.log(tags, msg);
        if (dbLogger != null) {
            dbLogger.log(LogMsgComposer.composeInternalServiceLogMsg(eventType, msg, objectName));
        } else {
            logNoDatabaseLogger();
        }
    }

    private void logNoDatabaseLogger(){
        System.out.println("DATABASE LOGGER IS NULL FOR "+toString());
    }

    @Override
    public String toString() {
        return serviceBeanCode!=null ? serviceBeanCode : getClass().getSimpleName();
    }
}
