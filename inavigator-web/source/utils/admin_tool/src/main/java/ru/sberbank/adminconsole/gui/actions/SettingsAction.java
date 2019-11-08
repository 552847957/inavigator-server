package ru.sberbank.adminconsole.gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import ru.sberbank.adminconsole.gui.datamodel.SettingsModel;
import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.gui.services.SwingRemoteRequestSender;
import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.syncserver2.service.pub.xml.Settings;
import ru.sberbank.syncserver2.service.pub.xml.Settings.SettingCommand;

public class SettingsAction extends AbstractAction {
	private SettingsModel settingsModel;
	

	public SettingsAction(SettingsModel settingsModel) {
		super();
		this.settingsModel = settingsModel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		ClientDataManager manager = ClientDataManager.getInstance();
		if (manager.getApplicationsSelectionCount()!=1) {
			JOptionPane.showMessageDialog(null, "Выберете 1 приложение", "Warning", JOptionPane.WARNING_MESSAGE);
			manager.unselectAllApplications();
			return;
		}
		Application application = manager.getSelectedServers().get(0);
		Settings settings = new Settings();
		if (e.getActionCommand().equals("GET")) {
			settings.setCommand(SettingCommand.GET);
			settingsModel.clear();
		} else if (e.getActionCommand().equals("SET")) {
			settings.setCommand(SettingCommand.SET);
			settings.setSettings(settingsModel.getSettings());
			
		}		
		
		SwingRemoteRequestSender<Settings> task = new SwingRemoteRequestSender<Settings>(
				application, settings, settingsModel);
		task.startTask();		
	}
	

}
