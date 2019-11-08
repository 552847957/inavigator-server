package ru.sberbank.adminconsole.model.configuration;

import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="configuration")
public class ModuleConfiguration {
	
	private List<Module> modules;

	@XmlElementWrapper(name="modules")
	@XmlElementRef()
	public List<Module> getModules() {
		return modules;
	}

	public void setModules(List<Module> modules) {
		this.modules = modules;
	}
}
