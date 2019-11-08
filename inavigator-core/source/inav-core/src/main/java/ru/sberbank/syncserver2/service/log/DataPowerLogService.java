/**
 *
 */
package ru.sberbank.syncserver2.service.log;

import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.ServiceState;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.sql.DataPowerService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.FieldType;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest.Arguments;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest.Arguments.Argument;
import ru.sberbank.syncserver2.util.XMLHelper;

import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Yuliya Solomina
 *
 */
public class DataPowerLogService extends SingleThreadBackgroundService {
	private String logSQL = "exec MIS_IPAD_GENERATOR.dbo.SP_SYNC_STORE_LOGMSG_WITH_ID ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?";
    private String provider;// = "DISPATCHER";
    private String service;//  = "finik2-new";
    private String dataPowerBeanCode;
    private DataPowerService dataPowerService;

    private ConcurrentLinkedQueue<LogMsg> queue = new ConcurrentLinkedQueue<LogMsg>();


    public DataPowerLogService() {
        super(60);
    }

    public String getDataPowerBeanCode() {
		return dataPowerBeanCode;
	}

	public void setDataPowerBeanCode(String dataPowerBeanCode) {
		this.dataPowerBeanCode = dataPowerBeanCode;
	}

    public String getLogSQL() {
        return logSQL;
    }

    public void setLogSQL(String logSQL) {
        this.logSQL = logSQL;
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
    public void doInit() {

        try {
            ServiceManager serviceManager = super.getServiceContainer().getServiceManager();
            ServiceContainer serviceContainer = serviceManager.findServiceByBeanCode(dataPowerBeanCode);
            if (serviceContainer == null) {
            	logServiceMessage(LogEventType.ERROR, "cannot start service " + getServiceBeanCode() + " because cannot find service for code = " + dataPowerBeanCode);
            	return;
            }
            dataPowerService = (DataPowerService) serviceContainer.getService();
        } catch (Throwable th) {
            tagLogger.log("Error at starting DbLogService " + th.getMessage());
            throw new RuntimeException(th);
        }

        tagLogger.log("DbLogService has been started");
    }

    @Override
    public void doRun() {
        try {
            int currSize = queue.size();
            if (currSize > 0 ) {
                tagLogger.log("DataPowerLogService has found "+currSize+" records for processing");

                while (!queue.isEmpty()) {
                    LogMsg msg = queue.poll();
                    OnlineRequest request = new OnlineRequest();
                    Arguments arguments = createArguments(msg);
					request.setArguments(arguments);
					request.setProvider(provider);
					request.setStoredProcedure(logSQL);
					request.setService(service);

					DataResponse result = dataPowerService.request(request);

					if (logger.isDebugEnabled()) {
                        tagLogger.log("DataPowerLogService request = " + XMLHelper.writeXMLToString(request,true,OnlineRequest.class));
						tagLogger.log("DataPowerLogService result  = " + String.valueOf(result));
					}
                }
            } else {
//                tagLogger.log("DbLogService hasn't found any records for writing to database");
            }
        } catch (Throwable th) {
            logger.error("Exception during storing log data in database", th);
        }
    }

	private Arguments createArguments(LogMsg msg) {
		Arguments result = new Arguments();
		Argument arg;
		arg = new Argument(1, FieldType.STRING, msg.getServerEventId());
		result.getArgument().add(arg);

		arg = new Argument(2, FieldType.STRING, msg.getUserEmail());
		result.getArgument().add(arg);

		arg = new Argument(3, FieldType.STRING, msg.getClientEventId());
		result.getArgument().add(arg);

		arg = new Argument(4, FieldType.STRING, msg.getClientDeviceId());
		result.getArgument().add(arg);

		arg = new Argument(5, FieldType.STRING, msg.getEventType().toString());
		result.getArgument().add(arg);

		arg = new Argument(6, FieldType.STRING, msg.getStartServerEventId());
		result.getArgument().add(arg);

		arg = new Argument(7, FieldType.STRING, msg.getEventDesc());
		result.getArgument().add(arg);

		arg = new Argument(8, FieldType.STRING, msg.getClientIpAddress());
		result.getArgument().add(arg);

        arg = new Argument(9, FieldType.STRING, msg.getWebHostName());
        result.getArgument().add(arg);

		arg = new Argument(10, FieldType.STRING, msg.getWebAppName());
		result.getArgument().add(arg);

		arg = new Argument(11, FieldType.STRING, msg.getDistribServer());
		result.getArgument().add(arg);

		arg = new Argument(12, FieldType.STRING, msg.getEventInfo());
		result.getArgument().add(arg);

		arg = new Argument(13, FieldType.STRING, msg.getErrorStackTrace());
		result.getArgument().add(arg);

		return result;
	}

/*
    private Properties composeMSSQLProperties() {
        Properties props = new Properties();
        props.put("driverClassName", DRIVER_CLASSNAME);
        props.put("url","jdbc:sqlserver://localhost\\SQLEXPRESS:11433;databaseName=syncserver20");
        props.put("username","sa");
        props.put("password","987654321");
        return props;
    } */

    public void log(LogMsg... logMsgs) {
    	if (dataPowerService != null) {
//    		logger.info("ADD LOG RECORD TO QUEUE "+logMsgs[0]);
    		ServiceContainer container = getServiceContainer();
    		if (container.getState()== ServiceState.STARTED) {
    			queue.addAll(Arrays.asList(logMsgs));
    		}
    	}
    }
}
