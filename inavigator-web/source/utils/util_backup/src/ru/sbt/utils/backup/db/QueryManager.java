package ru.sbt.utils.backup.db;

import java.util.*;

import ru.sbt.utils.backup.configuration.TableInfo;
import ru.sbt.utils.backup.model.Column;
import ru.sbt.utils.backup.model.Table;
import ru.sbt.utils.backup.util.ColumnsMapper;


public class QueryManager {
	
	public static final String SQL_GET_DB_INFO = "SELECT  isc.Table_Name AS TableName, "
			+"Column_Name, "
			+"Data_Type , "
			+"isc.IS_NULLABLE, "
			+"Column_Default, "
			+"ind.name as 'identity'"
			+"FROM    INFORMATION_SCHEMA.COLUMNS isc "
			+"INNER JOIN  information_schema.tables ist "
			+"ON isc.table_name = ist.table_name "
			+"LEFT JOIN sys.identity_columns ind "
			+"ON isc.TABLE_NAME = OBJECT_NAME(ind.object_id) AND isc.COLUMN_NAME = ind.name";
	

	private static final Set<String> incompatible_in_the_equal_to_operator = new HashSet<String>();
	static {
		incompatible_in_the_equal_to_operator.add("image");
		incompatible_in_the_equal_to_operator.add("varbinary");
	}
	private QueryManager() {
	}
	
	
	public static String selectAllFromTable(TableInfo tableinfo) {
		return "select * from "+tableinfo.getName();
	}
	
	public static String deleteAllFromTable(TableInfo tableinfo) {
		return "TRUNCATE TABLE "+tableinfo.getName();
	}


