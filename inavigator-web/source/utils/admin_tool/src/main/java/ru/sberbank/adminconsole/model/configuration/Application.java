package ru.sberbank.adminconsole.model.configuration;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.gui.services.SwingRemoteRequestSender;

@XmlRootElement(name="application")
public class Application {
	
	private String name;
	private String URL;
	private String category;
	
	@XmlTransient
	private volatile String cookie;
	
	@XmlTransient
	private Integer id;
	
	@XmlTransient
	private volatile Status status = Status.UNDEFINED;
	
	
	private boolean main = false;
	
	public boolean isMain() {
		return main;
	}	
	@XmlAttribute
	public void setMain(boolean main) {
		this.main = main;
	}


	public Status getStatus() {
		return status;
	}
	public void setStatus(Status s) {
		status = s;		
	}
	
	public Application() {}
	
	public String toString() {
		return name;
	}

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlAttribute
	public String getURL() {
		return URL;
	}

	public void setURL(String uRL) {
		URL = uRL;
	}


	@XmlAttribute
	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}	
	
	public String getCookie() {
		return cookie;
	}
	public void setCookie(String cookie) {
		if (cookie!=null)
			this.cookie = cookie;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((URL == null) ? 0 : URL.hashCode());
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
		Application other = (Application) obj;
		if (URL == null) {
			if (other.URL != null)
				return false;
		} else if (!URL.equals(other.URL))
			return false;
		return true;
	}



	public static enum Status {
		OK,
		UNDEFINED,
		FAIL,
		ACCESS_DENIED
	}
	

}
