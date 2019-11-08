package ru.sberbank.syncserver2.service.file.cache;

import ru.sberbank.syncserver2.service.core.BackgroundService;
import ru.sberbank.syncserver2.service.file.FileServiceRequest;
import ru.sberbank.syncserver2.service.file.FileServiceResponse;
import ru.sberbank.syncserver2.service.file.cache.data.ChunkInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileCacheConstants;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.log.LogMsg;
import ru.sberbank.syncserver2.service.log.LogMsgComposer;
import ru.sberbank.syncserver2.util.ExecutionTimeProfiler;

import java.io.UnsupportedEncodingException;
import java.text.MessageFormat;
import java.util.*;

/**
 *
 * FileCache.putFile -> AbstractFileLoader.loadDataToCache  -> AbstractFileLoader.loadReportData    ->  AbstractFileLoader.doInit
 *                                                                                                  ->  AbstractFileLoader.doRun
 *                                                          -> AbstractFileLoader.splitAndLoad      ->  SingleFileLoader.processFile
 *                                                                                                  ->  MbrUnzipper.processFile
 *
 * putFile  ->  DatabaseFileLoader.loadFilesFromDatabaseAndUpdateCache
 *
 *  removeFileFromCache ->  AbstractFileLoader.removeFileFromCache  ->  FileCacheDraftSupported.checkMd5OfFilesAndDeleteDraft   ->  SingleFileStatusCacheService.updateFileStatus   ->  doRun
 *                                                                  ->  FileCacheDraftSupported.checkMd5OfFilesAndPublish       ->  SingleFileStatusCacheService.updateFileStatus   ->  doRun
 *                                                                  ->  MbrUnzipper.processFile
 *
 *
 *  getPreview  ->  downloadPreview ->  processRequest  ->  FileService.request
 *
 *  getChunk ->  downloadData    ->  processRequest  ->  FileService.request
 *
 *  getFileList ->  downloadList    ->  processRequest  ->    FileService.request
 *
 *  getAllChunks    ->  FileCacheHelper.getSmallFileData
 *
 *  getFileInfo ->  FileCacheHelper.getSmallFileData
 *              ->  ProxyFileLister.getFileInfo ->  MbrUnzipper.processFile
 *              ->  ProxyFileLister.getOrAddFileInfo ->  MbrUnzipper.processFile
 *
 *
 * @author Sergey Erin
 */
public class FileCache extends BackgroundService {
    private List<AbstractFileLoader> loaders;
    private boolean debugModeWithoutLoadToMemory = false;

