package ru.sberbank.adminconsole.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import ru.sberbank.adminconsole.gui.datamodel.ServiceTreeModel;
import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.gui.services.SwingRemoteRequestSender;
import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.syncserver2.service.pub.xml.LogsTags;
import ru.sberbank.syncserver2.service.pub.xml.Services;
import ru.sberbank.syncserver2.service.pub.xml.Services.ServiceCommand;

public class ServiceCommandAction extends AbstractAction{
	private ServiceTreeModel treeModel;
	
	public ServiceCommandAction(ServiceTreeModel treeModel) {
		super();
		this.treeModel = treeModel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {		
		treeModel.clear();
		treeModel.fireTreeStructureChanged();
		for (Application application: ClientDataManager.getInstance().getSelectedServers()) {			
			SwingRemoteRequestSender<Services> task = new SwingRemoteRequestSender<Services>(
					application, new Services(application.getId(),ServiceCommand.GET), treeModel);
			task.startTask();
		}
		
	}
	

}
