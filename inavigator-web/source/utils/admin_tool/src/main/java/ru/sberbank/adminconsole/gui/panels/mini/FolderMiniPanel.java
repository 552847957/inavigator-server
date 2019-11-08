package ru.sberbank.adminconsole.gui.panels.mini;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;

import ru.sberbank.adminconsole.gui.listeners.LinkServiceAndLogsMouseListener;
import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.syncserver2.service.pub.xml.Services.Service;

public class FolderMiniPanel extends MiniPanel {
	
	private List<MiniPanel> children = new LinkedList<MiniPanel>();
	private Set<String> setOfNames = new HashSet<String>();

	public FolderMiniPanel(String name) {
		super(name);		
		Action a = new AbstractAction() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				click(e);				
			}
		};
		addButtons(a);
	}
	
	public void addChild(MiniPanel panel) {
		children.add(panel);
	}
	
	public int getChildCount() { 
		return children.size();
	}
	
	public MiniPanel getChildAt(int index) {
		return children.get(index);
	}
	
	public int getIndexOfChild(MiniPanel child) {
		return children.indexOf(child);
	}
	
	public MiniPanel getChild(String name) {
		if (setOfNames.contains(name)) {
			for (MiniPanel panel: children) {
				if (panel.getName().equals(name)) 
					return panel;
			}
		}
		FolderMiniPanel newPanel = new FolderMiniPanel(name);
		setOfNames.add(name);
		children.add(newPanel);
		return newPanel;
	}
	
	public ServiceMiniPanel createOrUpdateService(Integer app, Service s) {	
		for (MiniPanel panel: children) {
			ServiceMiniPanel servicePanel = (ServiceMiniPanel) panel;
			if (servicePanel.getApplicationId().equals(app)) {
				servicePanel.setState(s.getState());
				return servicePanel;
			}
		}
		ServiceMiniPanel newPanel = new ServiceMiniPanel(app, s);
		children.add(newPanel);
		return newPanel;
	}
	
	@Override
	public void click(ActionEvent e) {
		for (MiniPanel child: children) {
			child.click(e);
		}
		
	}

}
