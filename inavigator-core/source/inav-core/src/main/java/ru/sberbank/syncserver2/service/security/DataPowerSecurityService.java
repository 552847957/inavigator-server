package ru.sberbank.syncserver2.service.security;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.ServiceManagerHelper;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.sql.SQLService;
import ru.sberbank.syncserver2.service.sql.DataPowerService;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;

/**
 * Created by sbt-kozhinsky-lb on 04.03.14.
 */
public class DataPowerSecurityService extends SQLSecurityService {
    private String dataPowerServiceBeanCode;
    private String provider;// = "DISPATCHER";
    private String service;//  = "finik2-new";

    public String getDataPowerServiceBeanCode() {
        return dataPowerServiceBeanCode;
    }

    public void setDataPowerServiceBeanCode(String dataPowerServiceBeanCode) {
        this.dataPowerServiceBeanCode = dataPowerServiceBeanCode;
    }

    public DataPowerSecurityService() {
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    @Override
    protected void doStop() {
    	logServiceMessage(LogEventType.SERV_STOP, "stoping service");
    	logServiceMessage(LogEventType.SERV_STOP, "stoped service");
    }

    @Override
    protected void doStart() {
    	logServiceMessage(LogEventType.SERV_START, "starting service");
        if(dataPowerServiceBeanCode!=null){
            ServiceContainer container = ServiceManager.getInstance().findServiceByBeanCode(dataPowerServiceBeanCode);
            AbstractService service = container.getService();
            super.setPermissionProvider((SQLService) service);
        }
    	logServiceMessage(LogEventType.SERV_START, "started service");
    }

    public static void main(String[] args) {
        //1. Creating
        DataPowerService requestor = new DataPowerService();
        requestor.setOverrideProvider("DISPATCHER");
        requestor.setOverrideService1("finik2-new");
        requestor.setOverrideService2("finik2-new");
        ServiceManagerHelper.setTagLoggerForUnitTest(requestor);

        DataPowerSecurityService dataPowerSecurityService = new DataPowerSecurityService();
        dataPowerSecurityService.setPermissionProvider(requestor);
        ServiceManagerHelper.setTagLoggerForUnitTest(dataPowerSecurityService);

        SessionCachedSecurity sessionCachedSecurity = new SessionCachedSecurity();
        sessionCachedSecurity.setOriginalSecurityService(dataPowerSecurityService);
        int WAIT_SECONDS = 5;
        sessionCachedSecurity.setSessionTimeoutSeconds(WAIT_SECONDS);

        SecurityService securityService = sessionCachedSecurity;

        //2. Testing
        boolean allowedToUseBalanceNovoselov = securityService.isAllowedToUseApp("balance","RONovoselov.SBT@sberbank.ru","hello");
        System.out.println("POSITIVE RESULT : "+allowedToUseBalanceNovoselov);
        boolean allowedToUseBalanceZunov = securityService.isAllowedToUseApp("balance","ASZunov.SBT@sberbank.ru","hello");
        System.out.println("NEGATIVE RESULT : "+allowedToUseBalanceZunov);

        allowedToUseBalanceNovoselov = securityService.isAllowedToUseApp("balance","RONovoselov.SBT@sberbank.ru","hello");
        System.out.println("POSITIVE CACHED RESULT : "+allowedToUseBalanceNovoselov);
        allowedToUseBalanceZunov = securityService.isAllowedToUseApp("balance","ASZunov.SBT@sberbank.ru","hello");
        System.out.println("NEGATIVE CACHED RESULT : "+allowedToUseBalanceZunov);

        try {
            Thread.sleep(WAIT_SECONDS*2*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        allowedToUseBalanceNovoselov = securityService.isAllowedToUseApp("balance","RONovoselov.SBT@sberbank.ru","hello");
        System.out.println("POSITIVE EXPIRED RESULT : "+allowedToUseBalanceNovoselov);
        allowedToUseBalanceZunov = securityService.isAllowedToUseApp("balance","ASZunov.SBT@sberbank.ru","hello");
        System.out.println("NEGATIVE EXPIRED RESULT : "+allowedToUseBalanceZunov);

        try {
            Thread.sleep(WAIT_SECONDS*2*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("CLEANING CACHE TEST");
        sessionCachedSecurity.doRun();

        allowedToUseBalanceNovoselov = securityService.isAllowedToUseApp("balance","RONovoselov.SBT@sberbank.ru","hello");
        System.out.println("POSITIVE CLEANED RESULT : "+allowedToUseBalanceNovoselov);
        allowedToUseBalanceZunov = securityService.isAllowedToUseApp("balance","ASZunov.SBT@sberbank.ru","hello");
        System.out.println("NEGATIVE CLEANED RESULT : "+allowedToUseBalanceZunov);

    }

    @Override
    protected void customizeRequestForProvider(OnlineRequest or) {
        or.setProvider(provider);
        or.setService(service);
    }
}
