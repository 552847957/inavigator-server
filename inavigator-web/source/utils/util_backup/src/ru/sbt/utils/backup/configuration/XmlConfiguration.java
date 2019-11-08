package ru.sbt.utils.backup.configuration;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="configuration")
public class XmlConfiguration {
	
	
	private String backupPath;
	private String logsRoot;
	
	@XmlAttribute
	public String getBackupPath() {
		return backupPath;
	}

	public void setBackupPath(String backupPath) {
		this.backupPath = backupPath;
	}

	@XmlAttribute
	public String getLogsRoot() {
		return logsRoot;
	}

	public void setLogsRoot(String logsRoot) {
		this.logsRoot = logsRoot;
	}

	private List<DatabaseServerInfo> databaseServers;

	@XmlElementWrapper(name="servers")
	@XmlElementRef()	
	public List<DatabaseServerInfo> getDatabaseServers() {
		return databaseServers;
	}

	public void setDatabaseServers(List<DatabaseServerInfo> databaseServers) {
		this.databaseServers = databaseServers;
	}
	
	public static XmlConfiguration readConfiguration(InputStream is) throws JAXBException  {
		JAXBContext jaxbContext = JAXBContext.newInstance(XmlConfiguration.class);
		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
		XmlConfiguration xmlInfo = (XmlConfiguration) jaxbUnmarshaller.unmarshal(is);
		return xmlInfo;
	}	
	
	
	
	
}
