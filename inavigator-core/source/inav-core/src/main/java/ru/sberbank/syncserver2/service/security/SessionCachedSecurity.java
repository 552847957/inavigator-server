package ru.sberbank.syncserver2.service.security;

import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.util.ExecutionTimeProfiler;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by sbt-kozhinsky-lb on 04.03.14.
 */
public class SessionCachedSecurity extends SingleThreadBackgroundService implements SecurityService {
    private SecurityService originalSecurityService;
    private int sessionTimeoutSeconds = 600;
    private LinkedHashMap appPermissions = new LinkedHashMap();
    private LinkedHashMap filePermissions = new LinkedHashMap();

    public SessionCachedSecurity() {
        super(60);
    }

    public SecurityService getOriginalSecurityService() {
        return originalSecurityService;
    }

    public void setOriginalSecurityService(SecurityService originalSecurityService) {
        this.originalSecurityService = originalSecurityService;
    }

    public int getSessionTimeoutSeconds() {
        return sessionTimeoutSeconds;
    }

    public void setSessionTimeoutSeconds(int sessionTimeoutSeconds) {
        this.sessionTimeoutSeconds = sessionTimeoutSeconds;
    }

    @Override
    public boolean isAllowedToUseApp(String app, String userEmail, String deviceId) {
        //1. Getting or create permission info
        PermissionKey key = new PermissionKey(app, null, userEmail,deviceId);
        PermissionInfo info = getOrCreatePermissionInfo(appPermissions, key);

        //2. Checking info
        synchronized (info){
            switch(info.getStatus()){
                case PermissionInfo.ALLOWED: return true;
                case PermissionInfo.DENIED:  return false;
                case PermissionInfo.UNKNOWN:
                    boolean allowed = originalSecurityService.isAllowedToUseApp(app,userEmail,deviceId);
                    info.createTime = System.currentTimeMillis();
                    info.status = allowed ? PermissionInfo.ALLOWED:PermissionInfo.DENIED;
                    return allowed;
            }
        }
        return false;
    }

    @Override
    public boolean isAllowedToDownloadFile(String app, String fileName, String userEmail, String deviceId) {
        //1. Getting or create permission info
        PermissionKey key = new PermissionKey(app, null, userEmail,deviceId);
        PermissionInfo info = getOrCreatePermissionInfo(appPermissions, key);

        //2. Checking info
        synchronized (info){
            switch(info.getStatus()){
                case PermissionInfo.ALLOWED: return true;
                case PermissionInfo.DENIED:  return false;
                case PermissionInfo.UNKNOWN:
                    boolean allowed = originalSecurityService.isAllowedToDownloadFile(app, fileName, userEmail, deviceId);
                    info.createTime = System.currentTimeMillis();
                    info.status = allowed ? PermissionInfo.ALLOWED:PermissionInfo.DENIED;
                    return allowed;
            }
        }
        return false;
    }

    private PermissionInfo getOrCreatePermissionInfo(LinkedHashMap map, PermissionKey key){
        synchronized (map){
            PermissionInfo info = (PermissionInfo) map.get(key);
            if(info==null){
                info = new PermissionInfo();
                map.put(key, info);
            }
            return info;
        }
    }

    @Override
    public void doInit() {
    }

    @Override
    protected void doStop() {
        super.doStop();
        synchronized (appPermissions){
            appPermissions.clear();
        }
        synchronized (filePermissions){
            filePermissions.clear();
        }
    }

    @Override
    public void doRun() {
        //1. Cleaning all entries older then configured number of seconds - for apps
        long obsoleteTime = System.currentTimeMillis() - sessionTimeoutSeconds*1000;
        synchronized (appPermissions){
            for (Iterator iterator = appPermissions.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry entry = (Map.Entry) iterator.next();
                PermissionInfo info = (PermissionInfo) entry.getValue();
                //System.out.println(new Date(info.createTime)+" for "+entry.getKey());
                //System.out.println(new Date(obsoleteTime)+" for "+entry.getKey());
                if(info.createTime<obsoleteTime){
                    //System.out.println("REMOVED");
                    iterator.remove();
                }
            }
            //System.out.println("APP SIZE : "+appPermissions.size());
        }

        //2. Cleaning all entries older then configured number of seconds - for files
        synchronized (filePermissions){
            for (Iterator iterator = filePermissions.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry entry = (Map.Entry) iterator.next();
                PermissionInfo info = (PermissionInfo) entry.getValue();
                if(info.createTime<obsoleteTime){
                    //System.out.println("REMOVED");
                    iterator.remove();
                }
            }
            //System.out.println("FILE SIZE : "+appPermissions.size());
        }
    }

    private static class PermissionKey {
        private String app;
        private String filename;
        private String userEmail;
        private String deviceId;

        private PermissionKey(String app, String filename, String userEmail, String deviceId) {
            this.app = app;
            this.filename = filename;
            this.userEmail = userEmail;
            this.deviceId = deviceId;
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public void setDeviceId(String deviceId) {
            this.deviceId = deviceId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PermissionKey)) return false;

            PermissionKey that = (PermissionKey) o;

            if (app != null ? !app.equals(that.app) : that.app != null) return false;
            if (deviceId != null ? !deviceId.equals(that.deviceId) : that.deviceId != null) return false;
            if (filename != null ? !filename.equals(that.filename) : that.filename != null) return false;
            if (userEmail != null ? !userEmail.equals(that.userEmail) : that.userEmail != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = app != null ? app.hashCode() : 0;
            result = 31 * result + (filename != null ? filename.hashCode() : 0);
            result = 31 * result + (userEmail != null ? userEmail.hashCode() : 0);
            result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "PermissionKey{" +
                    "app='" + app + '\'' +
                    ", filename='" + filename + '\'' +
                    ", userEmail='" + userEmail + '\'' +
                    ", deviceId='" + deviceId + '\'' +
                    '}';
        }
    }

    private class PermissionInfo {
        private long createTime = System.currentTimeMillis();
        private int status = UNKNOWN;

        public static final int UNKNOWN = 0;
        public static final int DENIED  = -1;
        public static final int ALLOWED = 1;

        public int getStatus(){
            if(createTime<System.currentTimeMillis()-sessionTimeoutSeconds*1000){
                status = UNKNOWN;
            }
            return status;
        }

    }
}
