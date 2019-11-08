package ru.sberbank.syncserver2.service.core;

import org.apache.log4j.pattern.LogEvent;
import ru.sberbank.syncserver2.service.log.DataPowerLogService;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.log.LogMsgComposer;
import ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger;
import ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class FileServiceWithDpLogging extends SingleThreadBackgroundService {
	private static final String dpLoggerServiceCode = "dpLogger";
	private static final Set<LogEventType> loggedTypes = new HashSet<LogEventType>(Arrays.asList(new LogEventType[] {LogEventType.GEN_TRANSFER_FINISH, LogEventType.GEN_DEBUG, LogEventType.GEN_CACHING_START, LogEventType.GEN_CACHING_FINISH}));
	protected DataPowerLogService    dpLogger;

	protected FileServiceWithDpLogging(int waitSeconds) {
		super(waitSeconds);
	}

	@Override
	protected void doStart() {
		ServiceManager serviceManager = getServiceContainer().getServiceManager();
		ServiceContainer container = serviceManager.findServiceByBeanCode(dpLoggerServiceCode);
		if (container != null) {
			dpLogger = (DataPowerLogService) container.getService();
		} else {
			tagLogger.log(getServiceBeanCode(), "DataPowerLogService not found. serviceCode = " + dpLoggerServiceCode);
		}

		super.doStart();
	}

    //public void logToDataPower(LogEventType eventType, String eventDesc, String msg) {
	//	super.log(eventType, eventDesc, msg);
    //}

	private boolean isLogging(LogEventType eventType) {
		return dpLogger != null && loggedTypes.contains(eventType);
	}

    public void logToDataPower(LogEventType eventType, String eventDesc, String msg, Throwable th) {
		if (isLogging(eventType)) {
    		dpLogger.log(LogMsgComposer.composeInternalServiceLogMsg(eventType, serviceBeanCode + " " + eventDesc, msg, th));
    	}
    }

    public void logUserEventToDataPower(LogEventType eventType, String userEmail, String userIp, String eventDesc, String msg, Throwable th) {
		super.logUserEvent(eventType, userEmail, userIp, eventDesc, msg, th);
    }

    public void logUserEventToDataPower(LogEventType eventType, String userEmail, String userIp, String eventDesc, String msg) {
    	logUserEvent(eventType, userEmail, userIp, eventDesc, msg, null);
    }

    public void logObjectEventToDataPower(LogEventType eventType, String objectName, String eventDesc) {
        super.logObjectEvent(eventType, objectName, eventDesc);
        if (isLogging(eventType)) {
            dpLogger.log(LogMsgComposer.composeInternalServiceLogMsg(eventType, eventDesc, objectName));
        }
    }
    
    protected DataPowerNotificationLogger getDatapowerNotificationLogger() {
    	return (DataPowerNotificationLogger) getServiceContainer().getServiceManager().findFirstServiceByClassCode(DataPowerNotificationLogger.class);
    }
    
}
