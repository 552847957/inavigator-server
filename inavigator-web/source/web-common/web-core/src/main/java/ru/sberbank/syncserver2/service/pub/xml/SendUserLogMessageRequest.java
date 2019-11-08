package ru.sberbank.syncserver2.service.pub.xml;

import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name = "sendUserLogMessageRequest")
public class SendUserLogMessageRequest {
	
	private LogEvent logEvent;
	
	private String userEmail;

	public LogEvent getLogEvent() {
		return logEvent;
	}

	public void setLogEvent(LogEvent logEvent) {
		this.logEvent = logEvent;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	
	
}
