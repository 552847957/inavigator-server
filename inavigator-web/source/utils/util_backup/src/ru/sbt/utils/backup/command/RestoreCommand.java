package ru.sbt.utils.backup.command;

import java.io.File;
import java.sql.Connection;
import java.util.Map;

import ru.sbt.utils.backup.configuration.DatabaseInfo;
import ru.sbt.utils.backup.configuration.DatabaseServerInfo;
import ru.sbt.utils.backup.configuration.TableInfo;
import ru.sbt.utils.backup.configuration.XmlConfiguration;
import ru.sbt.utils.backup.db.BackupDAO;
import ru.sbt.utils.backup.db.SqlUtils;
import ru.sbt.utils.backup.file.FileHelper;
import ru.sbt.utils.backup.model.Table;
import ru.sbt.utils.backup.util.Logger;

/**
 * команда восстановления настроек из бекапа
 * @author sbt-gordienko-mv
 *
 */
public class RestoreCommand extends AbstractCommand {
	
	public RestoreCommand(XmlConfiguration configuration) {
		super(configuration);
	}
	
	@Override
	public String getCommandName() {
		return "RESTORE";
	}

	@Override
	public int execute(String args[]) {
		Logger.getInstance().info("Начинается процесс восстановления.");
		if (args.length<2) {
			Logger.getInstance().error("Не введена метка.");
			throw new RuntimeException("Can't get timestamp label.");
		}
		String timeStamp = args[1];		
		Logger.getInstance().info("Получена метка " + timeStamp + ".");
		for(DatabaseServerInfo dbsinfo : configuration.getDatabaseServers()) {
			for(DatabaseInfo dbinfo:dbsinfo.getDatabases()) {
				Connection connection = null;
				try {
					connection = SqlUtils.getConnection(dbsinfo, dbinfo);
					if (connection == null) {
						Logger.getInstance().error("Не удалось получить соединение к БД "+ Logger.createFullSqlStringName(dbsinfo, dbinfo) + ". Эта БД будет пропущена.");
					} else {
						restoreAllTables(connection, FileHelper.getBackupFolder(configuration, timeStamp, dbsinfo, dbinfo), dbsinfo, dbinfo);
					}
				} finally {
					SqlUtils.closeConnection(connection);
				}
			}
		}
		return 0;
	
	}
	
	protected void restoreAllTables(Connection connection, File root, DatabaseServerInfo dbsinfo, DatabaseInfo dbinfo) {
		Map<String, Table> tables = BackupDAO.getTablesInfo(connection);
		if (tables == null) {
			Logger.getInstance().error("Не получена информация о структуре БД "+Logger.createFullSqlStringName(dbsinfo, dbinfo)+". Восстановление этой БД пропущено.");
			return;
		}
		
		for(TableInfo tinfo:dbinfo.getTables()) {
			
			if (tinfo.isSkipRestore()) {
				Logger.getInstance().info("Восстановление таблицы " + Logger.createFullSqlStringName(dbsinfo, dbinfo, tinfo) + " пропущено.");
				continue;
			}
			
			if (tinfo.getQuery()!=null && !tinfo.getQuery().trim().equals("")) {
				Logger.getInstance().info("Восстановление таблицы " + Logger.createFullSqlStringName(dbsinfo, dbinfo, tinfo) + " пропущено (невозможно восстановить таблицу с пользовательским запросом).");
				continue;
			}
			
			Table table = tables.get(tinfo.getName()); 
			
			Logger.getInstance().info("Начинается чтение таблицы " + Logger.createFullSqlStringName(dbsinfo, dbinfo, tinfo) + " из файла и его восстановление.");
			
			if (table==null) {
				Logger.getInstance().error("Не найдена информация о таблице "  + Logger.createFullSqlStringName(dbsinfo, dbinfo, tinfo) + " в БД. Возможно, такая таблица не существует.");
				continue;
			}
			
			try {
				BackupDAO.restoreTableFromFile(connection, root, tinfo, table);
				Logger.getInstance().success("Таблица "+tinfo.getName()+" успешно восстановлена.");		
			} catch (BackupNotFound e) {
				Logger.getInstance().error("Не найден бекап для таблицы "+Logger.createFullSqlStringName(dbsinfo, dbinfo, tinfo)+" (" + e.getPath() + "). Восстановление этой таблицы пропущено.");
			} catch (Exception e) {
				Logger.getInstance().error("Во время чтения из файла и сохранения таблицы в БД произошли ошибки (" + e.getMessage() + "). Таблица "  + Logger.createFullSqlStringName(dbsinfo, dbinfo, tinfo) + " не восстановлена.");
			}
		}
	}
	
	public static class BackupNotFound extends RuntimeException {

		/**
		 * 
		 */
		private static final long serialVersionUID = 123456L;

		private String path;

		public BackupNotFound() {
		}

		public BackupNotFound(String path) {
			this.path = path;
		}

		public String getPath() {
			return path;
		}
	}

}
