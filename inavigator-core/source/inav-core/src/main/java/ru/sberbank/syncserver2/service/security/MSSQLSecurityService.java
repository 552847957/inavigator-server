package ru.sberbank.syncserver2.service.security;

import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.sql.MSSQLService;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;

/**
 * Created by sbt-kozhinsky-lb on 04.03.14.
 */
public class MSSQLSecurityService extends SQLSecurityService {
    public MSSQLSecurityService() {
        super.setIsAllowedToUseApp("exec SP_IS_ALLOWED_TO_USE_APP ? , ? ");
        super.setIsAllowedToDownloadFile("exec SP_IS_ALLOWED_TO_DOWNLOAD_FILE ? ,? ,?");
    }

    @Override
    protected void customizeRequestForProvider(OnlineRequest or) {
        or.setProvider("MSSQL");
    }

    @Override
    protected void doStop() {
    	logServiceMessage(LogEventType.SERV_STOP, "stopping service");
    	logServiceMessage(LogEventType.SERV_STOP, "stopped service");
    }

    @Override
    protected void doStart() {
    	logServiceMessage(LogEventType.SERV_START, "starting service");
    	logServiceMessage(LogEventType.SERV_START, "started service");
    }

    public static void main(String[] args) {
        MSSQLService mssqlService = new MSSQLService();
        mssqlService.setMssqlURL("jdbc:sqlserver://10.21.25.55:1433;databaseName=syncserver20");
        mssqlService.setMssqlUser("syncuser");
        mssqlService.setMssqlPassword("123456");
        mssqlService.doStart();
        MSSQLSecurityService securityService = new MSSQLSecurityService();
        securityService.setPermissionProvider(mssqlService);
        boolean allowedToUseBalance = securityService.isAllowedToUseApp("balance","RONovoselov.SBT@sberbank.ru","undefinedDeviceId");
        System.out.println("ALLOWED RESULT : "+allowedToUseBalance);
    }
}
