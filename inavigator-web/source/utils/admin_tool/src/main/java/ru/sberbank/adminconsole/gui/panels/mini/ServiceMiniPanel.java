package ru.sberbank.adminconsole.gui.panels.mini;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JSeparator;

import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.gui.services.SwingRemoteRequestSender;
import ru.sberbank.syncserver2.service.core.ServiceState;
import ru.sberbank.syncserver2.service.pub.xml.Services;
import ru.sberbank.syncserver2.service.pub.xml.Services.ServiceCommand;
import ru.sberbank.syncserver2.service.pub.xml.Services.Service;

public class ServiceMiniPanel extends MiniPanel{
	private JLabel state; 
	private Integer applicationId;
	private Service service;

	public ServiceMiniPanel(Integer appId, Service s) {
		super(ClientDataManager.getInstance().getApplicationById(appId).getName());		
		applicationId = appId;
		service = s;
		
		state = new JLabel();
		state.setOpaque(true);
		setState(s.getState());
		
		add(new JSeparator(JSeparator.VERTICAL));
		add(state);
		add(new JSeparator(JSeparator.VERTICAL));
		Action a = new AbstractAction() {					
			@Override
			public void actionPerformed(ActionEvent e) {	
				ClientDataManager manager = ClientDataManager.getInstance();
				ServiceCommand command = ServiceCommand.valueOf(e.getActionCommand());
				Services send = new Services(applicationId, command);
				send.setServices(Arrays.asList(service));
				
				SwingRemoteRequestSender<Services> task = new SwingRemoteRequestSender<Services>(
						manager.getApplicationById(applicationId), send, manager.getServiceModel());
				task.startTask();				
			}
		};
		addButtons(a);		
	}
	
	public Service getService() {
		return service;
	}
	
	public void setState(ServiceState s) {
		switch (s) {
		case STARTED:
			state.setBackground(Color.GREEN);
			break;
		case STOPPED:
			state.setBackground(Color.RED);
			break;
		default:
			state.setBackground(Color.YELLOW);		
		}
		state.setText(s.toString());
		service.setState(s);
	}

	public Integer getApplicationId() {
		return applicationId;
	}

}
