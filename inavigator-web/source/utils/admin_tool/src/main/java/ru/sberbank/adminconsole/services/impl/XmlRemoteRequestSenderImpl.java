package ru.sberbank.adminconsole.services.impl;

import java.awt.TrayIcon.MessageType;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.sberbank.adminconsole.gui.services.ClientDataManager;
import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.adminconsole.model.configuration.Application.Status;
import ru.sberbank.adminconsole.services.AbstractRemoteRequestSender;
import ru.sberbank.adminconsole.services.IRemoteDataReceiver;
import ru.sberbank.syncserver2.service.pub.xml.Message;

public class XmlRemoteRequestSenderImpl<T extends Message> extends AbstractRemoteRequestSender<T> {
	
	private static Logger logger = LoggerFactory.getLogger(XmlRemoteRequestSenderImpl.class);
	
	public XmlRemoteRequestSenderImpl(T requestObject,
			Application server, IRemoteDataReceiver<T> receiver) {
		super(requestObject, server, receiver);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T doRequest() throws Exception {
		URL url = new URL(server.getURL());
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Accept", "text/xml");
		connection.setRequestProperty("Content-type", "text/xml");
		if (server.getCookie() != null)
			connection.setRequestProperty("Cookie", server.getCookie());
		
		try {		
			JAXBContext jaxbContext = JAXBContext.newInstance(requestObject.getClass());
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.marshal(requestObject, connection.getOutputStream());
			//marshaller.marshal(requestObject, System.out);
			if (connection.getResponseCode()==HttpURLConnection.HTTP_OK) {
				server.setCookie(connection.getHeaderField("Set-Cookie"));
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				requestObject = (T) jaxbUnmarshaller.unmarshal(connection.getInputStream());				
			} else {
				requestObject.setCode(Message.Status.FAILED);				
				logger.error("Request to server failed [" + this.server.getURL() + "]", new Throwable("Http Code " + connection.getResponseCode()));
			}
		} catch (Exception e) {
			requestObject.setCode(Message.Status.FAILED);	
			logger.error("Request to server failed [" + this.server.getURL() + "]", e);
		}			
		return requestObject;				
	}

	@Override
	public void done(T responseObject) {
		switch (responseObject.getCode()) {
		case OK:
			server.setStatus(Status.OK);
			logger.info("Request to server "+"["+server.getURL()+"]"+" - OK");
			break;
		case FORBIDDEN:
		case UNAUTHORIZED:
			server.setStatus(Status.ACCESS_DENIED);
			logger.info("Request to server "+"["+server.getURL()+"]"+" - ACCESS ERROR");
			break;
		default:
			server.setStatus(Status.FAIL);
		}
		ClientDataManager.getInstance().updateServersStatus();
		if (receiver!=null)
			receiver.submit(responseObject);
	}
}
