/**
 *
 */
package ru.sberbank.syncserver2.service.sql;

import org.apache.log4j.Logger;
import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.PublicService;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.util.ExecutionTimeProfiler;
import ru.sberbank.syncserver2.util.FormatHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Yuliya Solomina
 */
public class SQLPublicService extends AbstractService implements PublicService, SQLService {
    private Logger traceOnlineSql = Logger.getLogger("trace.syncserver.online.sql");
    private MSSQLService mssqlService;
    private DataPowerService dataPowerService;
    private SQLiteService sqliteService;
    private SQLDispatcherService sqlDispatcherService;
    //private DataPowerSecurityService securityService;
    private String conversion;
    private String overrideProvider;

    public static final String CONVERT_TO_NEW_SIGMA        = "CONVERT_TO_NEW_SIGMA";
    public static final String CONVERT_TO_OLD_ALPHA        = "CONVERT_TO_OLD_ALPHA";
    public static final String CONVERT_TO_PASSPORT_SIGMA   = "CONVERT_TO_PASSPORT_SIGMA";
    public static final String CONVERT_TO_PASSPORT_ALPHA   = "CONVERT_TO_PASSPORT_ALPHA";

    public MSSQLService getMssqlService() {
        return mssqlService;
    }

    public void setMssqlService(MSSQLService mssqlService) {
        this.mssqlService = mssqlService;
    }

    public DataPowerService getDataPowerService() {
        return dataPowerService;
    }

    public void setDataPowerService(DataPowerService dataPowerService) {
        this.dataPowerService = dataPowerService;
    }

    public SQLiteService getSqliteService() {
        return sqliteService;
    }

    public void setSqliteService(SQLiteService sqliteService) {
        this.sqliteService = sqliteService;
    }

    public SQLDispatcherService getSqlDispatcherService() {
        return sqlDispatcherService;
    }

    public void setSqlDispatcherService(SQLDispatcherService sqlDispatcherService) {
        this.sqlDispatcherService = sqlDispatcherService;
    }

    public String getConversion() {
        return conversion;
    }

    public void setConversion(String conversion) {
        this.conversion = conversion;
    }

    public String getOverrideProvider() {
        return overrideProvider;
    }

    public void setOverrideProvider(String overrideProvider) {
        this.overrideProvider = overrideProvider;
    }

    public DataResponse request(OnlineRequest request) {
        //1. Trimming spaces in provider and service - required for some operations
        logUserEvent(LogEventType.SQL_START, request.getUserEmail(), request.getUserIpAddress(), "start sql request", request.toString());
        request.trim();
        DataResponse result = null;
        String errorMsg = null;

        if (traceOnlineSql.isTraceEnabled())
            traceOnlineSql.trace(FormatHelper.stringConcatenator("SQLPublicService.request: ", request));


        //2. Check permission to use app
        /**
         * TODO: We should add application to request to check permissions
         *
        if(securityService==null){
            AbstractService service = ServiceManager.getInstance().findFirstServiceByClassCode(DataPowerSecurityService.class);
            securityService = (DataPowerSecurityService) service;
        } */

        //2. Convert and change provider
        if(conversion!=null){
            if(CONVERT_TO_OLD_ALPHA.equalsIgnoreCase(conversion)){
                request.convertToOldAlpha();
            } else if(CONVERT_TO_PASSPORT_ALPHA.equalsIgnoreCase(conversion)){
                request.convertToPassportAlpha();
            }
        }
        if(overrideProvider!=null){
            request.setProvider(overrideProvider);
        }

        if (traceOnlineSql.isTraceEnabled())
            traceOnlineSql.trace(FormatHelper.stringConcatenator("SQLPublicService.request: ", request));


        //3. Forwared request to required service
        try {
            ExecutionTimeProfiler.start("SQLService.executeSql.dispatch");
            if (PROVIDER_DATAPOWER.equalsIgnoreCase(request.getProvider())) {
                if (dataPowerService != null) {
                    result = dataPowerService.request(request);
                } else {
                    errorMsg = PROVIDER_DATAPOWER+ " is null. Cannot perform request";
                }
            } else if (PROVIDER_DISPATCHER.equalsIgnoreCase(request.getProvider())) {
                if (sqlDispatcherService != null) {
                    result = sqlDispatcherService.request(request);
                } else {
                    errorMsg = PROVIDER_DISPATCHER + " is null. Cannot perform request";
                }
            } else if (PROVIDER_SQLITE.equalsIgnoreCase(request.getProvider())) {
                if (sqliteService != null) {
                    result = sqliteService.request(request);
                } else {
                    errorMsg = PROVIDER_SQLITE + " is null. Cannot perform request";
                }
            } else if (PROVIDER_MSSQL.equalsIgnoreCase(request.getProvider())) {
                if (mssqlService != null) {
                    result = mssqlService.request(request);
                } else {
                    errorMsg = PROVIDER_MSSQL+" is null. Cannot perform request";
                }
            } else {
                DataResponse error = new DataResponse();
                error.setError("Undefined provider - please check your request");
                error.setResult(Result.FAIL);
                logUserEvent(LogEventType.ERROR, request.getUserEmail(), request.getUserIpAddress(), "finish sql request with error", "warnMsg=" + errorMsg + " for request=" + request+" with result = "+error);
                return error;
            }
        } finally {
            ExecutionTimeProfiler.finish("SQLService.executeSql.dispatch");
        }

        //4. Log success and return - no error in this case
        if (result != null) {
            logUserEvent(LogEventType.SQL_FINISH, request.getUserEmail(), request.getUserIpAddress(), "finish sql request with success", request.toString());
            return result;
        }


        //4. Log error and return error if no CONFIGURED provder was found
        DataResponse error = new DataResponse();
        error.setError("Undefined service or source error - please check your request");
        error.setResult(Result.FAIL_ACCESS);
        logUserEvent(LogEventType.ERROR, request.getUserEmail(), request.getUserIpAddress(), "finish sql request with error", "warnMsg=" + errorMsg + " for request=" + request+" with result = "+error);
        return error;
    }

    /* (non-Javadoc)
     * @see ru.sberbank.syncserver2.service.core.PublicService#request(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void request(HttpServletRequest request, HttpServletResponse response) {
        throw new UnsupportedOperationException();
    }

    /* (non-Javadoc)
     * @see ru.sberbank.syncserver2.service.core.AbstractService#doStop()
     */
    @Override
    protected void doStop() {
    	logServiceMessage(LogEventType.SERV_STOP, "stopping service");
    	logServiceMessage(LogEventType.SERV_STOP, "stopped service");
    }

    @Override
    protected void waitUntilStopped() {
    }

    /* (non-Javadoc)
     * @see ru.sberbank.syncserver2.service.core.AbstractService#init()
     */
    @Override
    protected void doStart() {
    	logServiceMessage(LogEventType.SERV_START, "starting service");
    	logServiceMessage(LogEventType.SERV_START, "started service");
    }

}
