package ru.sberbank.adminconsole.gui.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.AbstractCellEditor;
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
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import ru.sberbank.adminconsole.gui.datamodel.ServiceTreeModel;
import ru.sberbank.adminconsole.gui.panels.mini.MiniPanel;
import ru.sberbank.adminconsole.services.IRemoteDataReceiver;
import ru.sberbank.syncserver2.service.pub.xml.Services;
import ru.sberbank.syncserver2.service.pub.xml.Services.Service;


public class ServicesTreePane extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JTree tree;
		

	public ServicesTreePane(ServiceTreeModel model) {
		super(new GridLayout(1,0));
		this.setOpaque(true);			
				
		tree = new JTree(model);
		model.setTree(tree);
		tree.setEditable(true);
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		MyPanelRender render = new MyPanelRender();
		tree.setCellRenderer(render);
		tree.setCellEditor(new MyPanelEditor(tree,render));
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		tree.setRowHeight(30);

		JScrollPane view = new JScrollPane(tree);
		view.setPreferredSize(new Dimension(400,400));
		add(view);
	}
	


	
	private class MyPanelEditor extends DefaultTreeCellEditor {
		
		public MyPanelEditor(JTree tree, DefaultTreeCellRenderer renderer) {
			super(tree, renderer);
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value,
				boolean isSelected, boolean expanded, boolean leaf, int row) {			
			MiniPanel panel = (MiniPanel) value;
			return panel;            
		}

		@Override
		public boolean isCellEditable(EventObject event) {
			return true;
		}	
	}

	
	private class MyPanelRender extends DefaultTreeCellRenderer {		

		public MyPanelRender() {
			super();
			setIcon(null);
			
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {			
			
            if (value instanceof MiniPanel) {
            	MiniPanel panel = (MiniPanel) value;
                return panel;
            }            
            return super.getTreeCellRendererComponent(tree, value, false, expanded, leaf, row, false);
		}		
	}

}