	public static ResultQueryAndMapper getUpdateOrInsert(Table table, TableInfo tableinfo, List<String> backupedHeaders) {
		/*if ("SYNC_PUSH_NOTIFICATIONS_CLIENTS".equals(table.getName())) {
			String[] columns = new String[]{"SYNC_PNC_OS_CODE", "SYNC_PNC_APPLICATION_CODE", "SYNC_PNC_APPLICATION_VERSION", "SYNC_PNC_EMAIL", "SYNC_PNC_DEVICE_NAME", "SYNC_PNC_DEVICE_CODE", "SYNC_PNC_APPLE_TOKEN", "SYNC_PNC_INVALIDATED_DATE"};
			String query = "" +
					"merge into SYNC_PUSH_NOTIFICATIONS_CLIENTS as t \n" +
					"using (values (?,?,?,?,?,?,?,?)) as s (SYNC_PNC_OS_CODE, SYNC_PNC_APPLICATION_CODE, SYNC_PNC_APPLICATION_VERSION, SYNC_PNC_EMAIL, SYNC_PNC_DEVICE_NAME, SYNC_PNC_DEVICE_CODE, SYNC_PNC_APPLE_TOKEN, SYNC_PNC_INVALIDATED_DATE)\n" +
					"on t.SYNC_PNC_DEVICE_CODE=ISNULL(s.SYNC_PNC_DEVICE_CODE, '') and t.SYNC_PNC_APPLICATION_CODE=ISNULL(s.SYNC_PNC_APPLICATION_CODE, '') and CONVERT(VARBINARY(MAX), t.SYNC_PNC_EMAIL)=CONVERT(VARBINARY(MAX), s.SYNC_PNC_EMAIL) and t.SYNC_PNC_OS_CODE=ISNULL(s.SYNC_PNC_OS_CODE, '')\n" +
					"when matched then update set t.SYNC_PNC_DEVICE_NAME=s.SYNC_PNC_DEVICE_NAME, t.SYNC_PNC_APPLICATION_VERSION = s.SYNC_PNC_APPLICATION_VERSION, t.SYNC_PNC_INVALIDATED_DATE = s.SYNC_PNC_INVALIDATED_DATE, t.SYNC_PNC_APPLE_TOKEN = s.SYNC_PNC_APPLE_TOKEN\n" +
					"when not matched then insert (SYNC_PNC_OS_CODE, SYNC_PNC_APPLICATION_CODE, SYNC_PNC_APPLICATION_VERSION, SYNC_PNC_EMAIL, SYNC_PNC_DEVICE_NAME, SYNC_PNC_DEVICE_CODE, SYNC_PNC_APPLE_TOKEN, SYNC_PNC_INVALIDATED_DATE) values (?,?,?,?,?,?,?,?);";
			ArrayList<Column> orderedColumns = new ArrayList<Column>();
			for (String column : columns) {
				orderedColumns.add(table.getColumn(column));
			}
			orderedColumns.addAll(Collections.unmodifiableList(orderedColumns));
			ColumnsMapper mapper = new ColumnsMapper(orderedColumns, backupedHeaders);

			return new ResultQueryAndMapper(query, mapper);
		} else {*/
			table.deleteUnnecessaryColumns(tableinfo, backupedHeaders);

			Set<Column> updates = new HashSet<Column>();
			for (Iterator<String> it = tableinfo.getColumnsUpdateOn().iterator(); it.hasNext();) {
				String columnName = it.next();
				Column c = table.getColumn(columnName);
				if (c == null) throw new RuntimeException("Can't find column "+columnName);
				updates.add(c);
			}
			List<Column> sets = new ArrayList<Column>();
			for (Iterator<Column> it = table.getColumns().iterator(); it.hasNext();) {
				Column c = it.next();
				if (!updates.contains(c) && !c.identity) //update identity колонки делать запрещено
					sets.add(c);
			}


			if (tableinfo.isUpdateOnly() && updates.size() == 0)
				throw new RuntimeException("No columns for updates. Try to turn off flag updateOnly.");

			if (sets.size() == 0)
				throw new RuntimeException("No columns to set.");

			StringBuilder sb = new StringBuilder();
			List<Column> orderedColumns = new ArrayList<Column>();
			boolean first = true;
			if (updates.size()>0) {
				// добавляем update
				sb.append("update ").append(table.getName()).append(" set ");
				for (Column column: sets) {
					if (!first) {
						sb.append(",");
					} else
						first = false;
					sb.append(column.columnName).append(" = ?");
					orderedColumns.add(column);
				}
				sb.append(" where ");
				first = true;
				for (Column column: updates) {
					if (first)
						first = false;
					else
						sb.append(" and ");
					sb.append(column.columnName).append(" = ?");
					orderedColumns.add(column);
				}

				if (!tableinfo.isUpdateOnly())
					sb.append(" \n if @@ROWCOUNT = 0 \n ");
			} else {
				// перед insert надо убедиться, что такой строки еще нет
				sb.append("if not exists(select 1 from ").append(table.getName()).append(" where ");
				first = true;
				for (Column column: table.getColumns()) {
					if (incompatible_in_the_equal_to_operator.contains(column.dataType))
						continue; // пропускаем колонки, которые нельзя сравнивать
					if (first)
						first = false;
					else
						sb.append(" and ");
					sb.append(column.columnName).append(" = ?");
					orderedColumns.add(column);
				}
				sb.append(") ");
			}

			if (!tableinfo.isUpdateOnly()) {
				//добавляем insert
				sb.append(" insert into ").append(table.getName());
				String temp = "";
				for (Column column: table.getColumns()) {
					sb.append(temp.equals("")?" (":",").append(column.columnName);
					temp = temp + (temp.equals("")?"?":",?");
					orderedColumns.add(column);
				}
				sb.append(") values (").append(temp).append(")");
			}

			ColumnsMapper mapper = new ColumnsMapper(orderedColumns, backupedHeaders);

			return new ResultQueryAndMapper(sb.toString(), mapper);
//		}
	}
	
	public static String IdentityInsertOn(TableInfo tableinfo) {
		return " set identity_insert "+tableinfo.getName()+" on ";
	}
	
	public static String IdentityInsertOff(TableInfo tableinfo) {
		return " set identity_insert "+tableinfo.getName()+" off ";
	}

	public static String CaseSensitiveOn(String tableName, String column, String size) {
		return String.format("alter table %s alter column %s varchar(%s) COLLATE Cyrillic_General_CS_AS", tableName, column, size);
	}

	public static String CaseSensitiveOff(String tableName, String column, String size) {
		return String.format("alter table %s alter column %s varchar(%s) COLLATE Cyrillic_General_CI_AS", tableName, column, size);
	}

	public static class ResultQueryAndMapper {
		public final String sql;
		public final ColumnsMapper mapper;
		ResultQueryAndMapper(String sql, ColumnsMapper mapper) {
			this.sql = sql;
			this.mapper = mapper;
		}
	}
}
