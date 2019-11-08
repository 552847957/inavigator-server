/**
 *
 */
package ru.sberbank.syncserver2.service.monitor;

import ru.sberbank.syncserver2.service.core.BackgroundService;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.generator.single.data.ActionState;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.sql.DataPowerService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.FieldType;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest.Arguments;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest.Arguments.Argument;

/**
 * @author Leonid Kozhinskiy
 *
 */
public class DataPowerNotificationLogger extends BackgroundService {
    private String provider;// = "DISPATCHER";
    private String service;//  = "finik2-new";

    private String dataPowerBeanCode;
    private DataPowerService dataPowerService;

    /**
     * Название БД монитора (Alpha)
     */
    private String databaseName;

    /**
     * Название БД генератора (Alpha)
     */
    private String databaseGeneratorName;

    public DataPowerNotificationLogger() {
        super();
    }

    @Override
    protected void doStop() {
        logServiceMessage(LogEventType.SERV_STOP, "stopping service");
        logServiceMessage(LogEventType.SERV_STOP, "stopped service");
    }

    public String getDataPowerBeanCode() {
		return dataPowerBeanCode;
	}

	public void setDataPowerBeanCode(String dataPowerBeanCode) {
		this.dataPowerBeanCode = dataPowerBeanCode;
	}

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }



    public String getDatabaseGeneratorName() {
		return databaseGeneratorName;
	}

	public void setDatabaseGeneratorName(String databaseGeneratorName) {
		this.databaseGeneratorName = databaseGeneratorName;
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

    private DataPowerService getDataPowerService(){
        //1. Check if service already found
        synchronized (this){
            if(dataPowerService!=null){
                return dataPowerService;
            }
        }

        //2. Finding a service
        DataPowerService service = null;
        try {
            ServiceManager serviceManager = super.getServiceContainer().getServiceManager();
            ServiceContainer serviceContainer = serviceManager.findServiceByBeanCode(dataPowerBeanCode);
            if (serviceContainer == null) {
                logServiceMessage(LogEventType.ERROR, "cannot start service " + getServiceBeanCode() + " cannot find service for code = " + dataPowerBeanCode);
                return null;
            }
            service = (DataPowerService) serviceContainer.getService();
        } catch (Throwable th) {
            tagLogger.log("Error at starting DbLogService " + th.getMessage());
            throw new RuntimeException(th);
        }

        //3. Amending service
        synchronized (this){
            dataPowerService = service;
        }
        return dataPowerService;
    }

    public void doStart() {
        super.doStart();

        tagLogger.log("DbLogService has been started");
    }

    public void delFPMove(String fileName, String fileMd5){
        try {
            //1. Creating online request
            String sql = "SYNCSERVER.DP_NOT_DEL_FILE_MOV_NOTIFICATION";
            //System.out.println(sql);
            OnlineRequest request = new OnlineRequest();
            request.setStoredProcedure(sql);
            Arguments arguments = createArguments(fileName, fileMd5, LOCAL_HOST_NAME);
            request.setArguments(arguments);
            request.setProvider(provider);
            request.setService(service);

            //2. Sending online request
            DataPowerService local = getDataPowerService();
            DataResponse result = local.request(request);
            //System.out.println("SP_DEL_FILE_MOV_NOTIFICATION  RESULT "+result);
        } catch (Exception e) {
            tagLogger.log("Ошибка при удалении уведомления о недоставке файла "+fileName+": "+e.toString());
            logger.error(e, e);
        }
    }

    public void addError(String text){
        try {
            //1. Creating online request
            String sql = "SYNCSERVER.DP_NOT_ADD_ERROR";
            OnlineRequest request = new OnlineRequest();
            request.setStoredProcedure(sql);
            Arguments arguments = createArguments(LOCAL_HOST_NAME, "monitor", text);
            request.setArguments(arguments);
            request.setProvider(provider);
            request.setService(service);

            //2. Sending online request
            DataPowerService local = getDataPowerService();
            DataResponse result = local.request(request);
            //System.out.println("SP_ADD_ERROR RESULT "+result);
        } catch (Exception e) {
            logger.error(e, e);
        }

    }

    public void ping(){
        try {
            //1. Creating online request
            String sql = "SYNCSERVER.DP_NOT_PING";
            OnlineRequest request = new OnlineRequest();
            request.setStoredProcedure(sql);
            Arguments arguments = createArguments(LOCAL_HOST_NAME, getWebAppName());
            request.setArguments(arguments);
            request.setProvider(provider);
            request.setService(service);

            //2. Sending online request
            DataPowerService local = getDataPowerService();
            DataResponse result = local.request(request);
            //System.out.println("SP_PING RESULT "+result);
        } catch (Exception e) {
            tagLogger.log("Ошибка при посылке ping "+e.toString());
            logger.error(e, e);
        }
    }

    public void addGenStaticFileEvent(String fileName,String statusCode,String host){
    	addGenStaticFileEvent(fileName, ActionState.PHASE_LOADING_TO_SIGMA, statusCode,"", host);
    }

    public void addGenStaticFileEvent(String fileName,String state, String statusCode,String host){
    	addGenStaticFileEvent(fileName, state, statusCode,"", host);
    }

    /**
     * Добавление сигнала-события при загрузке файла
     * Метод вызывается из SIGMA
     * @param fileName
     * @param statusCode
     * @param comment
     * @param host
     */
    public void addGenStaticFileEvent(String fileName,String phaseCode,String statusCode,String comment,String host){
        try {
            //1. Creating online request
            String sql = "SYNCSERVER.ADD_GENERATOR_STATE";
            OnlineRequest request = new OnlineRequest();
            request.setStoredProcedure(sql);
            Arguments arguments = createArguments(fileName,phaseCode,statusCode,comment,host,"");
            request.setArguments(arguments);
            request.setProvider(provider);
            request.setService(service);

            //2. Sending online request
            DataPowerService local = getDataPowerService();
            DataResponse result = local.request(request);

        } catch (Exception e) {
            tagLogger.log("Ошибка при обновлении статуса генерации файла "+fileName+": "+e.toString());
            logger.error(e, e);
        }
    }

	private Arguments createArguments(String ... args) {
		Arguments result = new Arguments();
		Argument arg;
        for (int i = 0; i < args.length; i++) {
            arg = new Argument(i+1, FieldType.STRING, args[i]);
            result.getArgument().add(arg);
        }
		return result;
	}

    private String getWebAppName(){
        return "Monitor"; //should be always monitor until we use separate JVMs in separate apps at Websphere Application Server ND
    }

}
