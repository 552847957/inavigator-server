package ru.sberbank.adminconsole.gui.modules;

import java.util.Collection;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.adminconsole.model.configuration.Module;

public abstract class AbstractSwingModule {
	Logger logger = LoggerFactory.getLogger(AbstractSwingModule.class);
	
	private Module moduleInfo;
	
	public AbstractSwingModule(Module moduleInfo) {
		this.moduleInfo = moduleInfo;
	}
	
	public Module getModuleInfo() {
		return moduleInfo;
	}
	
	public abstract void init();
	public abstract JPanel getModulePanel();
	public abstract void userHasChangedAppSelection();
	
	public void sendMessage(String messageCode,Object ... eventParams) {
		logger.warn("No message handler for message with code " + messageCode);
	}
}
