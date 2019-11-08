package ru.sberbank.syncserver2.service.monitor.check;

import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.FileHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sbt-kozhinsky-lb on 26.04.14.
 */
public class InputPingCheck extends SingleThreadBackgroundService {
    private String           folder;
    private String           hostnames;
    private Map<String,Long> lastDetectedPings = new HashMap();
    private int              detectAttemptCount = 0;
    
    private Map<String,CheckResult> lastCheckResultMap = new HashMap<String,CheckResult>();

    public InputPingCheck() {
        super(37); //the idea of implementing this as SingleThreadBackgroundService
                   // is to make InputPingCheck periodially different from OutputPingGenerator
    }

    public String getHostnames() {
        return hostnames;
    }

    public void setHostnames(String hostnames) {
        this.hostnames = hostnames;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public List<CheckResult> checkAndCompare(){
        List<CheckResult> checksForNotifications = new ArrayList<CheckResult>();
        String[] hostNames = this.hostnames.split(";");
        for (int i = 0; i < hostNames.length; i++) {
            String hostName = hostNames[i];
            CheckResult thisResult = check(hostName);
            CheckResult prevResult = lastCheckResultMap.get(hostName);
            lastCheckResultMap.put(hostName, thisResult);
            if(prevResult!=null && !prevResult.equals(thisResult)){
                dbLogger.logObjectEvent(LogEventType.DEBUG,"InputPingCheck","Notify about " + String.valueOf(thisResult));
                checksForNotifications.add(thisResult);
            } else {
                dbLogger.logObjectEvent(LogEventType.DEBUG,"InputPingCheck","Do not notify about "+String.valueOf(thisResult));
            }
        }
        return checksForNotifications;
    }

    private CheckResult check(String hostname) {
        //1. lastDetectedPings is filled in background thread
        Long lastModify;
        synchronized (lastDetectedPings){
            if(detectAttemptCount<3){ //do not make a check in first 3 minutes after start
                return null; //"Skip check of ping on start")
            }
            lastModify = lastDetectedPings.get(hostname);
        }

        //2. Checking if there was ping during last 5 minutes
        if(lastModify!=null && lastModify>System.currentTimeMillis()){
            return new CheckResult(true, "Monitor at "+hostname+" is working");
        } else {
            String txt = "Monitor at "+AbstractCheckAction.LOCAL_HOST_NAME +" did not found ping from "
                       + hostname+" in expected file "+new File(folder,hostname).getAbsolutePath()
                       + " for user "+System.getProperty("user.name");
            return new CheckResult(false, txt);
        }
    }

    @Override
    public void doInit() {
        FileHelper.createMissingFolders(folder);
    }

    @Override
    public void doRun() {
        try {
            //1. Listing all files in input folder
            File[] files = new File(folder).listFiles();
            if(files==null || files.length==0){
                return;
            }

            //2. Count only non-LOK files
            for (int i = 0; i < files.length; i++) {
                //1. Check if file exists
                File file = files[i];
                long lastModifyTime = file.lastModified();
                String name = file.getName();
                if(file==null || !file.exists() || name.endsWith(".LOK")){
                    continue;
                }
                if(name.endsWith(".LOK")){
                    continue;
                }

                //2. Skip file if it is a lock file or disappeared while this cycle
                if(new File(file.getAbsolutePath()+".LOK").exists()){
                    continue;
                }
                synchronized (lastDetectedPings){
                    lastDetectedPings.put(name, lastModifyTime);
                }
            }
        } finally {
            synchronized (lastDetectedPings){
                detectAttemptCount++;
            }
        }
    }

}
