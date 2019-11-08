package ru.sberbank.adminconsole.gui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.gui.services.SwingRemoteRequestSender;
import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.syncserver2.service.pub.xml.LogMessages;

public class SearchAction extends AbstractAction {
	
	private OneDateListener dateFrom = new OneDateListener();
	private OneDateListener dateTo = new OneDateListener();
	
	private JTextField forMsg; 
	private JTextField page;
	
	public SearchAction(JTextField msg, JTextField page) {
		forMsg = msg;
		this.page = page;
		page.setText("1");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Integer page;
		if (e.getActionCommand()=="NEW") {
			page = 1;
		} else {
			try {
				page = Integer.valueOf(this.page.getText());
				if (page<1)  {
					page = 1;
				}				
			} catch (NumberFormatException ne) {
				page = 1;	
			}
			if (e.getActionCommand()=="BACK") {
				if (page==1) {
					this.page.setText(page.toString());
					return;
				}
				page--;
			} else if (e.getActionCommand()=="FORWARD") {
				page++;
			}	
		}
		this.page.setText(page.toString());
		ClientDataManager manager = ClientDataManager.getInstance();
		manager.getTable().clear();
		LogMessages messages = new LogMessages((String)manager.getTagsModel().getSelectedItem());
		messages.setDateFrom(dateFrom.getDate());
		messages.setDateTo(dateTo.getDate());
		messages.setSearchWithMsg(forMsg.getText());
		messages.setPage(page);		
		
		for (Application server: manager.getSelectedServers()) {
				LogMessages send = messages.clone();
				send.setSource(server.toString());
				SwingRemoteRequestSender<LogMessages> task = new SwingRemoteRequestSender<LogMessages>(server, send , manager.getTable());
				task.startTask();
		}			
	}
	
	public PropertyChangeListener listnerForDateFrom() {
		return dateFrom;
	}
	
	public PropertyChangeListener listnerForDateTo() {
		return dateTo;
	}
	
	private class OneDateListener implements PropertyChangeListener {
		
		private Date date;
		
		public Date getDate() {
			return date;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {			
			if (evt.getNewValue()!=null && evt.getNewValue().getClass()!=Date.class)
				return;
			date = (Date) evt.getNewValue();
			
		}
		
	}	
}
