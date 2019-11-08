package ru.sberbank.syncserver2.service.file.transport;

import ru.sberbank.syncserver2.service.log.TagLogger;
import ru.sberbank.syncserver2.util.FileCopyHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 02.06.14.
 */
public class SharedFileLocker {
    private String   networkFilePath;
    private String[] allServers;
    private String   thisServer;

    public static final String START_LOCK    = "SYNC.START.LOK";
    public static final String FINISH_LOCK   = "SYNC.FINISH.LOK";
    public static final String TEMP_FILE     = "SYNC.TEMP.FILE.LOK";

    private static TagLogger tagLogger = TagLogger.getTagLogger(SharedFileLocker.class);

    public SharedFileLocker(String networkFilePath, String[] allServers, String thisServer) {
        this.networkFilePath = networkFilePath;
        this.allServers = allServers;
        this.thisServer = thisServer;
    }

    public boolean wasMovedByThisServer(){
        return wasMovedBefore(thisServer);
    }

    public boolean wasMovedByAll(List errors){
        //1. Checking for all configured servers
        for (int i = 0; i < allServers.length; i++) {
            String server = allServers[i];
            if(!wasMovedBefore(server)){
                log("File was not copied by "+server);
                return false;
            }
        }

        //2. Checking for locks from other servers - TODO
        File main   = new File(networkFilePath);
        final  String mainFileName = main.getName();
        File parent = main.getParentFile();
        File[] files = parent.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.startsWith(mainFileName)
                         && (name.endsWith(START_LOCK) || name.endsWith(FINISH_LOCK));
            }
        });
outer:  for (int i = 0; i < files.length; i++) {
            File file = files[i];
            String name = file.getName();
            for (int j = 0; j < allServers.length; j++) {
                String someServer = allServers[j];
                File startLock = getLock(someServer, START_LOCK);
                File finishLock = getLock(someServer, FINISH_LOCK);
                if(name.equals(startLock.getName()) || name.equals(finishLock.getName())){
                    continue outer;
                }
            }
            String errText = "A lock file from not configured server found: "+name+" . Please check SYNC_CACHE_STATIC_FILES";
            errors.add(errText);
            log(errText);
        }
        log("File was copied by all configured servers");
        return true;
    }

    private boolean wasMovedBefore(String someServer){
        File startLock = getLock(someServer, START_LOCK);
        File finishLock = getLock(someServer, FINISH_LOCK);
        return startLock.exists() && finishLock.exists();
    }

    public void start(){
        createLockFile(START_LOCK);
    }

    public void finish(){
        createLockFile(FINISH_LOCK);
    }

    private void createLockFile(String lockName) {
        File lock = getLock(thisServer, lockName);
        do {
            try {
                log("Creating " + lock.getAbsolutePath());
                lock.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } while (!lock.exists());
    }

    private File getLock(String server, String lock){
        return new File(networkFilePath+"."+server+"."+lock);
    }
      
    private void log(String text){
        String fileName = new File(networkFilePath).getName();
        tagLogger.log(fileName,text);
    }


    public void deleteLockFiles() {
        log("Deleting lock files");
        for (int i = 0; i < allServers.length; i++) {
            String someServer = allServers[i];
            File startLock = getLock(someServer,START_LOCK);
            File finishLock = getLock(someServer,FINISH_LOCK);
            FileCopyHelper.reliableDelete(startLock);
            FileCopyHelper.reliableDelete(finishLock);
        }
    }
}
