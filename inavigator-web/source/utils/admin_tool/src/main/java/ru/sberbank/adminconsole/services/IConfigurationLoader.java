package ru.sberbank.adminconsole.services;

import ru.sberbank.adminconsole.model.configuration.ApplicationConfiguration;
import ru.sberbank.adminconsole.model.configuration.ModuleConfiguration;

public interface IConfigurationLoader {
	public ApplicationConfiguration readApplicationConfiguration();
	public ModuleConfiguration readModuleConfiguration();
}
