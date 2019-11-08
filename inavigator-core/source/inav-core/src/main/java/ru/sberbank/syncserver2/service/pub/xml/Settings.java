package ru.sberbank.syncserver2.service.pub.xml;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="doSettings")
public class Settings extends Message {
	private Collection<Setting> settings;
	private SettingCommand command;
	
	@XmlElement
	public Collection<Setting> getSettings() {
		return settings;
	}

	public void setSettings(Collection<Setting> settings) {
		this.settings = settings;
	}
	
	@XmlElement
	public SettingCommand getCommand() {
		return command;
	}

	public void setCommand(SettingCommand command) {
		this.command = command;
	}

	public enum SettingCommand { 
		GET,
		SET
	}
	
	@XmlRootElement(name="setting")
	public static class Setting { 
		private String name;
		private String value;
		private String comment;
		
		public Setting() {
			
		}
		
		public Setting(String name, String value, String comment) {
			super();
			this.name = name;
			this.value = value;
			this.comment = comment;
		}
		@XmlAttribute
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}

		@XmlAttribute
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		
		@XmlAttribute
		public String getComment() {
			return comment;
		}
		public void setComment(String comment) {
			this.comment = comment;
		}
		
		
	}
		
	
	
	
}
