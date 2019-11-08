package ru.sbt.utils.backup.db;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.sbt.utils.backup.configuration.TableInfo;
import ru.sbt.utils.backup.conversion.ConvertorFactory;
import ru.sbt.utils.backup.conversion.IConvertor;
import ru.sbt.utils.backup.db.QueryManager.ResultQueryAndMapper;
import ru.sbt.utils.backup.file.CSVFile;
import ru.sbt.utils.backup.file.FileHelper;
import ru.sbt.utils.backup.model.Column;
import ru.sbt.utils.backup.model.Table;
import ru.sbt.utils.backup.util.ColumnsMapper;
import ru.sbt.utils.backup.util.Logger;

/**
 * SQL вызовы
 */
public class BackupDAO {	
	
	
	public static int readTableFromDBAndSaveToFile(Connection connection, TableInfo tinfo, File outputFile) throws SQLException, IOException {
		Statement stmt = null;
        ResultSet rs = null;
        int counter = 0;
        CSVFile file = null;
        
        try {
            stmt = connection.createStatement();
            String query = tinfo.getQuery();
            rs = stmt.executeQuery((query==null || query.trim().equals(""))?QueryManager.selectAllFromTable(tinfo):query);
            ResultSetMetaData rsmd = rs.getMetaData();
            int columnsNumber = rsmd.getColumnCount();
            
            List<String> row = new ArrayList<String>(columnsNumber);
            List<String> columnTypes = new ArrayList<String>(columnsNumber);
            for (int i = 1; i <= columnsNumber; i++) {
            	row.add(rsmd.getColumnName(i).toUpperCase());                
                columnTypes.add(rsmd.getColumnTypeName(i));                
            }    
            file = new CSVFile(outputFile, tinfo.getName(), row.toArray(new String[row.size()]));
            //file.save(columnTypes); //если надо сохранить типы, необходимо это учесть вкоманде восстановления
            row = new ArrayList<String>(columnsNumber);
            
            IConvertor[] convertors = ConvertorFactory.getConvertorFor(columnTypes);           
                        
            
            while (rs.next()) {                
                for (int i = 1; i <= columnsNumber; i++) {
                    row.add(convertors[i-1].convert(rs, i));                    
                }
                
                file.save(row);
                row.clear();
                
                counter++;
            }
			
        } finally {
        	SqlUtils.closeSqlObject(rs);
        	SqlUtils.closeSqlObject(stmt);
        	FileHelper.close(file);
        }
		return counter;
		
	}	
	
	public static Map<String,Table> getTablesInfo(Connection connection) {
		Statement stmt = null;
        ResultSet rs = null;
        Map<String,Table> info = new HashMap<String, Table>();
        
		try {	
			stmt = connection.createStatement();	
			rs = stmt.executeQuery(QueryManager.SQL_GET_DB_INFO);
			while (rs.next()) {
				String tableName = rs.getString("TableName").toUpperCase();
				String columnName = rs.getString("Column_Name").toUpperCase();
				String dataType = rs.getString("Data_Type").toLowerCase();
				Boolean isNullable = "YES".equalsIgnoreCase(rs.getString("Is_Nullable"));
				String identity = rs.getString("identity");
				String columnDefault = rs.getString("Column_Default");
				columnDefault = columnDefault==null?null:columnDefault.substring(1, columnDefault.length()-1);
				
				Table table = info.get(tableName);
				if (table==null) {
					table = new Table(tableName);
					info.put(tableName, table);
				}
				table.addColumn(identity==null?false:true,columnName, dataType, isNullable, columnDefault);
			}
			
			Logger.getInstance().success("Успешно получена информация о структуре БД");
		} catch (SQLException e) {
			Logger.getInstance().error("Во время получения информации о структуре БД произошли ошибки. (" + e.getMessage() + ")");
			return null;
		} finally {
			SqlUtils.closeSqlObject(rs);
			SqlUtils.closeSqlObject(stmt);
		}
		
		return info;
	}


	public static void restoreTableFromFile(Connection connection, File root, TableInfo tinfo, Table table) throws IOException, SQLException {
		
		CSVFile csvFile = new CSVFile(root, tinfo.getName());
		PreparedStatement pstmt = null;
		Statement stmt = null;
		try {
			
			List<String> row = csvFile.getRow(); 					// these are headers					
			
			ResultQueryAndMapper queryAndMapper = QueryManager.getUpdateOrInsert(table, tinfo, row);
			ColumnsMapper mapper = queryAndMapper.mapper; // mapper для строки из бэкапа в PreparedStatement
			Logger.getInstance().info("\n"+queryAndMapper.sql);
//			System.out.println("\n"+queryAndMapper.sql); //DEBUG сгенеренных запросов

			

					
			
			if (tinfo.isClear()) {
				stmt = connection.createStatement();
				stmt.execute(QueryManager.deleteAllFromTable(tinfo));
				Logger.getInstance().info("Таблица "+tinfo.getName()+" очищена");
				stmt.close();
			}
			
			if (tinfo.isIdentityInsert()) {
				// ставим флаг
				stmt = connection.createStatement();
				stmt.execute(QueryManager.IdentityInsertOn(tinfo));				
				Logger.getInstance().info("Добавлен флаг IDENTITY_INSERT для таблицы "+tinfo.getName());
				stmt.close();
			}

			if (tinfo.getCaseSensitiveColumns() != null && !tinfo.getCaseSensitiveColumns().trim().isEmpty()) {
				// ставим case sensitive для необходимых колонок
				for (String s : tinfo.getCaseSensitiveColumnsList()) {
					String[] split = s.split(":");
					if (split.length == 2) {
						stmt = connection.createStatement();
						stmt.execute(QueryManager.CaseSensitiveOn(tinfo.getName(), split[0], split[1]));
						Logger.getInstance().info("Установлен case sensitive для " + split[0]);
					} else {
						Logger.getInstance().error("Невозможно установить case sensitive: " + s);
					}
				}
			}
			
			
			int i = 0;
			pstmt = connection.prepareStatement(queryAndMapper.sql);
			while ((row = csvFile.getRow()) != null) {
				i++;
				mapper.mapAllValues(pstmt, row);
//				pstmt.addBatch();
//				if (i > 500) {
//					i = 0;
//					pstmt.executeBatch();
//				}
				pstmt.executeUpdate();
			}
//			pstmt.executeBatch();
			
			if (tinfo.isIdentityInsert()) {
				// убираем флаг
				stmt = connection.createStatement();
				stmt.execute(QueryManager.IdentityInsertOff(tinfo));
				Logger.getInstance().info("Убран флаг IDENTITY_INSERT для таблицы "+tinfo.getName());
				stmt.close();
			}

			if (tinfo.getCaseSensitiveColumns() != null && !tinfo.getCaseSensitiveColumns().trim().isEmpty()) {
				// ставим case sensitive для необходимых колонок
				for (String s : tinfo.getCaseSensitiveColumnsList()) {
					String[] split = s.split(":");
					if (split.length == 2) {
						stmt = connection.createStatement();
						stmt.execute(QueryManager.CaseSensitiveOff(tinfo.getName(), split[0], split[1]));
						Logger.getInstance().info("Откачен case sensitive для " + split[0]);
					} else {
						Logger.getInstance().error("Невозможно вернуть case sensitive: " + s);
					}
				}
			}
			
		} finally {
			SqlUtils.closeSqlObject(stmt);
			SqlUtils.closeSqlObject(pstmt);
			FileHelper.close(csvFile);			
		}		
	}
	
}
