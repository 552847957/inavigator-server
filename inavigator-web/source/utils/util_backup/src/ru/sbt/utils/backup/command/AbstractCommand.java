package ru.sbt.utils.backup.command;

import ru.sbt.utils.backup.configuration.XmlConfiguration;

public abstract class AbstractCommand {
	
	protected XmlConfiguration configuration;
	
	public XmlConfiguration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(XmlConfiguration configuration) {
		this.configuration = configuration;
	}

	public AbstractCommand(XmlConfiguration configuration) {
		this.configuration = configuration;
	}
	
	/**
	 * Выполнить команду
	 */
	public abstract int execute(String args[]);
	
	/**
	 * Имя команды
	 * @return
	 */
	public abstract String getCommandName();
}
