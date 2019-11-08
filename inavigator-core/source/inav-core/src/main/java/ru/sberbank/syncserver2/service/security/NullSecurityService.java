package ru.sberbank.syncserver2.service.security;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.log.LogEventType;

/**
 * Created by sbt-kozhinsky-lb on 04.03.14.
 */
public class NullSecurityService extends AbstractService implements SecurityService {
    @Override
    public boolean isAllowedToUseApp(String app, String userEmail, String deviceId) {
        return true;
    }

    @Override
    public boolean isAllowedToDownloadFile(String app, String fileName, String userEmail, String deviceId) {
        return true;
    }

	/* (non-Javadoc)
	 * @see ru.sberbank.syncserver2.service.core.AbstractService#doStop()
	 */
	@Override
	protected void doStop() {
		logServiceMessage(LogEventType.SERV_STOP, "stopping service");
    	logServiceMessage(LogEventType.SERV_STOP, "stopped service");;
	}

    @Override
    protected void waitUntilStopped() {
    }

}
