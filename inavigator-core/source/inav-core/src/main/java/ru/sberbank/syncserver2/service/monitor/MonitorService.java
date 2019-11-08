package ru.sberbank.syncserver2.service.monitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.monitor.check.CheckAction;
import ru.sberbank.syncserver2.service.monitor.check.CheckAction.Notification;
import ru.sberbank.syncserver2.service.monitor.check.ICheckResult;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;

/**
 * Created by sbt-kozhinsky-lb on 25.04.14.
 * Changed by sbt-Shakhov-IN on 05.04.16.
 */
public class MonitorService extends SingleThreadBackgroundService{
    private List<CheckAction> actions = new ArrayList<CheckAction>();
    private DatabaseNotificationLogger  databaseLogger;
    private DataPowerNotificationLogger datapowerLogger;
    private MonitorPingService monitorPingService;

    public MonitorService() {
        super(60);
    }


    public void addDatabaseLogger(DatabaseNotificationLogger databaseLogger) {
        this.databaseLogger = databaseLogger;
    }

    public void addDatapowerLogger(DataPowerNotificationLogger datapowerLogger) {
        this.datapowerLogger = datapowerLogger;
    }

    public void setMonitorPingService(MonitorPingService monitorPingService) {
        this.monitorPingService = monitorPingService;
    }

    public void addCheckAction(CheckAction action){
        actions.add(action);
    }

    @Override
    public void doInit() {
    	try {
    		// Чистим у каждого Checker-а результат предыдущего выполнения, чтобы при перезапуске
    		// не игнорировались новые результаты
	        for (int i = 0; i < actions.size(); i++) {
	        	CheckAction abstractAction = actions.get(i);
	        	abstractAction.clearAllCheckResults();
	        	//abstractAction.setLastCheckResult(null);
	        }
	        
    	} catch (Exception ex) {
    		ex.printStackTrace();
    		tagLogger.log(ex.toString());
    	}
    }

    @Override
    public void doRun() {
        //1. Make a ping
        setLastActionComment("doing ping");
        if(datapowerLogger!=null){
            datapowerLogger.ping();
        }
        if(shouldInternalTaskStop()){
            return;
        }
        if(databaseLogger!=null){
            databaseLogger.ping();
        }

        //2. Make other checks
        tagLogger.log("Start checking");
        for (int i = 0; i < actions.size(); i++) {
            if(shouldInternalTaskStop()){
            	tagLogger.log("Stop checking");
                return;
            }
            CheckAction abstractAction = actions.get(i);
            doCheck(abstractAction);                        
        }
        tagLogger.log("Finish checking");
    }
    
    private void doCheck(CheckAction checker) {
    	setLastActionComment("using "+checker.getName());
    	try {
        	List<? extends ICheckResult> checkResults = checker.check();
            for(ICheckResult checkResult:checkResults) {
            	if (checkResult == null)
            		continue;
            	
            	// получаем результат (изменилось ли состояние, результат - обертку)
            	Notification result = checker.setNextCheckResult(checkResult);
	            
	            if(result.notify){
	                tagLogger.log("Notify about "+String.valueOf(result.checkResult));
	                notify(checkResult.getErrorMessage());
	            } else {
	                tagLogger.log("Do not notify about "+String.valueOf(result.checkResult));
	            }
            }
        } catch (Exception e) {
        	logger.error(e, e);
        	tagLogger.log("Ошибка в "+checker.getName()+". "+e);
        }
    }
    
    public void doManualCheck(CheckAction checker) {
    	tagLogger.log("Start manual checking for " + checker.getName());
    	doCheck(checker);    	
    	tagLogger.log("Finish manual checking for " + checker.getName());
    }

    private void notify(String text) {
        //notify using aggregated service
        if (monitorPingService != null) {
            monitorPingService.notify(text);
        } else {
            //or using loggers without aggregation
            if(databaseLogger!=null){
                databaseLogger.addError(text);
            }
            if(datapowerLogger!=null){
                datapowerLogger.addError(text);
            }
        }
    }
    
    public List<CheckAction> getActions() {
    	return Collections.unmodifiableList(actions);
    }
    
    public static void main(String[] args) {
    	System.out.println(getBetter(Result.FAIL_DP,Result.OK));
    }
    
    private static Result getBetter(Result a, Result b) {
    	return (a.compareTo(b)>0) ? b : a;
    }
}
