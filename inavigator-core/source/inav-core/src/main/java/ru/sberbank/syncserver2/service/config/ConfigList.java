package ru.sberbank.syncserver2.service.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 05.03.14.
 */
@XmlRootElement(name = "config",namespace = "")
public class ConfigList {
    private List<ConfigProperty> properties;

    public ConfigList() {
    }

    public ConfigList(List<ConfigProperty> properties) {
        this.properties = properties;
    }

    @XmlElement(name = "property")
    public List<ConfigProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<ConfigProperty> properties) {
        this.properties = properties;
    }

	@Override
	public String toString() {
		return "ConfigList {properties=" + properties + "}";
	}


}
