package ru.sbt.utils.backup.configuration;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="database")
public class DatabaseInfo {
	
	/**
	 * Имя базы данных
	 */
	private String name;
	
	/**
	 * Информация о таблицах
	 */
	private List<TableInfo> tables;

	/**
	 * Путь к папке с SQL скриптами
	 */
	private String scriptsDir;


	
	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@XmlElementRef()
	public List<TableInfo> getTables() {
		return tables;
	}

	public void setTables(List<TableInfo> tables) {
		this.tables = tables;
	}

	@XmlAttribute
	public String getScriptsDir() {
		return scriptsDir;
	}

	public void setScriptsDir(String scriptsDir) {
		this.scriptsDir = scriptsDir;
	}
}
