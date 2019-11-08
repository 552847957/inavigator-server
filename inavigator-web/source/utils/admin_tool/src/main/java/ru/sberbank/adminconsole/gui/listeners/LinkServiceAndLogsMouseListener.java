package ru.sberbank.adminconsole.gui.listeners;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

import ru.sberbank.adminconsole.gui.modules.AbstractSwingModule;
import ru.sberbank.adminconsole.gui.panels.mini.FolderMiniPanel;
import ru.sberbank.adminconsole.gui.panels.mini.ServiceMiniPanel;
import ru.sberbank.adminconsole.gui.services.ClientDataManager;

public class LinkServiceAndLogsMouseListener implements MouseListener{

	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount()>1) {
			ClientDataManager manager = ClientDataManager.getInstance();
			AbstractSwingModule module = manager.getModuleByCode("LOGS");
			JLabel label = (JLabel) e.getSource();
			Object parent = label.getParent();
			if (parent instanceof ServiceMiniPanel) {
				
				manager.getTabbedPane().setSelectedComponent(module.getModulePanel());
				ServiceMiniPanel servicePanel = (ServiceMiniPanel) parent;
				module.sendMessage(null, servicePanel.getService().getCode(),servicePanel.getApplicationId());	
				
			} else if (parent instanceof FolderMiniPanel) {
				
				FolderMiniPanel folder = (FolderMiniPanel) label.getParent();
				if (folder.getChildAt(0) instanceof ServiceMiniPanel) {
					manager.getTabbedPane().setSelectedComponent(module.getModulePanel());
					ServiceMiniPanel servicePanel = (ServiceMiniPanel) folder.getChildAt(0);
					module.sendMessage(null, servicePanel.getService().getCode(),servicePanel.getApplicationId());
				}
			}						
		}		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}	

}
