package ru.sberbank.syncserver2.service.file.cache;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import ru.sberbank.syncserver2.service.core.FileServiceWithDpLogging;
import ru.sberbank.syncserver2.service.core.event.impl.FileLoadedToFileCacheEventInfo;
import ru.sberbank.syncserver2.service.file.cache.data.ChunkInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.generator.single.data.ActionState;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.FileHelper;
import ru.sberbank.syncserver2.util.FormatHelper;
import ru.sberbank.syncserver2.util.MD5Helper;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.zip.Deflater;


public abstract class AbstractFileLoader extends FileServiceWithDpLogging implements FileLoader {
    public static final String FILE_INFO_TMP = "fileinfo.tmp";
    public static final String FILE_INFO_READY = "fileinfo.ready";
    public static final String CHUNK_INFO = "chunkinfo.ready";
    public static final String PREVIEW = "preview.png";

    protected String chunkSize;
    protected String inboxFolder;
    protected String tempFolder;
    protected String archiveFolder;
    protected String cacheFolder;
    protected String serviceCode;
    protected boolean loading = true;

    protected SingleFileStatusCacheService fileStatusCacheService;

    protected FileCache fileCache;

    private String localHostName;

    private ByteArrayOutputStream buffer = new ByteArrayOutputStream(4 * 1024 * 1024);

    private boolean formatWithTimeZone = false;

    protected AbstractFileLoader() {
        super(60); //10 seconds to waite between executions
        try {
            localHostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } finally {
        }
    }

    public FileCache getFileCache() {
        return fileCache;
    }

    public void setFileCache(FileCache fileCache) {
        this.fileCache = fileCache;
    }

    public String getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(String chunkSize) {
        this.chunkSize = chunkSize;
    }

    public String getInboxFolder() {
        return inboxFolder;
    }

    public void setInboxFolder(String inboxFolder) {
        this.inboxFolder = inboxFolder;
    }

    public String getTempFolder() {
        return tempFolder;
    }

    public void setTempFolder(String tempFolder) {
        this.tempFolder = tempFolder;
    }

    public String getArchiveFolder() {
        return archiveFolder;
    }

    public void setArchiveFolder(String archiveFolder) {
        this.archiveFolder = archiveFolder;
    }

    public String getCacheFolder() {
        return cacheFolder;
    }

    public void setCacheFolder(String cacheFolder) {
        this.cacheFolder = cacheFolder;
    }

    public void setLoading(boolean loading) { //used for unit testing only
        this.loading = loading;
    }

    public String getFormatWithTimeZone() {
        return String.valueOf(formatWithTimeZone);
    }

    public void setFormatWithTimeZone(String formatWithTimeZone) {
        this.formatWithTimeZone = Boolean.parseBoolean(formatWithTimeZone);
    }

    public String getLocalhostName() {
        return localHostName;
    }


    public SingleFileStatusCacheService getFileStatusCacheService() {
        return fileStatusCacheService;
    }

    public void setFileStatusCacheService(
            SingleFileStatusCacheService fileStatusCacheService) {
        this.fileStatusCacheService = fileStatusCacheService;
    }

    public void doInit() {
        //1. Getting serviceBeanCode
        serviceCode = getServiceBeanCode();

        //Code for standalone testing below instead of 1
        //  serviceCode = "test"; //getServiceBeanCode();
        //  super.tagLogger = TagLogger.getTagLogger(SingleFileLoader.class);


        //2. Loading headers from cache
        String[] tags = new String[]{serviceCode};
        tagLogger.log(tags, "Start loading headers from cache");
        File[] files = new File(cacheFolder).listFiles();
        if (files == null || files.length == 0) {
            tagLogger.log(serviceCode, "Finish loading headers from cache - no files found");
            return;
        }
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            boolean loadData = false; //loading only headers
            loadReportData(file.getName(), loadData);
        }

