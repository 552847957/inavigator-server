package ru.sberbank.syncserver2.service.security;

import ru.sberbank.syncserver2.service.core.BackgroundService;
import ru.sberbank.syncserver2.service.sql.SQLService;
import ru.sberbank.syncserver2.service.sql.query.*;
import ru.sberbank.syncserver2.util.XMLHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 04.03.14.
 */
public abstract class SQLSecurityService extends BackgroundService implements SecurityService{
    private SQLService permissionProvider;
    private String isAllowedToUseApp;
    private String isAllowedToDownloadFile;

    public SQLService getPermissionProvider() {
        return permissionProvider;
    }

    public void setPermissionProvider(SQLService permissionProvider) {
        this.permissionProvider = permissionProvider;
    }

    public String getIsAllowedToUseApp() {
        return isAllowedToUseApp;
    }

    public void setIsAllowedToUseApp(String isAllowedToUseApp) {
        this.isAllowedToUseApp = isAllowedToUseApp;
    }

    public String getIsAllowedToDownloadFile() {
        return isAllowedToDownloadFile;
    }

    public void setIsAllowedToDownloadFile(String isAllowedToDownloadFile) {
        this.isAllowedToDownloadFile = isAllowedToDownloadFile;
    }

    private boolean check(String sql, String[] arguments){
        //1. Prepare request
        OnlineRequest or = new OnlineRequest();
        or.setStoredProcedure(sql);
        customizeRequestForProvider(or);
        List<OnlineRequest.Arguments.Argument> argList  = new ArrayList<OnlineRequest.Arguments.Argument>();
        for (int i = 0; i < arguments.length; i++) {
            OnlineRequest.Arguments.Argument arg = new OnlineRequest.Arguments.Argument();
            arg.setIndex(i+1);
            arg.setType(FieldType.STRING);
            arg.setValue(arguments[i]);
            argList.add(arg);
        }
        OnlineRequest.Arguments orArguments = new OnlineRequest.Arguments();
        orArguments.setArgument(argList);
        or.setArguments(orArguments);

        //System.out.println("PERMISSION REQUEST: "+XMLHelper.writeXMLToString(or, true, OnlineRequest.class));

        //2. Sending and parsing request
        DataResponse dr = permissionProvider.request(or);
        Dataset ds = dr==null ? null:dr.getDataset();
        List<DatasetRow> rows = ds==null ? null:ds.getRows();
        DatasetRow row = (rows==null || rows.size()!=1) ? null:rows.get(0);
        List<String> fields = row==null ? null:row.getValues();
        String value = (fields==null || fields.size()!=1) ? null:fields.get(0);
        //System.out.println("PERMISSION RESULT: "+value);
        boolean result = "true".equalsIgnoreCase(value);
        if(!result){
            String requestString = XMLHelper.writeXMLToString(or, true, OnlineRequest.class);
            tagLogger.log("PERMISSION FAILURE ON REQUEST: "+ requestString);
        }
        return result;
    }

    protected abstract void customizeRequestForProvider(OnlineRequest or);

    @Override
    public boolean isAllowedToUseApp(String app, String userEmail, String deviceId) {
        //TODO - we should replace this temporary hack with device id
        if(userEmail==null || userEmail.trim().length()==0){
            userEmail = "null";
        }
        if(deviceId==null || deviceId.trim().length()==0){
            deviceId = "null";
        }
        return check(isAllowedToUseApp, new String[]{app, userEmail, deviceId});
    }

    @Override
    public boolean isAllowedToDownloadFile(String app, String fileName, String userEmail, String deviceId) {
        //TODO - we should replace this temporary hack with device id
        if(userEmail==null || userEmail.trim().length()==0){
            userEmail = "null";
        }
        if(deviceId==null || deviceId.trim().length()==0){
            deviceId = "null";
        }
        if(fileName==null || fileName.trim().length()==0){
            fileName = "null";
        }
        return check(isAllowedToDownloadFile, new String[]{app, fileName, userEmail, deviceId});
    }
}
