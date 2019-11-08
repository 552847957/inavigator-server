package ru.sberbank.syncserver2.service.file.cache.list;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.BackgroundService;
import ru.sberbank.syncserver2.service.file.cache.FileCache;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by sbt-kozhinsky-lb on 09.07.14.
 */
public class ProxyFileLister  {
    private FileCache fileCache;
    private HashMap<AppAndCaptionKey,String> appAndCaptionToId = new HashMap<AppAndCaptionKey, String>();
    private AtomicLong fileIdSequence = new AtomicLong(0);

    public ProxyFileLister(FileCache fileCache) {
        //1. Set file cache
        this.fileCache = fileCache;

        //2. For all apps we filling map
        synchronized(this){
            long minimumFileId = 0;
            List<String> apps = fileCache.getAppList();
            for (int i = 0; i < apps.size(); i++) {
                String app =  apps.get(i);
                FileInfoList infoList = fileCache.getFileList(app);
                List<FileInfo> infos = infoList.getReportStatuses();
                for (int j = 0; j < infos.size(); j++) {
                    //2.1. Adding key
                    FileInfo fileInfo =  infos.get(j);
                    String caption = fileInfo.getCaption();
                    String id = fileInfo.getId();
                    AppAndCaptionKey key = new AppAndCaptionKey(app,caption);
                    appAndCaptionToId.put(key, id);

                    //2. Update minimum id
                    try {
                        long longId = Long.parseLong(id);
                        if(longId>minimumFileId){
                            minimumFileId = longId;
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                fileIdSequence.set(minimumFileId);
            }
        }
    }

    public synchronized FileInfo getFileInfo(String app, String caption) {
        AppAndCaptionKey key = new AppAndCaptionKey(app, caption);
        String id = appAndCaptionToId.get(key);
        return id==null ? null:fileCache.getFileInfo(app, id);
    }

    public synchronized FileInfo getOrAddFileInfo(FileInfo alphaFileInfo) {
        //1. Getting existing fileId or register new
        String app = alphaFileInfo.getApp();
        String caption = alphaFileInfo.getCaption();
        AppAndCaptionKey key = new AppAndCaptionKey(app, caption);
        String fileId = appAndCaptionToId.get(key);
        if(fileId==null){
            fileId = getNextFileId();
            appAndCaptionToId.put(key, fileId);
        }

        //2. Creating file info
        FileInfo sigmaFileInfo = fileCache.getFileInfo(app,fileId);
        if(sigmaFileInfo==null){
            sigmaFileInfo = (FileInfo) alphaFileInfo.clone();
        }
        sigmaFileInfo.setId(fileId);
        return sigmaFileInfo;
    }

    public String getNextFileId(){
        long value = fileIdSequence.addAndGet(1);
        return String.valueOf(value);
    }

    public void removeFileFromLister(String app, String caption) {
        AppAndCaptionKey key = new AppAndCaptionKey(app, caption);
        appAndCaptionToId.remove(key);
    }

    private static class AppAndCaptionKey implements Serializable {
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

}