    protected Map<String, Map<String, CacheValue>> cache = new HashMap<String, Map<String, CacheValue>>();
    //protected ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    public List<AbstractFileLoader> getLoaders() {
        return loaders;
    }

    public void addLoader(AbstractFileLoader loader) {
        if (loaders == null) {
            loaders = new ArrayList<AbstractFileLoader>();
        }

        if (loader != null) {
            loaders.add(loader);
            loader.setFileCache(this);
        }
    }

    public String isDebugModeWithoutLoadToMemory() {
        return String.valueOf(debugModeWithoutLoadToMemory);
    }

    public void setDebugModeWithoutLoadToMemory(String debugModeWithoutLoadToMemory) {
        this.debugModeWithoutLoadToMemory = Boolean.parseBoolean(debugModeWithoutLoadToMemory);
    }

    public void putFile(FileInfo info, ChunkInfo chunkInfo) {
        Map map = new HashMap();
        map.put(new Integer(0), chunkInfo);
        putFile(info, map);
    }

    public void putFile(FileInfo info, Map<Integer, ChunkInfo> chunks) {
        //1. Skip load if we are in debug mode
        if (debugModeWithoutLoadToMemory) {
            String id = info == null ? "null" : info.getId();
            logger.info("A SPECIAL DEBUG MODE USED - ONLY ONE FILE IN FILE CACHE'S MEMORY IS SUPPORTED - LOADING " + id);
            cache.clear();
        }

        //2. Preparing key and value
        CacheValue value = new CacheValue(info, chunks);
        CacheValue old = null;

        synchronized (cache) {
            Map<String, CacheValue> appFiles = cache.get(info.getApp());
            if (appFiles == null) {
                appFiles = new HashMap<String, CacheValue>();
                cache.put(info.getApp(), appFiles);
            }
            old = appFiles.put(info.getFullFileId(), value);
        }

        //4. Cleaning old values
        if (old != null) {
            old.clearMemory();
        }
        System.gc();
    }

    public void removeFileFromCache(String app, String id) {
        //1. Removing from cache
        CacheValue old = null;
        synchronized (cache) {
            Map<String, CacheValue> appFiles = cache.get(app);
            if (appFiles != null) {
                old = appFiles.remove(id);
            }
        }

        //2. Cleaning old values
        if (old != null) {
            old.clearMemory();
        }
        System.gc();
    }

    protected CacheValue getCacheValueFromAppMap(FileServiceRequest fileServiceRequest, Map<String, CacheValue> appFiles, String fileId, String app) {
        CacheValue result = null;
        result = appFiles.get(fileId);
        // Carry from AbstractFileLoader load data from FileCache directory
        if (result == null) {
            for (AbstractFileLoader loader : loaders) {
                loader.loadReportData(fileId, true);
            }
            appFiles = cache.get(app);
            result = appFiles.get(fileId);
        }
        //
        return result;
    }

    protected CacheValue getCacheValueFromAppMap(Map<String, CacheValue> appFiles, String fileId, String app) {
        return getCacheValueFromAppMap(null, appFiles, fileId, app);
    }

    public Map<String, CacheValue> getCacheValueAppFilesMap(String app, String id) {
        Map<String, CacheValue> appFiles = cache.get(app);
        // Carry from AbstractFileLoader load data from FileCache directory
        if (appFiles == null) {
            for (AbstractFileLoader loader : loaders) {
                loader.loadReportData(id, true);
            }
            appFiles = cache.get(app);
        }
        //
        return appFiles;
    }

    public Object getPreview(FileServiceRequest request) {
        String app = request.getApp();
        String id = request.getId();
        try {
            ExecutionTimeProfiler.start("getPreview");
            synchronized (cache) {
                Map<String, CacheValue> appFiles = getCacheValueAppFilesMap(app, id);
                if (appFiles == null) {
                    String error = format(FileCacheConstants.NO_SUCH_APP, app);
                    return downloadError(request, error);
                }
                CacheValue value = getCacheValueFromAppMap(request, appFiles, id, app);
                if (value == null) {
                    String error = format(FileCacheConstants.NO_SUCH_REPORT, id, app);
                    return downloadError(request, error);
                }

                FileInfo info = value.getFileInfo();
                if (info.getPreviewImage() == null) {
                    synchronized (info) {
                        for (AbstractFileLoader loader : loaders) {
                            byte[] content = loader.loadSinglePreview(id);
                            info.setPreviewImage(content);
                        }
                    }
                }
                return info;
            }
        } finally {
            ExecutionTimeProfiler.finish("getPreview");
        }
    }

    public Object getChunk(FileServiceRequest request) {
        //1. Logging request
        String app = request.getApp();
        String id = request.getId();
        int chunkIndex = request.getChunkIndex();

        try {
            //2.1. Locking
            ExecutionTimeProfiler.start("getChunk");
            synchronized (cache) {

                //2.2. Get file cache
                Map<String, CacheValue> appFiles = getCacheValueAppFilesMap(app, id);
                if (appFiles == null) {
                    String error = format(FileCacheConstants.NO_SUCH_APP, app);
                    return downloadError(request, error);
                }
                CacheValue value = getCacheValueFromAppMap(request, appFiles, id, app);
                if (value == null) {
                    String error = format(FileCacheConstants.NO_SUCH_REPORT, id, app);
                    return downloadError(request, error);
                }

                //2.3. Get chunk
                ChunkInfo info = value.getChunk(chunkIndex);
                if (request.getChunkIndex() == 0) {
                    logUserEvent(LogEventType.FILE_DOWNLOAD_START, request.getUserEmail(), request.getUserIpAddress(), "Start download file", "request = " + request.toString() + " response=" + info);
                } else if (request.getChunkIndex() == value.getChunks().size() - 1) {
                    logUserEvent(LogEventType.FILE_DOWNLOAD_FINISH, request.getUserEmail(), request.getUserIpAddress(), "Finish download file", "request = " + request.toString() + " response=" + info);
                }
                if (info == null) {
                    String error = format(FileCacheConstants.NO_SUCH_CHUNK, app, id, String.valueOf(chunkIndex));
                    return downloadError(request, error);
                }

                //2.4. Load chunk data
                if (!info.isLoaded()) {
                    synchronized (info) {
                        for (AbstractFileLoader loader : loaders) {
                            byte[] content = loader.loadSingleChunk(id, chunkIndex);
                            if (content != null) {
                                info.loadChunkContent(content);
                            }
                        }
                    }
                }

                //2.5. Return data
                return info;
            }
        } finally {
            ExecutionTimeProfiler.finish("getChunk");
        }
    }

    public Map<Integer, ChunkInfo> getAllChunks(String app, String id) {
        try {
            //2.1. Locking
            ExecutionTimeProfiler.start("getAllChunks");
            synchronized (cache) {

                //2.2. Get file cache
                Map<String, CacheValue> appFiles = getCacheValueAppFilesMap(app, id);
                if (appFiles == null) {
                    return null;
                }
                CacheValue value = getCacheValueFromAppMap(appFiles, id, app);
                if (value == null) {
                    return null;
                }

                //2.3. Get chunk
                return value.chunks;
            }
        } finally {
            ExecutionTimeProfiler.finish("getAllChunks");
        }
    }


    public FileInfo getFileInfo(String app, String id) {
        synchronized (cache) {

            //2.2. Get file cache
            Map<String, CacheValue> appFiles = getCacheValueAppFilesMap(app, id);
            if (appFiles == null) {
                return null;
            }
            CacheValue value = getCacheValueFromAppMap(appFiles, id, app);
            if (value == null) {
                return null;
            }

            return value == null ? null : value.fileInfo;
        }
    }


    public FileServiceResponse processRequest(FileServiceRequest request) {
        int command = request.getCommand();
        switch (command) {
            case FileServiceRequest.COMMANDS.DATA:
                return downloadData(request);
            case FileServiceRequest.COMMANDS.LIST:
                return downloadList(request);
            case FileServiceRequest.COMMANDS.PREVIEW:
                return downloadPreview(request);
            default:
                logUserEvent(LogEventType.ERROR, request.getUserEmail(), request.getUserIpAddress(), "downloadUnknown - no such command", request.toString());
                return downloadError(request, FileCacheConstants.NO_SUCH_COMMAND);
        }
    }

    private FileServiceResponse downloadPreview(FileServiceRequest request) {
        try {

            Object previewInfoOrError = getPreview(request);
            if (previewInfoOrError instanceof FileServiceResponse) {
                return (FileServiceResponse) previewInfoOrError;
            }
            FileInfo fileInfo = (FileInfo) previewInfoOrError;

            //2. Extracting content
            byte[] content = null;
            StringBuilder title = new StringBuilder();
            if (fileInfo != null) {
                content = fileInfo.getPreviewImage();
                title.append(fileInfo.getCaption());
            }
            FileServiceResponse response = new FileServiceResponse(FileServiceRequest.COMMANDS.PREVIEW, title.toString(), content);
            return response;
        } catch (Exception e) {
            logLoaderError("Unexpected error", e);
            logUserEvent(LogEventType.ERROR, request.getUserEmail(), request.getUserIpAddress(), "Unexpected error", "Unexpected error " + request.toString(), e);
        } finally {
            //tagLogger.log("FileCache","FINISH DATA REQUEST: "+request.toString());
        }


        return new FileServiceResponse(FileServiceRequest.COMMANDS.PREVIEW, "Preview not available!", null);
    }

    private FileServiceResponse downloadList(FileServiceRequest request) {
        //1. Declaring
        //tagLogger.log("FileCache","START LIST REQUEST: "+request.toString());
        LogMsg dbLogMsg = LogMsgComposer.composeStartLogMsg(request.getUserEmail(), null, request.getDeviceId(), LogEventType.FILE_LIST_START, "Start downloading list of files for " + request.getApp(), request.getUserIpAddress(), request.getId());
        //dbLogger.log(dbLogMsg);
        logServiceMessage(LogEventType.FILE_LIST_START, dbLogMsg.toString());

        //2. Collecting list of files
        try {
            synchronized (cache) {
                FileInfoList result = getFileList(null, request);
                return new FileServiceResponse(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FileServiceResponse(new FileInfoList(Collections.EMPTY_LIST));
    }

    public List<String> getAppList() {
        //ReentrantReadWriteLock.ReadLock lock = null;
        try {
            synchronized (cache) {
                //2.2. Copy array
                Set appList = cache.keySet();
                ArrayList<String> result = new ArrayList<String>(appList);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.EMPTY_LIST;
    }

    public FileInfoList getFileList(String app) {
        return getFileList(app, null);
    }

    public FileInfoList getFileList(String app, FileServiceRequest fileServiceRequest) {
        if (fileServiceRequest != null)
            app = fileServiceRequest.getApp();
        //2.2. Get file cache
        Map<String, CacheValue> appFiles = cache.get(app);
        List<CacheValue> values = appFiles == null ? Collections.EMPTY_LIST : new ArrayList<CacheValue>(appFiles.values());

        //
        //Если в запросе передано имя файла, то в общей куче ищем файл по имени, и возвращаем список только с этим файлом
        if (fileServiceRequest != null && fileServiceRequest.getName() != null) {
            for (CacheValue cv : values) {
                if (fileServiceRequest.getName().equals(cv.getFileInfo().getName())) {
                    values = new ArrayList<CacheValue>();
                    values.add(cv);
                    break;
                }
            }
        }
        //

        List<FileInfo> fileInfos = new ArrayList();
        for (int i = 0; i < values.size(); i++) {
            CacheValue cacheValue = values.get(i);
            FileInfo fileInfo = cacheValue.getFileInfo();
            fileInfo = processSkipPreview(fileServiceRequest, fileInfo);
            fileInfos.add(fileInfo);
        }

        //3. Compose result
        FileInfoList result = new FileInfoList(fileInfos);
        return result;
    }

    FileInfo processSkipPreview(FileServiceRequest request, FileInfo fileInfo) {
        if (request != null && request.isSkipPreview()) {
            fileInfo = (FileInfo) fileInfo.clone();
            fileInfo.setPreview(null);
            fileInfo.setPreviewImage(null);
        }
        return fileInfo;
    }

    private FileServiceResponse downloadData(FileServiceRequest request) {
        try {
            //1. Finding chunk
            //tagLogger.log("FileCache","START DATA REQUEST: "+request.toString());
            Object chunkInfoOrError = getChunk(request);
            if (chunkInfoOrError instanceof FileServiceResponse) {
                return (FileServiceResponse) chunkInfoOrError;
            }
            ChunkInfo chunkInfo = (ChunkInfo) chunkInfoOrError;

            //2. Extracting cotrent
            byte[] content = null;
            StringBuilder title = new StringBuilder();
            if (chunkInfo != null) {
                content = chunkInfo.getChunkContent();
                title.append(chunkInfo.getChunkTitle());
            }
            FileServiceResponse response = new FileServiceResponse(title.toString(), content);
            return response;
        } catch (Exception e) {
            logLoaderError("Unexpected error", e);
            logUserEvent(LogEventType.ERROR, request.getUserEmail(), request.getUserIpAddress(), "Unexpected error", "Unexpected error " + request.toString(), e);
        } finally {
            //tagLogger.log("FileCache","FINISH DATA REQUEST: "+request.toString());
        }

        //3. Processing unknown error
        return downloadError(request, FileCacheConstants.UNKNOWN_ERROR);
    }

    private FileServiceResponse downloadError(FileServiceRequest request, String error) {
        byte[] utf8Bytes = getUTF8Bytes(error);
        String msg = "Downloading error: request=" + request + ", error=" + error;
        logLoaderMessage(msg);
        FileServiceResponse response = new FileServiceResponse(request.getCommand(), "", utf8Bytes);
        response.setError(true);
        logUserEvent(LogEventType.ERROR, request.getUserEmail(), request.getUserIpAddress(), "Downloading error", msg);
        return response;
    }

    @Override
    public void doStop() {
        logServiceMessage(LogEventType.SERV_STOP, "stopping service");
        cache.clear();
        logServiceMessage(LogEventType.SERV_STOP, "stopped service");
    }

    @Override
    protected void doStart() {
        logServiceMessage(LogEventType.SERV_START, "starting service");
        logServiceMessage(LogEventType.SERV_START, "started service");
    }

    protected static class CacheValue {
        private FileInfo fileInfo = null;
        private Map<Integer, ChunkInfo> chunks = new HashMap<Integer, ChunkInfo>();

        protected CacheValue(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        protected CacheValue(FileInfo fileInfo, Map<Integer, ChunkInfo> chunks) {
            this.fileInfo = fileInfo;
            this.chunks = chunks;
        }

        public FileInfo getFileInfo() {
            return fileInfo;
        }

        public void setFileInfo(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        public Map<Integer, ChunkInfo> getChunks() {
            return chunks;
        }

        public void setChunks(Map<Integer, ChunkInfo> chunks) {
            this.chunks = chunks;
        }

        public ChunkInfo getChunk(Integer chunkIndex) {
            return chunks.get(chunkIndex);
        }

        public ChunkInfo getChunk(String chunkIndex) {
            int index = Integer.parseInt(chunkIndex);
            return chunks.get(index);
        }

        /*
        This function should be done by loader
        public void clearFileSystem() {
            if(fileInfo==null || fileInfo.getId()==null){
                return;
            }

            for (int i = 0; i < loaders.size(); i++) {
                AbstractFileLoader loader = loaders.get(i);
                loader.removeFileFromCache(fileInfo.getApp(),fileInfo.getId());
            }
        }*/


        public void clearMemory() {
            if (chunks != null) {
                for (Iterator iterator = chunks.values().iterator(); iterator.hasNext(); ) {
                    ChunkInfo chunkInfo = (ChunkInfo) iterator.next();
                    chunkInfo.clear();
                }
                chunks.clear();
                chunks = null;
            }
            fileInfo = null;
        }
    }

    public static String format(String fmt, String... args) {
        MessageFormat mf = new MessageFormat(fmt);
        return mf.format(args);
    }

    public static byte[] getUTF8Bytes(String s) {
        try {
            return s.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public void logLoaderMessage(String txt) {
        logger.info(txt);

    }

    public void logLoaderError(String txt, Throwable t) {
        logger.error(txt, t);
    }

}

