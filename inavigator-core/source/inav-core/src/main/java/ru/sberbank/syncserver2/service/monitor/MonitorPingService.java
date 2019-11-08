package ru.sberbank.syncserver2.service.monitor;

import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.monitor.check.CheckAction;
import ru.sberbank.syncserver2.service.monitor.check.CheckAction.Notification;
import ru.sberbank.syncserver2.service.monitor.check.ICheckResult;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by sbt-Shakhov-IN on 05.04.16.
 */
public class MonitorPingService extends SingleThreadBackgroundService{
    private DatabaseNotificationLogger  databaseLogger;
    private DataPowerNotificationLogger datapowerLogger;

    public MonitorPingService() {
        super(60);
    }


    public void setDatabaseLogger(DatabaseNotificationLogger databaseLogger) {
        this.databaseLogger = databaseLogger;
    }

    public void setDatapowerLogger(DataPowerNotificationLogger datapowerLogger) {
        this.datapowerLogger = datapowerLogger;
    }


    @Override
    public void doInit() {

    }

    @Override
    public void doRun() {
        //1. Make a ping
        if(shouldInternalTaskStop()){
            return;
        }
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
    }

    public void notify(String text) {
        if(databaseLogger!=null){
            databaseLogger.addError(text);
        }
        if(datapowerLogger!=null){
            datapowerLogger.addError(text);
        }
    }

}
