/**
 *
 */
package ru.sberbank.syncserver2.service.log;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

/**
 * @author Yuliya Solomina
 *
 */
public class LogMsg {
	private String serverEventId;
	private Date eventTime;
	private String userEmail;
	private String clientEventId;
	private String clientDeviceId;
	private LogEventType eventType;
	private String startServerEventId;
	private String eventDesc;
	private String clientIpAddress;
    private String webHostName;
	private String webAppName;
	private String distribServer;
	private String eventInfo;
	private String errorStackTrace;
	private Long id;

    public String getServerEventId() {
		return serverEventId;
	}

	public void setServerEventId(String enetId) {
		this.serverEventId = enetId;
	}

	public Date getEventTime() {
		return eventTime;
	}

	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getClientEventId() {
		return clientEventId;
	}

	public void setClientEventId(String clientEventId) {
		this.clientEventId = clientEventId;
	}

	public String getClientDeviceId() {
		return clientDeviceId;
	}

	public void setClientDeviceId(String clientDeviceId) {
		this.clientDeviceId = clientDeviceId;
	}

	public LogEventType getEventType() {
		return eventType;
	}

	public void setEventType(LogEventType eventType) {
		this.eventType = eventType;
	}

	public String getStartServerEventId() {
		return startServerEventId;
	}

	public void setStartServerEventId(String startServerEventId) {
		this.startServerEventId = startServerEventId;
	}

	public String getEventDesc() {
		return eventDesc;
	}

	public void setEventDesc(String eventDesc) {
		this.eventDesc = eventDesc;
	}

    public String getClientIpAddress() {
        return clientIpAddress;
    }

    public void setClientIpAddress(String clientIpAddress) {
        this.clientIpAddress = clientIpAddress;
    }

    public String getWebHostName() {
        return webHostName;
    }

    public void setWebHostName(String webHostName) {
        this.webHostName = webHostName;
    }

    public String getWebAppName() {
		return webAppName;
	}

	public void setWebAppName(String webAppName) {
		this.webAppName = webAppName;
	}

	public String getDistribServer() {
		return distribServer;
	}

	public void setDistribServer(String distribServer) {
		this.distribServer = distribServer;
	}

	public String getEventInfo() {
		return eventInfo;
	}

	public void setEventInfo(String eventInfo) {
		this.eventInfo = eventInfo;
	}

	public String getErrorStackTrace() {
		return errorStackTrace;
	}

	public void setErrorStackTrace(String errorStackTrace) {
		this.errorStackTrace = errorStackTrace;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
    

	/**
	 * @param eventId
	 * @param eventTime
	 * @param userEmail
	 * @param clientEventId
	 * @param clientDeviceId
	 * @param eventType
	 * @param startEventId
	 * @param eventDesc
	 * @param clientIpAddress
     * @param webHostName
	 * @param webAppName
	 * @param distribServer
	 * @param eventInfo
	 * @param errorStackTrace
	 */
	public LogMsg(String eventId, Date eventTime, String userEmail, String clientEventId, String clientDeviceId,
			LogEventType eventType, String startEventId, String eventDesc, String clientIpAddress, String webHostName,
            String webAppName, String distribServer, String eventInfo, String errorStackTrace) {
		this.serverEventId = eventId;
		this.eventTime = eventTime;
		this.userEmail = userEmail;
		this.clientEventId = clientEventId;
		this.clientDeviceId = clientDeviceId;
		this.eventType = eventType;
		this.startServerEventId = startEventId;
		this.eventDesc = eventDesc;
		this.clientIpAddress = clientIpAddress;
        this.webHostName = webHostName;
		this.webAppName = webAppName;
		this.distribServer = distribServer;
		this.eventInfo = eventInfo;
		this.errorStackTrace = errorStackTrace;
	}

	public LogMsg() {
        this.eventTime = new Date();
	}

    public LogMsg(ResultSet rs) throws SQLException
    {
        this.serverEventId = rs.getString("SERVER_EVENT_ID");
        this.eventTime = rs.getTimestamp("EVENT_TIME");
        this.userEmail = rs.getString("USER_EMAIL");
        this.clientEventId = rs.getString("CLIENT_EVENT_ID");
        this.clientDeviceId = rs.getString("CLIENT_DEVICE_ID");
        this.eventType = LogEventType.valueOf(rs.getString("EVENT_TYPE"));
        this.startServerEventId = rs.getString("START_SERVER_EVENT_ID");
        this.eventDesc = rs.getString("EVENT_DESC");
        this.clientIpAddress = rs.getString("CLIENT_IP_ADDRESS");
        this.webHostName = rs.getString("WEB_HOST_NAME");
        this.webAppName = rs.getString("WEB_APP_NAME");
        this.distribServer = rs.getString("DISTRIB_SERVER");
        this.eventInfo = rs.getString("EVENT_INFO");
        this.id = rs.getLong("EVENT_ID");
    }

    @Override
    public String toString() {
        return "LogMsg{" +
                "serverEventId='" + serverEventId + '\'' +
                ", eventTime=" + eventTime +
                ", userEmail='" + userEmail + '\'' +
                ", clientEventId='" + clientEventId + '\'' +
                ", clientDeviceId='" + clientDeviceId + '\'' +
                ", eventType=" + eventType +
                ", startServerEventId='" + startServerEventId + '\'' +
                ", eventDesc='" + eventDesc + '\'' +
                ", clientIpAddress='" + clientIpAddress + '\'' +
                ", webHostName='" + webHostName + '\'' +
                ", webAppName='" + webAppName + '\'' +
                ", distribServer='" + distribServer + '\'' +
                ", eventInfo='" + eventInfo + '\'' +
                ", errorStackTrace='" + errorStackTrace + '\'' +
                '}';
    }
}
