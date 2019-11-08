package ru.sberbank.syncserver2.service.pub.xml;

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="getUserLogMessagesResponse")
public class GetUserLogMessagesResponse extends BaseXmlResponse {

	private List<LogEvent> logEvent;

	
	@XmlElementWrapper(name="logEvents")
	@XmlElementRef()	
	public List<LogEvent> getLogEvent() {
		return logEvent;
	}

	public void setLogEvent(List<LogEvent> logEvent) {
		this.logEvent = logEvent;
	}
	
	
	
}
