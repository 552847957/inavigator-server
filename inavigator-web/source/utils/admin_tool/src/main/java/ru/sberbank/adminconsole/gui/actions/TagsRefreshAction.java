package ru.sberbank.adminconsole.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.gui.services.SwingRemoteRequestSender;
import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.adminconsole.model.configuration.Application.Status;
import ru.sberbank.syncserver2.service.pub.xml.LogsTags;

public class TagsRefreshAction extends AbstractAction {	
	

	@Override
	public void actionPerformed(ActionEvent e) {		
		ClientDataManager manager = ClientDataManager.getInstance();
		manager.getTagsModel().removeAllElements();
		manager.getTable().clear();
		for (Application server: manager.getSelectedServers()) {
			
			SwingRemoteRequestSender<LogsTags> task = new SwingRemoteRequestSender<LogsTags>(server, new LogsTags(), manager.getTagsModel());
			task.startTask();
		}	
		
	}	

}
