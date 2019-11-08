package ru.sberbank.adminconsole.model.configuration;

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="configuration")
public class ApplicationConfiguration {
	
	private List<Application> applications;

	@XmlElementWrapper(name="applications")
	@XmlElementRef()
	public List<Application> getApplications() {
		return applications;
	}

	public void setApplications(List<Application> applications) {
		this.applications = applications;
	}
}