        //3. Loading of data happen in doRun in loading mode
        tagLogger.log(serviceCode, "Finish loading headers from cache");
        loading = true;

    }

    @Override
    public void doRun() {
        if (loading) {
            //1. Loading headers from cache
            super.setLastActionComment("Initial loading data from cache");
            tagLogger.log(serviceCode, "Start loading data from cache");
            try {
                File[] files = new File(cacheFolder).listFiles();
                if (files == null || files.length == 0) {
                    return;
                }
                for (int i = 0; i < files.length; i++) {
                    boolean loadData = true; //loading data
                    loadReportData(files[i].getName(), loadData);
                }
            } finally {
                loading = false;
                tagLogger.log(serviceCode, "Finish loading data from cache");
            }
        } else {
            //2. Load data from inbox folder - only after cache is fully loaded
            //2.1. Moving files from inbox folder to temp folder
            super.setLastActionComment("Moving files from " + inboxFolder + " to " + tempFolder);
            tagLogger.log(serviceCode, "Start moving files from " + inboxFolder + " to " + tempFolder);
            File[] files = new File(inboxFolder).listFiles();
            if (files != null && files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    File src = files[i];
                    File dst = new File(tempFolder, files[i].getName());
                    tagLogger.log(new String[]{files[i].getName()}, "Moving file from " + src + " to " + dst);
//                    FileCopyHelper.reliableMove(src, dst);
                    FileCopyHelper.reliableDelete(dst);
                    FileCopyHelper.loggableMkdirs(dst.getParentFile());
                    src.renameTo(dst);
                }
                tagLogger.log(serviceCode, "Finish moving files from " + inboxFolder + " to " + tempFolder);
            } else {
                tagLogger.log(serviceCode, "Finish moving files from " + inboxFolder + " to " + tempFolder + " - no files found");
            }

            //2.2. Processing files in temp folder
            //2.2.1. Check if the any file
            super.setLastActionComment("Start processing files in " + tempFolder);
            tagLogger.log(serviceCode, "Start processing files in " + tempFolder);
            files = new File(tempFolder).listFiles();
            if (files == null || files.length == 0) {
                tagLogger.log(serviceCode, "Finish processing files in " + tempFolder + " - no files found");
                return;
            }

            //2.2. Reading and split file by file
            for (File file : files) {
                if (!file.isDirectory()) {
                    super.setLastActionComment("Processing " + tempFolder);
                    processFile(file);
                }
                if (shouldInternalTaskStop()) {
                    return;
                }
            }
        }
    }

    protected abstract void processFile(File src);

    public void loadReportData(String folder, boolean loadData) {
        try {
            //1. Check if FILE_INFO_READY exists and drop folder if it is not
            File file = new File(cacheFolder, folder);
            File fileInfoFile = new File(file, FILE_INFO_READY);
            if (!fileInfoFile.exists()) {
                String msg = file + " with incompleted file info";
                dropInvalidReport(file, msg);
                return;
            }

            //2. Loading all headers
            FileInfo fileInfo = (FileInfo) FileHelper.readObject(fileInfoFile);
            File chunkInfoFile = new File(file, CHUNK_INFO);
            Set chunkInfoSet = (Set) FileHelper.readObject(chunkInfoFile.getAbsolutePath());
            if (fileInfo == null || chunkInfoSet == null) {
                String msg = file + " with corrupted file info and/or chunk info";
                dropInvalidReport(file, msg);
                return;
            }

            //3. Replacing reference in files to same file info
            Map<Integer, ChunkInfo> chunkInfoMap = new HashMap<Integer, ChunkInfo>();
            for (Iterator iterator = chunkInfoSet.iterator(); iterator.hasNext(); ) {
                ChunkInfo chunk = (ChunkInfo) iterator.next();
                chunk.setLoaded(false); // will be changed to true in loadChunkContent
                chunk.setFileInfo(fileInfo);
                chunk.setLoader(this);
                chunkInfoMap.put(chunk.getChunkIndex(), chunk);
            }

            //4. Loading data if necessary
            if (loadData) {
                super.setLastActionComment("Loading " + folder);
                for (Iterator iterator = chunkInfoMap.values().iterator(); iterator.hasNext(); ) {
                    ChunkInfo chunkInfo = (ChunkInfo) iterator.next();
                    byte[] data = loadSingleChunk(folder, chunkInfo.getChunkIndex());
                    chunkInfo.loadChunkContent(data); //if data is null then it sets loaded to false
                    if (data == null) {
                        tagLogger.log(new String[]{serviceCode, file.getName()}, " Chunk " + chunkInfo.getChunkIndex() + " for " + fileInfo.getId() + " is corrupted. Manual reloading from archive should be done");
                    } else {
                        try {
                            String md5FromData = MD5Helper.getCheckSumAsString(data);
                            if (!md5FromData.equalsIgnoreCase(chunkInfo.getChunkMd5())) {
                                //TODO - in this case we should 1) drop corrupted chunks 2) reload from archive
                                tagLogger.log(new String[]{serviceCode, file.getName()}, " Chunk " + chunkInfo.getChunkIndex() + " for " + fileInfo.getId() + " is corrupted. Manual reloading from archive should be done");
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    if (shouldInternalTaskStop()) {
                        return;
                    }
                }
            }

            //5. Put data to cache
            loadDataToCache(fileInfo, chunkInfoMap, null);
        } catch (Exception e) {
            e.printStackTrace();
            tagLogger.log(new String[]{serviceCode, folder}, " Unexpected error while loading " + folder + " : " + e.getMessage());
        }
    }

    private void dropInvalidReport(File file, String msg) {
        tagLogger.log(new String[]{serviceCode, file.getName()}, " Skip loading folder " + msg);
        FileCopyHelper.reliableDeleteFolderContent(file);
        FileCopyHelper.reliableDelete(file);
        tagLogger.log(new String[]{serviceCode, file.getName()}, " Deleted content of folder " + msg);
    }

    private void loadDataToCache(FileInfo fileInfo, Map<Integer, ChunkInfo> chunks, String fileName) {
        super.setLastActionComment("Loading " + fileName + " to cache ");
        String id = fileInfo.getId();
        String app = fileInfo == null ? "null" : fileInfo.getApp();
        String caption = fileInfo == null ? "null" : fileInfo.getCaption();
        String[] tags = fileName == null ? new String[]{serviceCode, id, caption} : new String[]{serviceCode, id, caption, fileName};
        tagLogger.log(tags, "Start loading file " + id + " for app " + app + " to cache " + fileCache);
        if (fileCache != null) {
            fileCache.putFile(fileInfo, chunks);
            tagLogger.log(tags, "Finish loading file " + id + " with caption " + caption + " for app " + app + " to cache " + fileCache + " with success");
        } else {
            tagLogger.log(tags, "Finish loading file " + id + " with caption " + caption + " for app " + app + " to cache with warning : cache was not found");

            //Code for standalone testing is commented below
            //List chunkInfos = new ArrayList(chunks.values());
            //for (int i = 0; i < chunkInfos.size(); i++) {
            //    ChunkInfo chunkInfo = (ChunkInfo) chunkInfos.get(i);
            //    System.out.println("MD5 for index "+i+" is equal "+chunkInfo.getChunkMd5());
            //}
        }
    }

    protected boolean splitAndLoad(FileInfo fileInfo, File file) throws Exception {
        //1. Parsing chunk size
        getDatapowerNotificationLogger().addGenStaticFileEvent(fileInfo.getName(), ActionState.STATUS_PERFORM, localHostName);
        logObjectEventToDataPower(LogEventType.GEN_CACHING_START, fileInfo.getName(), "Start loading " + fileInfo.getName() + " to cache at server in Sigma");
        logObjectEventToDataPower(LogEventType.GEN_DEBUG, fileInfo.getName(), "Start splitting " + fileInfo.getName() + " in chunks");
        super.setLastActionComment("Start splitting " + fileInfo.getName() + " in chunks");
        int MAX_BUFFER_CHUNK_SIZE = 1024 * 1024;
        try {
            MAX_BUFFER_CHUNK_SIZE = Integer.parseInt(chunkSize);
        } catch (NumberFormatException e) {
        }

        //1. Create preview
        PDDocument document = null;
        try {
            document = PDDocument.load(file);
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            fileInfo.setPreview(pdfRenderer.renderImage(0, 0.4f));
            logObjectEventToDataPower(LogEventType.GEN_DEBUG, fileInfo.getName(), "Сформировали image предпросмотра.");
        } catch (Exception ex) {
            //On any exception not create preview
            logObjectEventToDataPower(LogEventType.GEN_DEBUG, fileInfo.getName(), "Не возможно сформировать image предпросмотра.");
            ex.printStackTrace();
        } finally {
            if (document != null) {
                document.close();
            }

        }
        //

        //2. Calculating file's MD5
        String md5 = MD5Helper.toHexString(MD5Helper.getCheckSumAsBytes(file.getAbsolutePath()));
        String fileName = file.getName(); // we will need it later to report to datapower
        fileInfo.setDataMD5(md5);
        fileInfo.setFileLength(file.length());

        // обновляем статус файла если задан кеширующий сервис
        if (fileStatusCacheService != null)
            fileStatusCacheService.updateFileInfoStatusByCachedValue(fileInfo);

        //3. Reading file, splitting it and accumulating chunks
        //4.1. Declaring
        Map<Integer, ChunkInfo> chunks = new HashMap<Integer, ChunkInfo>();
        byte[] readBuffer = new byte[MAX_BUFFER_CHUNK_SIZE];
        byte[] tempBuffer = new byte[MAX_BUFFER_CHUNK_SIZE * 2]; //in some case zipping increase size of data
        byte[] zippedData;
        int readBufferLength = 0;
        long unzippedOffset = 0;
        InputStream bis = null;
        InputStream fis = null;
        try {
            int chunkIndex = 0;
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis, readBuffer.length);
            do {
                //2.1. Reading next chunk to readBuffer
                bis.mark(readBuffer.length);
                readBufferLength = IOUtils.read(bis, readBuffer);

                //2.1. Zipping to array
                synchronized (buffer) {
                    buffer.reset();
                    Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);
                    deflater.setInput(readBuffer, 0, readBufferLength);
                    deflater.finish();
                    while (!deflater.finished()) {
                        int count = deflater.deflate(tempBuffer);
                        buffer.write(tempBuffer, 0, count);
                    }
                    zippedData = buffer.toByteArray();
                    buffer.reset();
                }

                //2.2. Calc MD5 for chunk and calc
                synchronized (buffer) {
                    try {
                        if (zippedData.length > readBufferLength) {
                            tagLogger.log(new String[]{serviceCode, file.getName()}, "Deflating is not effecient: original size is " + readBufferLength + " and zipped size is " + zippedData.length);
                        }
                        String chunkMd5 = MD5Helper.getCheckSumAsString(zippedData);
                        String unzippedChunkMd5 = MD5Helper.getCheckSumAsString(readBuffer, 0, readBufferLength);
                        ChunkInfo chunkInfo = new ChunkInfo(chunkIndex, unzippedOffset, readBufferLength, zippedData, chunkMd5, unzippedChunkMd5, fileInfo, this);
                        chunkInfo.setLoaded(true);
                        chunks.put(chunkIndex, chunkInfo);
                        chunkIndex++;
                    } finally {
                        buffer.reset();
                    }
                    if (shouldInternalTaskStop()) {
                        return false;
                    }
                }
                unzippedOffset += readBufferLength;
            } while (readBufferLength == readBuffer.length);

            //3. Complete filling of file into
            String sLastModified = formatWithTimeZone ? FormatHelper.formatDateTimeWithTimeZone(new Date(file.lastModified()))
                    : FormatHelper.formatDateTime(new Date(file.lastModified()));
            fileInfo.setLastModified(sLastModified);
            fileInfo.setChunkCount(String.valueOf(chunkIndex));
            String[] md5s = calcMD5OfMD5(chunks.values());
            fileInfo.setMd5OfMd5(md5s[0]);
            fileInfo.setUnzippedMd5OfMd5(md5s[1]);
        } finally {
            FileHelper.close(bis);
            IOUtils.closeQuietly(fis);
        }

        //3. Handing loaded data to singleFileLoader
        logObjectEventToDataPower(LogEventType.GEN_DEBUG, fileName, "Start loading file " + fileName + " to memory cache");
        loadDataToCache(fileInfo, chunks, file.getName());
        logObjectEventToDataPower(LogEventType.GEN_DEBUG, fileName, "Finish loading file " + fileName + " to memory cache");

        //4. Dumping files to catalog
        logObjectEventToDataPower(LogEventType.GEN_DEBUG, fileName, "Start saving chunks for " + fileName + " to disk");
        saveFileInChunks(fileInfo, chunks);
        //System.out.println("Finish saving file "+file.getName()+" to disk");
        logObjectEventToDataPower(LogEventType.GEN_DEBUG, fileName, "Finish saving chunks for " + fileName + " to disk");
        logObjectEventToDataPower(LogEventType.GEN_CACHING_FINISH, fileName, "Finish loading " + fileName + " to cache at server " + localHostName + " in Sigma");
        getDatapowerNotificationLogger().addGenStaticFileEvent(fileInfo.getName(), ActionState.STATUS_COMPLETED_SUCCESSFULLY, localHostName);

        // вызываем обработку события загрузки файл в fileCache
        getServiceContainer().getServiceManager().getSystemEventHandler().performEvent(new FileLoadedToFileCacheEventInfo(fileInfo));

        return true;
    }

    private void saveFileInChunks(FileInfo fileInfo, Map<Integer, ChunkInfo> chunks) {
        //1. Finding folder with right report id and dropping everything in it
        File folder = new File(cacheFolder, fileInfo.getFullFileId());
        if (folder.exists()) {
            FileCopyHelper.reliableDeleteFolderContent(folder);
        } else {
            folder.mkdirs();
        }

        //2. Saving chunks
        for (Iterator<ChunkInfo> iterator = chunks.values().iterator(); iterator.hasNext(); ) {
            ChunkInfo info = iterator.next();
            String chunkFileName = composeChunkFileName(info.getChunkIndex());
            File chunkFile = new File(folder, chunkFileName);
            FileHelper.writeBinary(info.getChunkContent(), chunkFile.getAbsolutePath());
        }

        //3. Saving information about chunks
        Set<ChunkInfo> chunkInfoWithoutData = new HashSet<ChunkInfo>();
        for (Iterator iterator = chunks.values().iterator(); iterator.hasNext(); ) {
            ChunkInfo chunkInfo = (ChunkInfo) iterator.next();
            ChunkInfo headerInfoOnly = chunkInfo.getHeaderInfoOnly();
            chunkInfoWithoutData.add(headerInfoOnly);
        }
        File chunkInfoFile = new File(folder, CHUNK_INFO);
        FileHelper.writeObject((Serializable) chunkInfoWithoutData, chunkInfoFile);

        //4. Saving file info
        File fiTempFile = new File(folder, FILE_INFO_TMP);
        FileHelper.writeObject(fileInfo, fiTempFile);


        try {
            if (fileInfo.getPreview() != null) {
                File previewFile = new File(folder, PREVIEW);
                ImageIOUtil.writeImage(fileInfo.getPreview(), previewFile.getAbsolutePath(), 127);
                logObjectEventToDataPower(LogEventType.GEN_DEBUG, fileInfo.getName(), "Записали image предпросмотра в файл на диск.");
            }
            fileInfo.setPreview(null);
        } catch (Exception exc) {
            //On all exception nothing to do
            logObjectEventToDataPower(LogEventType.GEN_DEBUG, fileInfo.getName(), "Не возможно записать image предпросмотра в файл на диск.");
            exc.printStackTrace();
        }

        //5. Finish operation by renaming file info
        File fiFinalFile = new File(folder, FILE_INFO_READY);
        FileCopyHelper.reliableMove(fiTempFile, fiFinalFile);
    }

    public byte[] loadSingleChunk(String folder, int chunkIndex) {
        String chunkFileName = composeChunkFileName(chunkIndex);
        File chunkFile = new File(new File(cacheFolder, folder), chunkFileName);
        if (chunkFile.exists()) {
            byte[] data = FileHelper.readBinary(chunkFile);
            return data;
        }

        return null;
    }

    public byte[] loadSinglePreview(String folderId) {
        File previewPath = new File(cacheFolder, folderId);
        File previewFile = new File(previewPath, PREVIEW);
        if (previewFile.exists()) {
            byte[] data = FileHelper.readBinary(previewFile);
            return data;
        }
        return null;
    }

    private static String composeChunkFileName(int chunkIndex) {
        return "chunk" + chunkIndex;
    }

    public void removeFileFromCache(String app, String id) {
        //1. Clear memory
        if (fileCache != null) {
            fileCache.removeFileFromCache(app, id);
            tagLogger.log("Removed file " + id + " for app " + app + " from cache");
        } else {
            tagLogger.log("Failed to remove file " + id + " for app " + app + " since cache is undefined");
        }

        //2. Clear file system
        File folder = new File(cacheFolder, id);
        FileCopyHelper.reliableDeleteFolderContent(folder);
        FileCopyHelper.reliableDelete(folder);
    }

    /**
     * Заменить одну папку в кеше на другую
     *
     * @param app
     * @param replaceTo
     * @param replaceBy
     */
    public void replaceOneFileEntryWithAnother(String app, String replaceTo, String replaceBy) {

        // этап 1 - удаляем папку с заменяемыми данными с диска
        File folderForReplaceTo = new File(cacheFolder, replaceTo);
        FileCopyHelper.reliableDeleteFolderAndSubFolders(folderForReplaceTo);

        // Этап 2 - переименовываем заменитель в заменяемого
        File folderReplaceBy = new File(cacheFolder, replaceBy);
        folderReplaceBy.renameTo(folderForReplaceTo);
    }

    /**
     * Прочитать данные файла FileInfo с диска
     *
     * @param fileId
     * @return
     */
    public FileInfo readFileInfoFromCacheFolder(String fileId) {
        File fileInfoFile = new File(new File(cacheFolder, fileId), FILE_INFO_READY);
        FileInfo fileInfo = (FileInfo) FileHelper.readObject(fileInfoFile);
        return fileInfo;
    }

    /**
     * Обновить данные FileInfo на диске
     *
     * @param fileId
     * @param fileInfo
     */
    public void updateFileInfoToCacheFolder(String fileId, FileInfo fileInfo) {
        File fileInfoFile = new File(new File(cacheFolder, fileId), FILE_INFO_READY);
        FileHelper.writeObject(fileInfo, fileInfoFile);
    }


    protected static String[] calcMD5OfMD5(Collection<ChunkInfo> values) {
        //1. Collecting MD5
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        for (Iterator<ChunkInfo> iterator = values.iterator(); iterator.hasNext(); ) {
            ChunkInfo chunkInfo = iterator.next();
            String chunkMD5 = chunkInfo.getChunkMd5();
            String unzippedChunkMD5 = chunkInfo.getUnzippedChunkMd5();
            sb1.append(chunkMD5);
            sb2.append(unzippedChunkMD5);
        }

        //2. Calculating
        byte[] data1 = sb1.toString().getBytes();
        byte[] data2 = sb2.toString().getBytes();
        String[] result = new String[2];
        try {
            result[0] = MD5Helper.getCheckSumAsString(data1);
        } catch (Exception e) {
            e.printStackTrace();
            result[0] = "EXCEPTION";
        }
        try {
            result[1] = MD5Helper.getCheckSumAsString(data2);
        } catch (Exception e) {
            e.printStackTrace();
            result[1] = "EXCEPTION";
        }
        return result;
    }


}
