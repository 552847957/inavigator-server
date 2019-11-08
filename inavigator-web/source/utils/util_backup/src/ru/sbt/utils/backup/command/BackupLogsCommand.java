package ru.sbt.utils.backup.command;

import ru.sbt.utils.backup.configuration.XmlConfiguration;
import ru.sbt.utils.backup.util.Logger;


public class BackupLogsCommand extends BackupCommand {
	
	public BackupLogsCommand(XmlConfiguration configuration) {
		super(configuration);
	}

	@Override
	public String getCommandName() {
		return "BACKUPLOGS";
	}

	@Override
	protected boolean skip(String tableName) {
		if (!"SYNC_LOGS".equals(tableName)) {
			Logger.getInstance().info("Команда бекапирования логов работает только с таблицами SYNC_LOGS");
			return true;
		} else 
			return false;
	}
	
	

}
