package ru.sbt.utils.backup.command;

import java.util.HashMap;
import java.util.Map;

import ru.sbt.utils.backup.configuration.XmlConfiguration;

public class CommandFactory {

	private static Map<String,String> registry;
	
	static {
		registry = new HashMap<String, String>();
		registry.put(new BackupCommand(null).getCommandName(),BackupCommand.class.getCanonicalName());
		registry.put(new RestoreCommand(null).getCommandName(),RestoreCommand.class.getCanonicalName());
		registry.put(new BackupLogsCommand(null).getCommandName(),BackupLogsCommand.class.getCanonicalName());
		registry.put(new RunScriptsCommand(null).getCommandName(),RunScriptsCommand.class.getCanonicalName());
	}
	
	private CommandFactory() {
		
	}
	
	public static AbstractCommand getCommand(String commandName,XmlConfiguration configuration) {
		if (commandName == null) return null;
		try {
			return (AbstractCommand)Class.forName(registry.get(commandName.toUpperCase())).getConstructor(XmlConfiguration.class).newInstance(configuration);
		} catch (Exception ex) {
			System.out.println("Can't get command " + commandName);
		}
		return null;
	}
	
}
