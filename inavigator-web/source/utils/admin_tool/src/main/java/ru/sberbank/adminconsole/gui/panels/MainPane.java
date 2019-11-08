package ru.sberbank.adminconsole.gui.panels;

import java.awt.GridLayout;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.sberbank.adminconsole.gui.listeners.AppSelectionListener;
import ru.sberbank.adminconsole.gui.modules.AbstractSwingModule;
import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.model.configuration.Module;

public class MainPane extends JPanel {
	
	Logger logger = LoggerFactory.getLogger(MainPane.class);

	public MainPane() {
		super(new GridLayout(1,0));

		ServersTreePane treePane = new ServersTreePane();		
		ClientDataManager.getInstance().setTree(treePane);
		
		JTabbedPane tabbedPane = new JTabbedPane();
		Collection<AbstractSwingModule> systemModules = ClientDataManager.getInstance().getSystemModules();
		for(AbstractSwingModule module:systemModules) {
			module.init();
			tabbedPane.add(module.getModuleInfo().getTitle(),module.getModulePanel());
			AppSelectionListener.addModule(module);
		}
		ClientDataManager.getInstance().setTabbedPane(tabbedPane);		
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePane, tabbedPane);
		splitPane.setDividerLocation(150);
		splitPane.setResizeWeight(0.3);
		add(splitPane);
	}

}
