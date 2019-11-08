package ru.sberbank.adminconsole.gui.datamodel;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import ru.sberbank.adminconsole.gui.panels.mini.FolderMiniPanel;
import ru.sberbank.adminconsole.gui.panels.mini.MiniPanel;
import ru.sberbank.adminconsole.gui.panels.mini.ServiceMiniPanel;
import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.services.IRemoteDataReceiver;
import ru.sberbank.syncserver2.service.pub.xml.Message;
import ru.sberbank.syncserver2.service.pub.xml.Services;
import ru.sberbank.syncserver2.service.pub.xml.Services.Service;

public class ServiceTreeModel implements TreeModel,  IRemoteDataReceiver<Services>{
	private String root = "Services";
	private List<FolderMiniPanel> folders = new LinkedList<FolderMiniPanel>();	
	private Set<String> foldersSet = new HashSet<String>();
    private List<TreeModelListener> treeModelListeners = new LinkedList<TreeModelListener>();
    private JTree tree;
	
	
	public ServiceTreeModel() {
		ClientDataManager.getInstance().setServiceModel(this);
	}
	
	public void setTree(JTree tree) {
		this.tree = tree;
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public Object getChild(Object parent, int index) {
		if (parent==root) {
			return folders.get(index);
		}
		FolderMiniPanel folder = (FolderMiniPanel) parent;
		return folder.getChildAt(index);
	}

	@Override
	public int getChildCount(Object parent) {
		if (parent==root) {
			return folders.size();
		}
		if (parent instanceof ServiceMiniPanel)
			return 0;
		FolderMiniPanel folder = (FolderMiniPanel)parent;
		return folder.getChildCount();
	}

	@Override
	public boolean isLeaf(Object node) {
		if (node instanceof ServiceMiniPanel)
			return true;
		return false;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		if (parent==root) 
			return folders.indexOf(child);
		if (parent instanceof FolderMiniPanel) {
			FolderMiniPanel folder = (FolderMiniPanel) parent;
			return folder.getIndexOfChild((MiniPanel)child);
		}
		return -1;
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.add(l);		
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l)  {
        treeModelListeners.remove(l);
    }
	
	public void fireTreeStructureChanged() {
        TreeModelEvent e = new TreeModelEvent(this, 
                                              new Object[] {root});
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeStructureChanged(e);
        }
    }
	
	public FolderMiniPanel getFolder(String name) {
		if (foldersSet.contains(name)) {
			for (FolderMiniPanel panel: folders) 
				if (panel.getName().equals(name)) 
					return panel;
		}
		FolderMiniPanel newPanel = new FolderMiniPanel(name);
		foldersSet.add(name);
		folders.add(newPanel);
		return newPanel;
	}

	@Override
	public void submit(Services xml) {
		if (xml.getCode()!=Message.Status.OK)
			return;
		switch (xml.getCommand()) {
		case GET: 
			for (Service service: xml.getServices()) {
				FolderMiniPanel folder = getFolder(service.getFolder());	
				FolderMiniPanel group = (FolderMiniPanel) folder.getChild(service.getCode());
				group.createOrUpdateService(xml.getApplicationId(), service);
				fireTreeStructureChanged();	
			}
			break;
		default:
			Service service = xml.getServices().get(0);
			FolderMiniPanel folder = getFolder(service.getFolder());	
			FolderMiniPanel group = (FolderMiniPanel) folder.getChild(service.getCode());
			ServiceMiniPanel panel = group.createOrUpdateService(xml.getApplicationId(), service);
			Enumeration<TreePath> e = tree.getExpandedDescendants(new TreePath(new Object[] {root}));
			fireTreeStructureChanged();	
			while (e.hasMoreElements()) {
				tree.expandPath(e.nextElement());				
			}
			
		}		
		
	}
	
	public void clear() {
		folders.clear();
		foldersSet.clear();
	}

}
