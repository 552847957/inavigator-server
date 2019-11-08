package ru.sberbank.syncserver2.service.pub.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="eventInfo")
public class EventInfo {
	
	private String eventSourceId;
	private String eventTime;
	private String timeZone;
	private String userEmail;
	private String eventType;
	private String eventDesc;
	private String ipAddress;
	private String dataServer;
	private String distribServer;
	private String eventInfo;
	private String errorStackTrace;
	private String configurationServer;

	
	public String getConfigurationServer() {
		return configurationServer;
	}
	public void setConfigurationServer(String configurationServer) {
		this.configurationServer = configurationServer;
	}
	public String getEventSourceId() {
		return eventSourceId;
	}
	public void setEventSourceId(String eventSourceId) {
		this.eventSourceId = eventSourceId;
	}
	public String getEventTime() {
		return eventTime;
	}
	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}
	public String getTimeZone() {
		return timeZone;
	}
	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getEventType() {
		return eventType;
	}
	public void setEventType(String eventType) {
		this.eventType = eventType;
	}
	public String getEventDesc() {
		return eventDesc;
	}
	public void setEventDesc(String eventDesc) {
		this.eventDesc = eventDesc;
	}
	public String getIpAddress() {
		return ipAddress;
	}
	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}
	public String getDataServer() {
		return dataServer;
	}
	public void setDataServer(String dataServer) {
		this.dataServer = dataServer;
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
	
	

}
