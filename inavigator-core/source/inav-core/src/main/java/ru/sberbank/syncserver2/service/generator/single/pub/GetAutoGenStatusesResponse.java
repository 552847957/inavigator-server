package ru.sberbank.syncserver2.service.generator.single.pub;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="getAutoGenStatusesResponse")
public class GetAutoGenStatusesResponse {
	
	private List<AutoGenStatus> statuses;
	
	
	private boolean active;
	
	
	@XmlAttribute(name="active")
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public List<AutoGenStatus> getStatuses() {
		return statuses;
	}

	@XmlElement(name="statuses")
	public void setStatuses(List<AutoGenStatus> statuses) {
		this.statuses = statuses;
	}

	@XmlRootElement(name="status")
	public static class AutoGenStatus {
		
		private List<Long> misDbTimeStamps;
		private List<String> misDbReadaleDates;
		private List<Long> generatorDbTimeStamp;
		private List<String> generatorDbReadaleDate;
		private boolean autoGenUIDisabled;
		private boolean misRequestError;
		private String fileName;
		private String applicationName;
		
		@XmlElementWrapper(name="misDbTimestamps",nillable=true)
		@XmlElement(name="misDbTimestamp",nillable=true)
		public List<Long> getMisDbTimeStamps() {
			return misDbTimeStamps;
		}
		public void setMisDbTimeStamps(List<Long> misDbTimeStamps) {
			this.misDbTimeStamps = misDbTimeStamps;
		}

		@XmlElementWrapper(name="misDbReadableDates",nillable=true) 
		@XmlElement(name="misDbReadableDate",nillable=true)
		public List<String> getMisDbReadaleDates() {
			return misDbReadaleDates;
		}
		public void setMisDbReadaleDates(List<String> misDbReadaleDates) {
			this.misDbReadaleDates = misDbReadaleDates;
		}

		@XmlElementWrapper(name="misGeneratorDbTimestamps",nillable=true)
		@XmlElement(name="misGeneratorDbTimestamp",nillable=true)
		public List<Long> getGeneratorDbTimeStamp() {
			return generatorDbTimeStamp;
		}
		public void setGeneratorDbTimeStamp(List<Long> generatorDbTimeStamp) {
			this.generatorDbTimeStamp = generatorDbTimeStamp;
		}

		@XmlElementWrapper(name="misGeneratorReadableDates",nillable=true)
		@XmlElement(name="misGeneratorReadableDate",nillable=true)
		public List<String> getGeneratorDbReadaleDate() {
			return generatorDbReadaleDate;
		}
		public void setGeneratorDbReadaleDate(List<String> generatorDbReadaleDate) {
			this.generatorDbReadaleDate = generatorDbReadaleDate;
		}
		public boolean isMisRequestError() {
			return misRequestError;
		}
		public void setMisRequestError(boolean misRequestError) {
			this.misRequestError = misRequestError;
		}
		public boolean isAutoGenUIDisabled() {
			return autoGenUIDisabled;
		}
		public void setAutoGenUIDisabled(boolean autoGenUIDisabled) {
			this.autoGenUIDisabled = autoGenUIDisabled;
		}
		public String getFileName() {
			return fileName;
		}
		public void setFileName(String fileName) {
			this.fileName = fileName;
		}
		public String getApplicationName() {
			return applicationName;
		}
		public void setApplicationName(String applicationName) {
			this.applicationName = applicationName;
		}
		
		
	}
}
