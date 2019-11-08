package ru.sberbank.syncserver2.service.log;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.output.ByteArrayOutputStream;

/**
 * Created by sbt-kozhinsky-lb on 05.03.14.
 */
public class LogMsgComposer {
    private static final String startServerPrefix = new SimpleDateFormat("yyyyMMdd_hhmmss_sss_").format(new Date());
    private static AtomicLong counter = new AtomicLong(0);
    private static String webHostName = "localhost";
    private static String webAppName  = "AppNameIsNotFilled";

    static {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            webHostName = inetAddress.getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void setWebAppName(String appName){
        LogMsgComposer.webAppName = appName;
    }

    public static synchronized String getWebAppName() {
        return webAppName;
    }

    public static LogMsg composeStartLogMsg(String userEmail, String clientEventId, String clientDeviceId,LogEventType eventType, String eventDesc, String clientIpAddress, String eventInfo){
        String eventId = getNextEventId();
        Date eventTime = new Date();
        return new LogMsg(eventId,eventTime,userEmail,clientEventId,clientDeviceId, eventType,null,eventDesc,clientIpAddress,webHostName,getWebAppName(),null, eventInfo,null);
    }

    public static LogMsg composeUserEventLogMsg(String userEmail, LogEventType eventType, String eventDesc, String clientIpAddress, String eventInfo, Throwable th){
        String eventId = getNextEventId();
        Date eventTime = new Date();
        String errorStackTrace = logThrowableStackTrace(th);
        return new LogMsg(eventId,eventTime,userEmail,null,null, eventType,null,eventDesc,clientIpAddress,webHostName,getWebAppName(),null, eventInfo, errorStackTrace);
    }

    public static LogMsg composeUserEventLogMsg(String userEmail, LogEventType eventType, String eventDesc, String clientIpAddress, String eventInfo){
    	return composeUserEventLogMsg(userEmail, eventType, eventDesc, clientIpAddress, eventInfo, null);
    }

	/**
	 * @return
	 */
	private static String getNextEventId() {
		return startServerPrefix+String.valueOf(counter.addAndGet(1));
	}

    /**
     * @param eventDesc
     * @return
     */
    public static LogMsg composeInternalServiceLogMsg(LogEventType eventType, String eventDesc) {
        String eventId = getNextEventId();
        Date eventTime = new Date();
        return new LogMsg(eventId, eventTime, null, null, null, eventType, null, eventDesc, null, webHostName, getWebAppName(), null, null, null);
    }

    /**
     * @param eventDesc
     * @param eventInfo
     * @return
     */
    public static LogMsg composeInternalServiceLogMsg(LogEventType eventType, String eventDesc, String eventInfo) {
        String eventId = getNextEventId();
        Date eventTime = new Date();
        return new LogMsg(eventId, eventTime, null, null, null, eventType, null, eventDesc, null, webHostName, getWebAppName(), null, eventInfo, null);
    }

	/**
	 * @param eventDesc
	 * @param eventInfo
	 * @return
	 */
	public static LogMsg composeInternalServiceLogMsg(LogEventType eventType, String eventDesc, String eventInfo, Throwable th) {
		String eventId = getNextEventId();
        Date eventTime = new Date();
        String errorStackTrace = logThrowableStackTrace(th);
		return new LogMsg(eventId, eventTime, null, null, null, eventType, null, eventDesc, null, webHostName, getWebAppName(), null, eventInfo, errorStackTrace);
	}

	/**
	 * @param th
	 * @return
	 */
	public static String logThrowableStackTrace(Throwable th) {
		String errorStackTrace = null;
        if (th != null) {
        	ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
        	th.printStackTrace(ps);
        	ps.close();
        	errorStackTrace = baos.toString();
        }
		return errorStackTrace;
	}


}
