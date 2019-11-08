package ru.sberbank.syncserver2.service.pub.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="getVersionsRequest")
public class GetVersionsRequest {
	
	private String applicationBundle;

	@XmlElement
	public String getApplicationBundle() {
		return applicationBundle;
	}

	public void setApplicationBundle(String applicationBundle) {
		this.applicationBundle = applicationBundle;
	}
	
	
}
