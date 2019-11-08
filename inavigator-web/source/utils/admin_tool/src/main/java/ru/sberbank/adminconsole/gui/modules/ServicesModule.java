package ru.sberbank.adminconsole.gui.modules;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.EventObject;
import java.util.GregorianCalendar;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import ru.sberbank.adminconsole.gui.actions.ServiceCommandAction;
import ru.sberbank.adminconsole.gui.datamodel.ServiceTreeModel;
import ru.sberbank.adminconsole.gui.panels.ServicesTreePane;
import ru.sberbank.adminconsole.model.configuration.Module;


public class ServicesModule extends AbstractSwingModule {

	private JPanel modulePanel = null;
	private ServicesTreePane treePanel;
	private ServiceTreeModel treeModel;
	private AbstractAction action;
	
	public ServicesModule(Module moduleInfo) {
		super(moduleInfo);
	}

	@Override
	public void init() {
		treeModel = new ServiceTreeModel();
		treePanel = new ServicesTreePane(treeModel);
		modulePanel = new JPanel(new BorderLayout());
		action = new ServiceCommandAction(treeModel);
		
		JButton refresh = new JButton(action);		
		refresh.setText("обновить");
		JPanel topPanel = new JPanel();
		topPanel.add(refresh);	
		JScrollPane scrollPane = new JScrollPane(treePanel);
        scrollPane.setPreferredSize(new Dimension(200, 200));
		modulePanel.add(topPanel, BorderLayout.PAGE_START);
		modulePanel.add(scrollPane, BorderLayout.CENTER);		
		
	}

	@Override
	public JPanel getModulePanel() {
		return modulePanel;
	}

	@Override
	public void userHasChangedAppSelection() {
		action.actionPerformed(new ActionEvent(this, 0, ""));		
	}	

}
