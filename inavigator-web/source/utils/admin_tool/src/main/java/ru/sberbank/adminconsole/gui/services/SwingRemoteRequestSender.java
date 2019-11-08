package ru.sberbank.adminconsole.gui.services;

import java.util.HashMap;
import java.util.Map;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.adminconsole.model.configuration.Application.Status;
import ru.sberbank.adminconsole.services.AbstractRemoteRequestSender;
import ru.sberbank.adminconsole.services.IRemoteDataReceiver;
import ru.sberbank.adminconsole.services.impl.XmlRemoteRequestSenderImpl;
import ru.sberbank.syncserver2.service.pub.xml.Message;

public class SwingRemoteRequestSender<T extends Message> extends SwingWorker<T, Void> {
	
	private static Logger logger = LoggerFactory.getLogger(SwingRemoteRequestSender.class);
	
	// Кеш запущенных ранее заданий
	//public static Map<String,SwingRemoteRequestSender> startedTasks = new HashMap<String,SwingRemoteRequestSender>();
	
	private AbstractRemoteRequestSender<T> requestSender;
	
	public SwingRemoteRequestSender(Application server,T requestObject, IRemoteDataReceiver<T> receiver) {
		requestSender = new XmlRemoteRequestSenderImpl<T>(requestObject,server,receiver);
	}	
	
	@Override
	protected T doInBackground() throws Exception {
		T result = requestSender.doRequest();
		return result;
	}

	@Override
	protected void done() {
		try {			
			requestSender.done(get());		
		} catch (Exception ex) {
			logger.error("Getting request result", ex);
		}
	}
	
	public void startTask() {		
		requestSender.getServer().setStatus(Status.UNDEFINED);		
		this.execute();
	}
	
}