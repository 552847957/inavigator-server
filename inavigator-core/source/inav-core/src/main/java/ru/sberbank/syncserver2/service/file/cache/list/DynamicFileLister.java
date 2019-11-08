package ru.sberbank.syncserver2.service.file.cache.list;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ru.sberbank.syncserver2.service.core.BackgroundService;
import ru.sberbank.syncserver2.service.core.config.StaticFileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;
import ru.sberbank.syncserver2.service.file.cache.zip.MbrZippedFile;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.xml.bind.JAXBException;

/**
 * This class is not derived from FileLister because it has a different key a
 * and there is no intersection in functionality
 */
public class DynamicFileLister extends BackgroundService {
    private AtomicLong fileIdSequence = new AtomicLong(0);
	private File statusFile;

    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private HashMap<AppAndCaptionKey,FileInfo> actualFileInfos = new HashMap<AppAndCaptionKey, FileInfo>();

	public DynamicFileLister() {
	}

    @Override
    public void doStart() {
        //1. Anyway folder for file should be created
        File folder = statusFile.getParentFile();
        folder.mkdirs();

        //2. Restoring from backup. Backup exists only if something goes wrong during last change
        File bakFile = new File(statusFile+".bak");
        if(bakFile.exists()){
            try {
                FileCopyHelper.reliableCopy(bakFile, statusFile);
                FileCopyHelper.reliableDelete(bakFile);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //3. Reading from main file
        if(statusFile.exists()){
            try {
                actualFileInfos = (HashMap) FileHelper.readObject(statusFile);
                if(actualFileInfos!=null){
                    long maxFileId = getMaximumFileId(actualFileInfos.values());
                    fileIdSequence.set(maxFileId);
                } else {
                    actualFileInfos = new HashMap<AppAndCaptionKey, FileInfo>();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doStop() {
        logServiceMessage(LogEventType.SERV_STOP, "stopped service");
    }

/*
    public File getStatusFile() {
		return statusFile;
	}
*/


	public void setStatusFile(String statusFile) {
		this.statusFile = new File(statusFile);
	}

    public synchronized void removeFileFromList(String app, String caption){
        //1. Get a local copy of actual fileInfo
        HashMap<AppAndCaptionKey, FileInfo> localCopy = getLocalCopy();

        //2. Removing
        AppAndCaptionKey key = new AppAndCaptionKey(app,caption);
        localCopy.remove(key);

        //3. Saving
        saveLocalCopy(localCopy);
    }

    public synchronized void update(FileInfoList fileInfoList){
        //1. Get a local copy of actual fileInfo
        HashMap<AppAndCaptionKey, FileInfo> localCopy = getLocalCopy();

        //2. Updating file info in a local copy
        List<FileInfo> fileInfos = fileInfoList.getReportStatuses();
        for (int i = 0; i < fileInfos.size(); i++) {
            FileInfo fileInfo =  fileInfos.get(i);
            FileInfo fileInfoCopy = (FileInfo) fileInfo.clone();
            AppAndCaptionKey key = new AppAndCaptionKey(fileInfo.getApp(), fileInfo.getCaption());
            if(fileInfo.isRemoved()){
                //System.out.println("REMOVING "+key);
                localCopy.remove(key);
            } else {
                FileInfo existing = localCopy.get(key);
                if(existing!=null){
                    fileInfoCopy.setId(existing.getId());
                } else {
                    long fileId  = fileIdSequence.addAndGet(1);
                    fileInfoCopy.setId(String.valueOf(fileId));
                }
                //System.out.println("REPLACING "+key);
                localCopy.put(key, fileInfoCopy);
            }
        }

        //3. Saving updated status to temp and then replacing permanent
        saveLocalCopy(localCopy);
    }

    private HashMap<AppAndCaptionKey, FileInfo> getLocalCopy() {
        HashMap<AppAndCaptionKey,FileInfo> localCopy = null;
        try {
            lock.writeLock().lock();
            localCopy = (HashMap<AppAndCaptionKey, FileInfo>) actualFileInfos.clone();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
        return localCopy;
    }

    private void saveLocalCopy(HashMap<AppAndCaptionKey, FileInfo> localCopy) {
        try {
            File bakFile = new File(statusFile.getAbsolutePath()+".bak");
            File newFile = new File(statusFile.getAbsolutePath()+".tmp");
            FileHelper.writeObject(localCopy, newFile);
            FileCopyHelper.reliableDelete(bakFile);
            FileCopyHelper.reliableMove(statusFile, bakFile);//file is deleted only after MD5 matches
            FileCopyHelper.reliableMove(newFile, statusFile);//newFile is deleted only after MD5 matches
            FileCopyHelper.reliableDelete(bakFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //4. Replacing copy in memory
        try {
            lock.writeLock().lock();
            actualFileInfos = localCopy;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public FileInfo getFileInfo(String app, String caption){
        AppAndCaptionKey key = new AppAndCaptionKey(app, caption);
        try {
            lock.readLock().lock();
            return actualFileInfos.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.readLock().unlock();
        }
        return null;
    }

    private static class AppAndCaptionKey implements Serializable{
        private String app;
        private String caption;

        public AppAndCaptionKey(String app, String caption) {
            this.app = app;
            this.caption = caption;
        }

        public String getApp() {
            return app;
        }

        public void setApp(String app) {
            this.app = app;
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AppAndCaptionKey)) return false;

            AppAndCaptionKey that = (AppAndCaptionKey) o;

            if (app != null ? !app.equals(that.app) : that.app != null) return false;
            if (caption != null ? !caption.equals(that.caption) : that.caption != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = app != null ? app.hashCode() : 0;
            result = 31 * result + (caption != null ? caption.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "AppAndCaptionKey{" +
                    "app='" + app + '\'' +
                    ", caption='" + caption + '\'' +
                    '}';
        }
    }

    private static long getMaximumFileId(Collection<FileInfo> infos){
        long id = 0;
        for (Iterator<FileInfo> fileInfoIterator = infos.iterator(); fileInfoIterator.hasNext(); ) {
            FileInfo next = fileInfoIterator.next();
            String fileId = next.getId();
            Long longFileId = Long.valueOf(fileId);
            if(longFileId.longValue()>id){
                id = longFileId;
            }
        }
        return id;
    }

    public static void main(String[] args) throws JAXBException, IOException {
        DynamicFileLister dynamicFileLister = new DynamicFileLister();
        dynamicFileLister.setStatusFile("C:\\usr\\cache\\dev\\dynamicFileLister\\mbrList\\list.xml");
        File[] files = new File("C:\\usr\\cache\\dev\\dynamicFileLister\\inbox\\").listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            MbrZippedFile zmf = new MbrZippedFile(file);
            FileInfoList  zmfList = zmf.getFileInfoList();
            System.out.println("START UPDATING FILE LIST FROM "+file);
            dynamicFileLister.update(zmfList);
            System.out.println("FINISH UPDATING FILE LIST FROM " + file);
            System.out.println(dynamicFileLister.actualFileInfos);
        }
    }
}
