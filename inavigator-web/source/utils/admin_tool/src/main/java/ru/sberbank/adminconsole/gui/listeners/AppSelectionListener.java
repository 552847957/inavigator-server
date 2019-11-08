package ru.sberbank.adminconsole.gui.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import ru.sberbank.adminconsole.gui.modules.AbstractSwingModule;
import ru.sberbank.adminconsole.gui.services.ClientDataManager;

public class AppSelectionListener {
	private static Map<JPanel,AbstractSwingModule> modules = new HashMap<JPanel, AbstractSwingModule>();
	private static TreeSelectionListener listener = new TreeSelectionListener() {		
		@Override
		public void valueChanged(TreeSelectionEvent e) {
			ClientDataManager manager = ClientDataManager.getInstance();
			AbstractSwingModule module = modules.get(manager.getTabbedPane().getSelectedComponent());
			if (module!=null) {
				module.userHasChangedAppSelection();
			}
			
		}
	};	
	public static TreeSelectionListener getListener() {
		return listener;
	}
	public static void addModule(AbstractSwingModule module) {
		modules.put(module.getModulePanel(), module);
	}

}
