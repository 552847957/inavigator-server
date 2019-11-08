package ru.sberbank.syncserver2.service.config;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Created by sbt-kozhinsky-lb on 05.03.14.
 */
@XmlType(name="property")
public class ConfigProperty {
    private String name;
    private String value;

    public ConfigProperty() {
    }

    public ConfigProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

	@Override
	public String toString() {
		return "ConfigProperty {name=" + name + ", value=" + value + "}";
	}


}
