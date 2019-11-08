package ru.sbt.utils.backup.command;

import ru.sbt.utils.backup.configuration.DatabaseInfo;
import ru.sbt.utils.backup.configuration.DatabaseServerInfo;
import ru.sbt.utils.backup.configuration.XmlConfiguration;
import ru.sbt.utils.backup.db.SqlUtils;
import ru.sbt.utils.backup.file.FileHelper;
import ru.sbt.utils.backup.model.SQLScript;
import ru.sbt.utils.backup.util.Logger;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.*;


public class RunScriptsCommand extends AbstractCommand {

	public RunScriptsCommand(XmlConfiguration configuration) {
		super(configuration);
	}

	@Override
	public String getCommandName() {
		return "RUN_SCRIPTS";
	}

	@Override
	public int execute(String args[]) {
		Logger.getInstance().info("Запуск процесса выполнения скриптов");

		for(DatabaseServerInfo dbsinfo : configuration.getDatabaseServers()) {
			for(DatabaseInfo dbinfo:dbsinfo.getDatabases()) {
				Logger.getInstance().info("Начинается считывание скриптов для БД "+dbinfo.getName()+" из директории "+dbinfo.getScriptsDir());
				if (dbinfo.getScriptsDir() == null) {
					Logger.getInstance().info("Не указана директория со скриптами. Эта БД будет пропущена.");
					continue;
				}
				File scriptsFolder = new File(dbinfo.getScriptsDir());
				if (!scriptsFolder.isDirectory()) {
					Logger.getInstance().error("Указанная директория со скриптами не существует. См. "+scriptsFolder.getAbsolutePath()+". Эта БД будет пропущена.");
					continue;
				}
				Map<String, List<SQLScript>> scripts;
				try {
					scripts = FileHelper.readScriptsFromDirectory(scriptsFolder);
				} catch (IOException e) {
					Logger.getInstance().error("Ошибка при чтении скриптов в директории "+scriptsFolder.getAbsolutePath()+": "+e.toString()+" Эта БД будет пропущена.");
					if (e.getCause() != null)
						Logger.getInstance().printStackTrace(e.getCause());
					else
						Logger.getInstance().printStackTrace(e);
					continue;
				}
				if (scripts == null || scripts.isEmpty()) {
					Logger.getInstance().info("Не найдены скрипты в директории "+scriptsFolder.getAbsolutePath()+". Эта БД будет пропущена.");
					continue;
				}
				Logger.getInstance().success("В директории найдено "+scripts.size()+" скрипт(ов).");
				List<String> scriptNames = new ArrayList<String>(scripts.keySet());
				Collections.sort(scriptNames, new Comparator<String>() {
					@Override
					public int compare(String s1, String s2) {
						return s1.compareToIgnoreCase(s2);
					}
				});
				Logger.getInstance().info("В БД "+ Logger.createFullSqlStringName(dbsinfo, dbinfo)+" будут применены скрипты в данном порядке: "+scriptNames.toString());

				Connection connection = null;
				try {
					connection = SqlUtils.getConnection(dbsinfo, dbinfo);
					if (connection == null) {
						Logger.getInstance().error("Не удалось получить соединение к БД "+ Logger.createFullSqlStringName(dbsinfo, dbinfo) + ". Эта БД будет пропущена.");
						continue;
					}
					for (String scriptName: scriptNames) {
						Logger.getInstance().info("Начинается выполнение скрипта "+scriptName);
						try {
							executeScripts(scripts.get(scriptName), connection);
							Logger.getInstance().success("Выполнение скрипта "+scriptName+" выполнено успешно.");
						} catch (SQLException e) {
							Logger.getInstance().error("При выполнении скрипта " + scriptName + " возникла ошибка: " + e.toString()+" Дальнейшее выполнение скриптов в этой БД прервано!");
							continue;
						}
					}
				} finally {
					SqlUtils.closeConnection(connection);
				}
			}
		}
		return 0;
	}


	protected void executeScripts(List<SQLScript> scripts, Connection connection) throws SQLException {
		for (SQLScript script: scripts) {
			try {
				Statement statement = connection.createStatement();
				statement.execute(script.getSql());
				SQLWarning warnings = statement.getWarnings();
				while (warnings != null) {
					Logger.getInstance().info("При выполнении скрипта получено предупреждение: " + warnings.getMessage());
					warnings = warnings.getNextWarning();
				}
			} catch (SQLException e) {
				Logger.getInstance().printAdditionalInfo(script.getSql());
				throw e;
			}
		}
	}
}
