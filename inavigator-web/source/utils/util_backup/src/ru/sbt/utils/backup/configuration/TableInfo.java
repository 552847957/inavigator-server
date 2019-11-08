package ru.sbt.utils.backup.configuration;

import java.util.*;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="table")
public class TableInfo {
	
	/**
	 * Название таблицы
	 */
	private String name;
	
	/**
	 * имя колонок таблицы через ";", по которым необходимо делать update
	 */
	private String update = null;
	
	/**
	 * имя колонок таблицы через ";", которые необходимо игнорировать
	 */
	private String ignore = null;

	/**
	 * имя колонок таблицы через ; для которых необходимо включить, а затем выключить case sensitive сравнение строк (применимо только для MSSQL)
	 */
	private String caseSensitiveColumns = null;
	
	/**
	 * если true, то перед воостановлением необходимо очистить таблицу
	 */
	private boolean clear = false;
	
	/**
	 * если true, то перед восстановлением будет выставлен флаг IDENTITY_INSERT
	 */
	private boolean identityInsert = false;
	
	/**
	 * если true, то восстановление будет только обновлять сущестующие записи
	 */
	private boolean updateOnly = false;
	
	/**
	 * если true, то эта таблица будет пропущена при восстановлении
	 */
	private boolean skipRestore = false;
	
	/**
	 * пользовательский запрос (ипользуется для сохранения данных из нескольких таблиц в одну)
	 */
	private String query = null;
	

	@XmlAttribute
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.toUpperCase();
	}

	@XmlAttribute
	public String getUpdate() {
		return update;
	}

	public void setUpdate(String sync) {
		this.update = sync.toUpperCase();
	}

	@XmlAttribute
	public boolean isClear() {
		return clear;
	}

	public void setClear(boolean clean) {
		this.clear = clean;
	}	
	
	public Set<String> getColumnsUpdateOn() {
		return getColumnsSet(update);
	}
	
	public Set<String> getColumnsToIgnore() {
		return getColumnsSet(ignore);
	}
		
	@XmlAttribute
	public boolean isIdentityInsert() {
		return identityInsert;
	}

	public void setIdentityInsert(boolean identityInsert) {
		this.identityInsert = identityInsert;
	}

	private Set<String> getColumnsSet(String columns) {
		if (columns==null) 
			return Collections.emptySet();
		Set<String> result = new HashSet<String>();
		for (String s: columns.split(";")) {
			result.add(s.trim());
		}
		return result;
	}

	@XmlAttribute
	public boolean isUpdateOnly() {
		return updateOnly;
	}

	public void setUpdateOnly(boolean updateOnly) {
		this.updateOnly = updateOnly;
	}

	@XmlAttribute
	public boolean isSkipRestore() {
		return skipRestore;
	}

	public void setSkipRestore(boolean skipRestore) {
		this.skipRestore = skipRestore;
	}

	@XmlElement
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	@XmlAttribute
	public String getIgnore() {
		return ignore;
	}

	public void setIgnore(String ignore) {
		this.ignore = ignore;
	}

	@XmlAttribute
	public String getCaseSensitiveColumns() {
		return caseSensitiveColumns;
	}

	public void setCaseSensitiveColumns(String caseSensitiveColumns) {
		this.caseSensitiveColumns = caseSensitiveColumns;
	}

	public List<String> getCaseSensitiveColumnsList() {
		return Arrays.asList(caseSensitiveColumns.trim().split(";"));
	}
}
