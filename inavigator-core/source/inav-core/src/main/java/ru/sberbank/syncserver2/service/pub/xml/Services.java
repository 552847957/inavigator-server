package ru.sberbank.syncserver2.service.pub.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.*;

import ru.sberbank.syncserver2.service.core.ServiceState;

@XmlRootElement(name="getServices")
public class Services extends Message {
	
	private Integer applicationId;
	
	
	private ServiceCommand command;
	
	private List<Service> services = new ArrayList<Service>();
	
	
	public Services() {
	}

	public Services(Integer applicationId, ServiceCommand command) {
		this.applicationId = applicationId;
		this.command = command;
	}	
		
	@XmlElementWrapper(name="services")
	@XmlElementRef()
	public List<Service> getServices() {
		return services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
	}
	@XmlAttribute
	public ServiceCommand getCommand() {
		return command;
	}
	
	public void setCommand(ServiceCommand command) {
		this.command = command;
	}
	
	public Integer getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(Integer applicationId) {
		this.applicationId = applicationId;
	}
	
	public static enum ServiceCommand {
		GET,
		STOP,
		START;
	}

	@XmlRootElement(name="service")
	public static class Service {
		private String code;
		private ServiceState state;	
		private String folder;
		
		public Service() {
			super();
		}

		public Service(String folder, String code, ServiceState state) {
			this.code = code;
			this.state = state;
			this.folder = folder;
		}
		
		public String getCode() {
			return code;
		}
		public ServiceState getState() {
			return state;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public void setState(ServiceState state) {
			this.state = state;
		}	
		
		@Override
		public String toString() {		
			return folder+"/"+code;
		}
		
		public String getFolder() {
			return folder;
		}
		
		public void setFolder(String folder) {
			this.folder = folder;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((code == null) ? 0 : code.hashCode());
			result = prime * result
					+ ((folder == null) ? 0 : folder.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Service other = (Service) obj;
			if (code == null) {
				if (other.code != null)
					return false;
			} else if (!code.equals(other.code))
				return false;
			if (folder == null) {
				if (other.folder != null)
					return false;
			} else if (!folder.equals(other.folder))
				return false;
			return true;
		}

				
		
	}

}
