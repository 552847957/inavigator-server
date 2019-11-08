package ru.sbt.utils.backup.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ru.sbt.utils.backup.configuration.TableInfo;

/**
 * Модель - Таблица БД
 */
public class Table {
	
	private String name;
	private Map<String,Column> columns;

	public Table(String name) {
    	this.name = name;
    	columns = new HashMap<String,Column>();
    }   

    public String getName() {
    	return name;
    }   
    
    
	public void addColumn(boolean identity, String columnName, String dataType, Boolean isNullable, String columnDefault) {
		columns.put(columnName,new Column(identity, columnName, dataType, isNullable, columnDefault));
	}

	/**
	 * delete Column (nullable or with default value) from columns if backupedHeaders don't contain this column
	 * delete identity Column if identity_insert is switched off
	 * @param tinfo
	 * @param backupedHeaders
	 */
	public void deleteUnnecessaryColumns(TableInfo tinfo, List<String> backupedHeaders) {
		Set<String> ignore = tinfo.getColumnsToIgnore();
		for (Iterator<Column> it = columns.values().iterator(); it.hasNext(); ) {
			Column c = it.next();			
			if (	(!backupedHeaders.contains(c.columnName) && 
							(c.nullable || c.columnDefault != null) ) 
					|| (c.identity && !tinfo.isIdentityInsert()) || (ignore.contains(c.columnName)))
				it.remove();
		}
	}
	
	public Collection<Column> getColumns() {
		return columns.values();
	}
	
	public Column getColumn(String name) {
		return columns.get(name);
	}
}
