package ru.sberbank.adminconsole.gui.modules;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;

import com.toedter.calendar.JDateChooser;

import ru.sberbank.adminconsole.gui.actions.SearchAction;
import ru.sberbank.adminconsole.gui.actions.TagsRefreshAction;
import ru.sberbank.adminconsole.gui.datamodel.LogsTableModel;
import ru.sberbank.adminconsole.gui.datamodel.TagsComboBoxModel;
import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.model.configuration.Module;

public class LogsModule extends AbstractSwingModule {

	private JPanel modulePanel = null;
	private SearchAction searchAction;
	private TagsRefreshAction tagsRefreshAction;
	
	public LogsModule(Module moduleInfo) {
		super(moduleInfo);
	}

	@Override
	public void init() {
		modulePanel = new JPanel(new GridLayout(1, 0)); 
		
		LogsTableModel tableModel = new LogsTableModel();
		ClientDataManager.getInstance().setTable(tableModel);		
		JTable table = new JTable(tableModel);
		table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
		JScrollPane scrollTable = new JScrollPane(table);
		
		JPanel searchPanel = searchPanel();	
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, searchPanel, scrollTable);
		modulePanel.add(splitPane);
		
	}

	@Override
	public JPanel getModulePanel() {
		// TODO Auto-generated method stub
		return modulePanel;
	}

	private JPanel searchPanel() {		
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.insets = new Insets(5, 2, 5, 2);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;		
		panel.add(new JLabel("Выберите тег: "), c);
		
		c.gridx = 2;
		c.gridwidth =2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.3;
		TagsComboBoxModel comboBoxModel = new TagsComboBoxModel();
		ClientDataManager.getInstance().setTagsModel(comboBoxModel);		
		panel.add(new JComboBox(comboBoxModel), c);		
		
		c.gridx = 4;
		c.gridwidth = 1;	
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		tagsRefreshAction = new TagsRefreshAction();
		JButton reload = new JButton(tagsRefreshAction);
		reload.setIcon(new ImageIcon(LogsModule.class.getResource("/images/refresh.png")));
		reload.setToolTipText("обновить теги");
		panel.add(reload, c);
		
		//========================			
		JTextField textForMsg = new JTextField();
		JTextField page = new JTextField();
		ClientDataManager.getInstance().getTable().setCurPage(page);		
		searchAction = new SearchAction(textForMsg, page);
		
		c.gridy = 1;
		c.gridx = 0;		
		c.gridwidth = 2;
		panel.add(new JLabel("Дата с: "),c);
		
		c.gridx = 2;		
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.3;
		JDateChooser calendar = new JDateChooser();
		calendar.setDateFormatString("dd.MM.yyyy HH:mm");
		calendar.getJCalendar().setNullDateButtonVisible(true);
		calendar.addPropertyChangeListener(searchAction.listnerForDateFrom());		
		panel.add(calendar,c);
		
		c.gridx = 5;		
		c.gridwidth = 2;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		panel.add(new JLabel("Дата по: "),c);
		
		c.gridx = 7;		
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.3;
		calendar = new JDateChooser();
		calendar.setDateFormatString("dd.MM.yyyy HH:mm");
		calendar.getJCalendar().setNullDateButtonVisible(true);
		calendar.addPropertyChangeListener(searchAction.listnerForDateTo());		
		panel.add(calendar,c);
		//========================
		
		c.gridy = 2;
		c.gridx = 0;		
		c.gridwidth = 2;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		panel.add(new JLabel("Поиск в сообщении: "),c);
		
		c.gridx = 2;		
		c.gridwidth = 2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 0.3;
		panel.add(textForMsg,c);		

		c.gridy = 3;
		c.gridx = 1;		
		c.gridwidth = 3;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0;
		panel.add(getPaging(searchAction, page), c);
		
		c.gridy = 3;
		c.gridx = 6;		
		c.gridwidth = 1;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0;
		JButton button = new JButton(searchAction);
		button.setText("Поиск");
		panel.add(button,c);
		
		return panel;		
	}
	
	private JPanel getPaging(Action action, JTextField page) {
		JPanel panel = new JPanel(new GridLayout(1,0));
		JButton left = new JButton(action);
		left.setIcon(new ImageIcon(LogsModule.class.getResource("/images/back.png")));
		left.setActionCommand("BACK");
		panel.add(left);
		
		page.setHorizontalAlignment(JTextField.TRAILING);
		panel.add(page);
		
		JLabel label = new JLabel("/");
		label.setHorizontalAlignment(JLabel.CENTER);
		panel.add(label);
		
		JLabel maxPage = new JLabel("0");
		maxPage.setHorizontalAlignment(JLabel.LEADING);
		ClientDataManager.getInstance().getTable().setMaxPageLabel(maxPage);		
		panel.add(maxPage);
		
		JButton forward = new JButton(action);
		forward.setIcon(new ImageIcon(LogsModule.class.getResource("/images/forward.png")));
		forward.setActionCommand("FORWARD");
		panel.add(forward);
		
		return panel;
	}
		

	@Override
	public void sendMessage(String messageCode, Object... eventParams) {
		//временная реализация
		ClientDataManager manager = ClientDataManager.getInstance();
		manager.setSelectedApplication(manager.getApplicationById((Integer)eventParams[1]));
		manager.getTagsModel().setSelectedItem(eventParams[0]);
		searchAction.actionPerformed(new ActionEvent(this,0,"NEW"));
	}

	@Override
	public void userHasChangedAppSelection() {
		tagsRefreshAction.actionPerformed(new ActionEvent(this,0,""));
	}
	
	
	
	
	
}
