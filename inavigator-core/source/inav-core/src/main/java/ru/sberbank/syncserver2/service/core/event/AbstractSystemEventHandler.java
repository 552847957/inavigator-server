package ru.sberbank.syncserver2.service.core.event;

import ru.sberbank.syncserver2.service.core.AbstractService;

public abstract class AbstractSystemEventHandler extends AbstractService{
	
	public abstract BaseEventPerformResult performEvent(AbstractEventInfo eventInfo);

	@Override
	protected void doStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doStart() {
		// TODO Auto-generated method stub
		super.doStart();
	}
	
	@Override
	protected void waitUntilStopped() {
		// TODO Auto-generated method stub
		
	}	
	
}
