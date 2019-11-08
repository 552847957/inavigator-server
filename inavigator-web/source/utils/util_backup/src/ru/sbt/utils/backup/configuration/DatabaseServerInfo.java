package ru.sbt.utils.backup.configuration;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="server")
public class DatabaseServerInfo {
	
	/**
	 * Имя сервера
	 */
	private String serverName;
	
	/**
	 * Хост сервера БД
	 */
	private String host;
	
	/**
	 * Порт сеевера БД
	 */
	private String port;
	
	/**
	 * Имя пользователя
	 */
	private String username;
	
	/**
	 * Пароль
	 */
	private String password;

	/**
	 * Аутентификация из под пользователя под которым запущено приложение
	 */
	private boolean windowsSecurity;
	
	/**
	 * 
	 */
	private List<DatabaseInfo> databases;

	@XmlAttribute
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	@XmlAttribute
	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	@XmlAttribute
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@XmlAttribute
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@XmlElementRef()	
	public List<DatabaseInfo> getDatabases() {
		return databases;
	}

	public void setDatabases(List<DatabaseInfo> databases) {
		this.databases = databases;
	}

	@XmlAttribute
	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}
	
	@XmlAttribute
	public boolean isWindowsSecurity() {
		return windowsSecurity;
	}

	public void setWindowsSecurity(boolean windowsSecurity) {
		this.windowsSecurity = windowsSecurity;
	}
	
	
	
}
