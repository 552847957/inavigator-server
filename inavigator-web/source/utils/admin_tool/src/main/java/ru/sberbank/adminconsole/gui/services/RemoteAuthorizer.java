package ru.sberbank.adminconsole.gui.services;

import javax.swing.SwingWorker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.sberbank.adminconsole.gui.services.AuthorizationService.AccessChecker;
import ru.sberbank.adminconsole.model.configuration.Application;
import ru.sberbank.adminconsole.services.AbstractRemoteRequestSender;
import ru.sberbank.adminconsole.services.IRemoteDataReceiver;
import ru.sberbank.adminconsole.services.impl.XmlRemoteRequestSenderImpl;
import ru.sberbank.syncserver2.service.pub.xml.Authentication;
import ru.sberbank.syncserver2.service.pub.xml.Message.Status;

public class RemoteAuthorizer extends SwingWorker<Boolean, Void>{
	private static Logger logger = LoggerFactory.getLogger(RemoteAuthorizer.class);
	private Authentication requestObject;
	private IRemoteDataReceiver<Boolean> handler;
	private volatile String msg="no available applications";
	
	public RemoteAuthorizer(IRemoteDataReceiver<Boolean> handler, Authentication auth) {
		requestObject = auth;
		this.handler = handler;
	}
	public static void authenticate(IRemoteDataReceiver<Boolean> handler, Authentication auth) {
		RemoteAuthorizer authorizer = new RemoteAuthorizer(handler, auth);
		authorizer.execute();
	}

	@Override
	protected Boolean doInBackground() throws Exception {
		boolean access = false;
		AbstractRemoteRequestSender<Authentication> sender = null;
	
		for (Application application: ClientDataManager.getInstance().getAllApplications()) {			
			sender = new XmlRemoteRequestSenderImpl<Authentication>(requestObject,application,null);			
			access = access | sender.doRequest().getCode()==Status.OK;
			sender.done(sender.getRequestObject());	
			String s =sender.getRequestObject().getNoteMessage();
			if (s!=null && s!="") msg = s;
		}		
		return access;
	}
	
	@Override
	protected void done() {
		try {
			try {
				AccessChecker checker = (AccessChecker) handler;
				checker.setMsg(msg);
			} catch (ClassCastException e) {}
			handler.submit(get());
		} catch (Exception e) {
			logger.error("Getting request result",e);
		}
	}

}
