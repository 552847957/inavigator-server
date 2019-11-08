package ru.sberbank.adminconsole.gui.datamodel;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ru.sberbank.adminconsole.services.IRemoteDataReceiver;
import ru.sberbank.syncserver2.service.pub.xml.Message;
import ru.sberbank.syncserver2.service.pub.xml.Settings;
import ru.sberbank.syncserver2.service.pub.xml.Settings.*;

public class SettingsModel implements IRemoteDataReceiver<Settings> {
	
	private JPanel settingsPanel;
	private List<SettingPanel> settingsHolder = new ArrayList<SettingPanel>();
	private int row = 0;
	private GridBagConstraints constraints = new GridBagConstraints();
	

	public SettingsModel(JPanel settingsPanel) {
		super();
		this.settingsPanel = settingsPanel;
		constraints.insets = new Insets(5, 2, 5, 2);
	}

	@Override
	public void submit(Settings xml) {
		if (xml.getCode()!=Message.Status.OK)
			return;
		if (xml.getCommand()==SettingCommand.GET) {			
			settingsHolder = new ArrayList<SettingPanel>(xml.getSettings().size());
			for (Setting s: xml.getSettings()) {
				SettingPanel panel = createSettingPanel(s);
				//settingsPanel.add(panel);
				settingsHolder.add(panel);
			}
			settingsPanel.updateUI();
		}
		
	}
	
	public void clear() {
		settingsHolder.clear();
		settingsPanel.removeAll();
		row = 0;
		settingsPanel.updateUI();
	}

	private SettingPanel createSettingPanel(Setting s) {
		SettingPanel panel = new SettingPanel(s.getName(),s.getValue(),s.getComment(), row++);
		return panel;		
	}
	
	public List<Setting> getSettings() {
		List<Setting> list = new ArrayList<Settings.Setting>(settingsHolder.size());
		for (SettingPanel panel: settingsHolder) {
			list.add(new Setting(panel.getKey(),panel.getValue(),""));
		}
		return list;
	}
	
	private class SettingPanel {
		private JLabel key;
		private JTextField value;
		private String valueStr;
		private JLabel desc;
		
		public SettingPanel(String name, String value, String desc, int n) {
			this.key = new JLabel(name);
			this.valueStr = value;
			if (name.toLowerCase().contains("password"))
				value = "***";
			this.value = new JTextField(value);
			this.desc = new JLabel("<html>"+desc+"</html>");
						
			constraints.gridy = n;
			constraints.gridx = 0;			
			constraints.gridwidth = 2;
			constraints.weightx = 0;
			constraints.fill = GridBagConstraints.NONE;
			settingsPanel.add(this.key,constraints);
			constraints.gridx = 2;
			constraints.gridwidth = 2;
			constraints.weightx = 1;
			constraints.fill = GridBagConstraints.HORIZONTAL;
			settingsPanel.add(this.value,constraints);
			constraints.gridx = 4;
			constraints.gridwidth = 1;
			constraints.weighty = 1;
			constraints.weightx = 0.4;
			constraints.fill = GridBagConstraints.BOTH;			
			settingsPanel.add(this.desc,constraints);			
		}
		public String getKey() {
			return key.getText();
		}
		public String getValue() {
			return value.getText().trim().equals("***")?valueStr:value.getText();
		}
		public String getDesc() {
			return desc.getText();
		}	
	}
}
