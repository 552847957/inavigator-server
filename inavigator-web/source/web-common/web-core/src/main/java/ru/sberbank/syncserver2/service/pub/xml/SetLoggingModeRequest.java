package ru.sberbank.syncserver2.service.pub.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "setLoggingModeRequest")
public class SetLoggingModeRequest {
	
	private String userEmail;
	
	private boolean mode;

	@XmlElement
	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	@XmlElement
	public boolean isMode() {
		return mode;
	}

	public void setMode(boolean mode) {
		this.mode = mode;
	}
	
	
	
}
