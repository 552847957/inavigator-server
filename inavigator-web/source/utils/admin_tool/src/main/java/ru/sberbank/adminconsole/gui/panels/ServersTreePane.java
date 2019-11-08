package ru.sberbank.adminconsole.gui.panels;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import ru.sberbank.adminconsole.gui.listeners.AppSelectionListener;
import ru.sberbank.adminconsole.gui.modules.AbstractSwingModule;
import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.model.configuration.Application;

public class ServersTreePane extends JPanel {
	
	private JTree tree;

	public ServersTreePane() {
		super(new GridLayout(1,0));
		this.setOpaque(true);
		
		DefaultMutableTreeNode mainNode = new DefaultMutableTreeNode("servers");		
		createNodes(mainNode);
		tree = new JTree(mainNode);		
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);

		tree.setCellRenderer(new ServerStatusCellRenderer());
		tree.setSelectionModel(new ServerSelectionModel());
		tree.getSelectionModel().addTreeSelectionListener(AppSelectionListener.getListener());
		tree.setRootVisible(false);
		tree.setShowsRootHandles(true);
		int rows = tree.getRowCount();
		for (int i=rows; i>=0; i--) {
			tree.expandRow(i);
		}
		ToolTipManager.sharedInstance().registerComponent(tree);

		JScrollPane view = new JScrollPane(tree);
		view.setPreferredSize(new Dimension(430,400));
		add(view);		
	}
	
	private void createNodes(DefaultMutableTreeNode top) {
		DefaultMutableTreeNode category = null;
        
		ClientDataManager manager = ClientDataManager.getInstance();
		for (String s: manager.getCategories()) {
			if (s==null || s=="") {
				for (Application server: manager.getServers(s)) {
					top.add(new DefaultMutableTreeNode(server));
				}
			} else {
				category = new DefaultMutableTreeNode(s);
				top.add(category);
				for (Application server: manager.getServers(s)) {
					category.add(new DefaultMutableTreeNode(server));					
				}
			}
		}
		
	}

	public List<Application> getSelectedServers() {
		List<Application> selected = new ArrayList<Application>(tree.getSelectionCount()); 
		DefaultMutableTreeNode node;
		TreePath[] paths = tree.getSelectionPaths();		
		if (paths!=null)
			for (TreePath path: paths) {
				node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (node.isLeaf()) {
					selected.add((Application)node.getUserObject());
				}				
			}
		return selected;
	}
	
	public void setSelectedApplication(Application app) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getModel().getRoot();
		Enumeration<TreeNode> e = node.depthFirstEnumeration();
		TreePath path;
		while (e.hasMoreElements())
			if ((node=(DefaultMutableTreeNode)e.nextElement()).isLeaf() && node.getUserObject()==app) {
				path = new TreePath(node.getPath());
				tree.getSelectionModel().setSelectionPath(path);
				return;
			}
	}
	
	public int getSelectionCount() {
		return tree.getSelectionCount();
	}
	
	public void unselectAll() {
		tree.setSelectionPaths(new TreePath[0]);
	}

	public void updateIcons() {
		tree.repaint();
	}
	
	private class ServerStatusCellRenderer extends DefaultTreeCellRenderer {
		
		Icon fail = new ImageIcon(ServersTreePane.class.getResource("/images/circle_red.png"));
		Icon ok = new ImageIcon(ServersTreePane.class.getResource("/images/circle_green.png"));
		Icon undefined = new ImageIcon(ServersTreePane.class.getResource("/images/circle_yellow.png"));	
		Icon accessDenied = new ImageIcon(ServersTreePane.class.getResource("/images/key.png"));
		
		public ServerStatusCellRenderer() {
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row,
				boolean hasFocus) {			
			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf,row, hasFocus);
			if (leaf) {
				
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				setToolTipText(((Application)node.getUserObject()).getURL());
				switch (((Application)node.getUserObject()).getStatus()) {
					case UNDEFINED: 
						setIcon(undefined); break;
					case OK: 
						setIcon(ok); break;
					case FAIL: 
						setIcon(fail); break;
					case ACCESS_DENIED:
						setIcon(accessDenied); break;
				}
			} else {
				setIcon(null);
			}
			return this;
		}
		
	}
	
	private class ServerSelectionModel extends DefaultTreeSelectionModel {
		private boolean canBeAdded(TreePath path) {
			return ((DefaultMutableTreeNode) path.getLastPathComponent()).isLeaf();
		}
		
		private TreePath[] getFilteredPaths(TreePath[] paths) {
			List<TreePath> filtered = new ArrayList<TreePath>(paths.length);
			for (TreePath path: paths) {
				if (canBeAdded(path))
					filtered.add(path);
			}
			return filtered.toArray(new TreePath[filtered.size()]);
		}

		@Override
		public void addSelectionPath(TreePath path) {
			if (canBeAdded(path))
				super.addSelectionPath(path);
		}

		@Override
		public void addSelectionPaths(TreePath[] paths) {
			paths = getFilteredPaths(paths);
			super.addSelectionPaths(paths);
		}

		@Override
		public void setSelectionPath(TreePath path) {
			if (canBeAdded(path))
				super.setSelectionPath(path);
		}

		@Override
		public void setSelectionPaths(TreePath[] pPaths) {
			pPaths = getFilteredPaths(pPaths);
			super.setSelectionPaths(pPaths);
		}
		
		
		
	}

}
