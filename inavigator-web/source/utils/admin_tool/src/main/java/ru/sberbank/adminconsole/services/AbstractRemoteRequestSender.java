package ru.sberbank.adminconsole.services;

import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.syncserver2.service.pub.xml.Message;

public abstract class AbstractRemoteRequestSender<T extends Message> {
	protected T requestObject;
	protected Application server;	
	protected IRemoteDataReceiver<T> receiver;
	
	public AbstractRemoteRequestSender(T requestObject, Application server,
			IRemoteDataReceiver<T> receiver) {
		super();
		this.requestObject = requestObject;
		this.server = server;
		this.receiver = receiver;
	}
	
	abstract public T doRequest() throws Exception;
	abstract public void done(T responseObject);

	public T getRequestObject() {
		return requestObject;
	}

	public Application getServer() {
		return server;
	}

	public IRemoteDataReceiver<T> getReceiver() {
		return receiver;
	}
}
