package ru.sberbank.syncserver2.service.core.event;

import org.apache.log4j.Logger;

public class ThrowExceptionSystemEventHandler extends AbstractSystemEventHandler {
	Logger logger = Logger.getLogger(ThrowExceptionSystemEventHandler.class);
	
	@Override
	public BaseEventPerformResult performEvent(AbstractEventInfo eventInfo) {
		logger.error("System event handler error", new NotFoundEventHandlerException(((eventInfo!= null)?eventInfo.toString():"")));
		return new BaseEventPerformResult(true);
	}


	
}
