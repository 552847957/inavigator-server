package ru.sberbank.syncserver2.service.file.transport;

import org.apache.log4j.Logger;
import ru.sberbank.syncserver2.service.file.cache.list.FileLister;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;
import ru.sberbank.syncserver2.util.MD5Helper;

import java.io.File;
import java.io.IOException;

/**
 * @author Leonid Kozhinskiy
 *
 */
public class SharedMoveLock {
    private String   networkFilePath;
    private String[] allServers;
    private String   thisServer;
    private String   lastCheckSum;

    public static final String CHECKSUM_LOCK_SUFFIX   = "SYNC.CHECKSUM.LOK";
    public static final String CONFIRM_LOCK_SUFFIX    = "SYNC.CONFIRM.LOK";

    private static final int ONE_MINUTE = 60000;

    private Logger logger = Logger.getLogger(SharedMoveLock.class);

    public SharedMoveLock(String networkFilePath, String[] allServers, String thisServer) {
        this.networkFilePath = networkFilePath;
        this.allServers = allServers;
        this.thisServer = thisServer;
    }

    private void waitUntilCheckSumFileIsCalculated(){
        log("started waitUntilCheckSumFileIsCalculated");
        String path = composeCheckSumPath();
        File checkSumFile = new File(path);
        while(true){
            if(createNewFile(checkSumFile)){
                try {
                    lastCheckSum = MD5Helper.getCheckSumAsString(networkFilePath);
                    FileHelper.writeObject(lastCheckSum, checkSumFile);
                    log("finished waitUntilCheckSumFileIsCalculated with calculation of checksum");
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                //1. Waiting one minute until other lock calculates checksum
                if(checkSumFile.length()<39){
                    try {
                        Thread.sleep(ONE_MINUTE);
                        continue;
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }

                //2. Reading checksum from calculated lock
                try {
                    lastCheckSum = (String) FileHelper.readObject(checkSumFile);
                    log("finished waitUntilCheckSumFileIsCalculated with reading of checksum");
                    return;
                } catch (Exception e1) {
                    e1.printStackTrace();
                    checkSumFile.delete();
                }
            }
        }
    }

    public boolean wasMovedBefore(){
        //1. Wait until checksum file is calculated
        waitUntilCheckSumFileIsCalculated();

        //2. We copied it before if lock file exists and cont
        String lockFilePath = composeLockPath();
        File lockFile = new File(lockFilePath);
        String checkSumFilePath = composeCheckSumPath();
        File checkSumFile = new File(checkSumFilePath);
        if(!FileHelper.areFilesSame(lockFile, checkSumFile)){
            log("file was not moved earlier - we should move it");
            return false;
        } else {
            log("file was moved earlier - we should not move it");
        }

        //3. If all lock files contains same then we drop locks, checksum and main file
        //3.1. Composing file list
        File[] files = new File[allServers.length+1];
        files[0] = checkSumFile;
        for (int i = 0; i < allServers.length; i++) {
            lockFilePath = composeLockPath(allServers[i]);
            files[i+1] = new File(lockFilePath);
        }
        if(FileHelper.areFilesSame(files)){
            log("all servers moved file and we start dropping it");
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                FileCopyHelper.reliableDelete(file);
            }
            new File(networkFilePath).delete();
            log("all servers moved file and we finished dropping it");
       } else {
            log("not all servers moved file and we wait for them");
        }
        return true;
    }

    public void confirmMove(){
        log("start to confirm move");
        String lockFilePath = composeLockPath();
        File lockFile = new File(lockFilePath);
        FileHelper.writeObject(lastCheckSum, lockFile.getAbsolutePath());
        log("finish to confirm move");
    }

    private static String getCheckSum(String path) {
        String checkSum = ""+new File(path).lastModified();
        try {
            checkSum+="-"+MD5Helper.getCheckSumAsString(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return checkSum;
    }

    private static String getCheckSum(File file) {
        return getCheckSum(file.getAbsolutePath());
    }

    private String composeLockPath(){
        return composeLockPath(thisServer);
    }

    private String composeLockPath(String server){
        String result = networkFilePath+"."+server+"."+ CONFIRM_LOCK_SUFFIX;
        return result;
    }

    private String composeCheckSumPath(){
        String result = networkFilePath + "." + CHECKSUM_LOCK_SUFFIX;
        return result;
    }

    private static boolean createNewFile(File file){
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void log(String txt){
        txt = "Moving "+networkFilePath+" to server "+thisServer+" : "+txt;
        //System.out.println(txt);
        logger.debug(txt);
    }

    /*
    public static void main(String[] args) throws InterruptedException {
        final String root = "C:/Локальные документы/temp/SharedMoveLock/test1";
        final String server1 = "server1";
        final String server2 = "server2";
        FileHelper.createMissingFolders(root);

        //1. Test functionally
        System.out.println("-------------------------------------TEST 1----------------------------------");
        String sourceFileName = "C:/Локальные документы/temp/SharedMoveLock/source/sync_server2.rar";
        String filename = new File(root,"sync_server2.rar").getAbsolutePath();
        FileCopyHelper.reliableCopy(new File(sourceFileName), new File(filename));
        SampleLockTest lock1 = new SampleLockTest(server1,new String[]{server1,server2}, filename);
        SampleLockTest lock2 = new SampleLockTest(server2,new String[]{server1,server2}, filename);
        lock1.run();
        lock2.run();

        //2. Test with wait
        System.out.println("-------------------------------------TEST 2----------------------------------");
        FileCopyHelper.reliableCopy(new File(sourceFileName), new File(filename));
        lock1 = new SampleLockTest(server1,new String[]{server1,server2}, filename);
        lock2 = new SampleLockTest(server2,new String[]{server1,server2}, filename);
        lock1.run();
        lock1.run();
        lock1.run();
        lock1.run();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        lock2.run();

        //3. Test with replacement
        System.out.println("-------------------------------------TEST 3----------------------------------");
        lock1 = new SampleLockTest(server1,new String[]{server1,server2}, filename);
        lock2 = new SampleLockTest(server2,new String[]{server1,server2}, filename);
        FileCopyHelper.reliableCopy(new File(sourceFileName), new File(filename));
        lock1.run();
        FileCopyHelper.reliableCopy(new File(new File(sourceFileName).getParentFile(),"build.xml"), new File(filename));
        lock2.run();
    }

    private static class SampleLockTest implements Runnable{
        private String serverId;
        private String[] servers;
        private String fileName;

        private SampleLockTest(String serverId, String[] servers, String fileName) {
            this.serverId = serverId;
            this.servers = servers;
            this.fileName = fileName;
        }

        @Override
        public void run() {
            SharedMoveLock lock = new SharedMoveLock(fileName,servers);
            System.out.println("SERVER ID ="+serverId);
            if(!lock.wasMovedBefore(serverId)){
                lock.confirmMove(serverId);
            }
        }
    } */

    /*

del /q mover1\dest\sqljdbc4.jar
del /q mover1\temp\sqljdbc4.jar
del /q mover2\dest\sqljdbc4.jar
del /q mover2\temp\sqljdbc4.jar
copy sqljdbc4.jar Z:\IN\dev\mover\sqljdbc4.jar

     */
}
