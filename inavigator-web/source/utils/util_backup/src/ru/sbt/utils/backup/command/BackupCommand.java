package ru.sbt.utils.backup.command;

import java.io.File;
import java.sql.Connection;
import java.util.Date;

import ru.sbt.utils.backup.configuration.DatabaseInfo;
import ru.sbt.utils.backup.configuration.DatabaseServerInfo;
import ru.sbt.utils.backup.configuration.TableInfo;
import ru.sbt.utils.backup.configuration.XmlConfiguration;
import ru.sbt.utils.backup.db.BackupDAO;
import ru.sbt.utils.backup.db.SqlUtils;
import ru.sbt.utils.backup.file.FileHelper;
import ru.sbt.utils.backup.util.DateUtils;
import ru.sbt.utils.backup.util.Logger;


public class BackupCommand extends AbstractCommand {
	
	public BackupCommand(XmlConfiguration configuration) {
		super(configuration);
	}

	@Override
	public String getCommandName() {
		return "BACKUP";
	}

	@Override
	public int execute(String args[]) {
		Logger.getInstance().info("Начинается процесс бекапирования.");
		String timeStamp = DateUtils.getFormattedDate(new Date(), "yyyyMMdd_HHmmss");
		Logger.getInstance().info("Сформирована метка " + timeStamp + ".");
		for(DatabaseServerInfo dbsinfo : configuration.getDatabaseServers()) {
			for(DatabaseInfo dbinfo:dbsinfo.getDatabases()) {
				
				Connection connection = null;
				try {
					connection =  SqlUtils.getConnection(dbsinfo, dbinfo);
					if (connection == null) {
						Logger.getInstance().error("Не удалось получить соединение к БД "+ Logger.createFullSqlStringName(dbsinfo, dbinfo) + ". Эта БД будет пропущена.");
					} else {
						readAndSaveAllTables(connection, FileHelper.getBackupFolder(configuration, timeStamp, dbsinfo, dbinfo), dbsinfo, dbinfo);
					}
				} finally {
					SqlUtils.closeConnection(connection);
				}
			}
		}
		
		return 0;
	}
	
	protected boolean skip(String tableName) {
		if ("SYNC_LOGS".equals(tableName)) {
			Logger.getInstance().info("Общая команда бекапирования не работает с таблицами SYNC_LOGS");
			return true;
		} else 
			return false;
	}
	
	protected void readAndSaveAllTables(Connection connection, File outFile, DatabaseServerInfo dbsinfo, DatabaseInfo dbinfo) {	
		for(TableInfo tinfo:dbinfo.getTables()) {		
			if (skip(tinfo.getName().toUpperCase())) {
	        	Logger.getInstance().info("Таблица "  + Logger.createFullSqlStringName(dbsinfo, dbinfo, tinfo) + " пропущена.");						
			} else {

				Logger.getInstance().info("Начинается чтение таблицы " + Logger.createFullSqlStringName(dbsinfo, dbinfo, tinfo));
				try {
					int count = BackupDAO.readTableFromDBAndSaveToFile(connection, tinfo, outFile);
					Logger.getInstance().success("Успешно завершен процесс чтения и записи в файл таблицы "+tinfo.getName()+". Найдено " + count + " записей");				
				} catch (Exception e) {
					Logger.getInstance().error("Во время чтения и сохранения таблицы в файл произошли ошибки (" + e.getMessage() + "). Таблица "  + Logger.createFullSqlStringName(dbsinfo, dbinfo, tinfo) + " будет пропущена.");
				}
				
			}
		}
	}

}
