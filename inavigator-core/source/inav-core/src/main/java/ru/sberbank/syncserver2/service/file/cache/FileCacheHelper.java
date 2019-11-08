package ru.sberbank.syncserver2.service.file.cache;

import org.apache.commons.io.IOUtils;
import ru.sberbank.syncserver2.service.file.cache.data.ChunkInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.util.FileHelper;
import ru.sberbank.syncserver2.util.FormatHelper;
import ru.sberbank.syncserver2.util.MD5Helper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

/**
 * Created by sbt-kozhinsky-lb on 18.02.15.
 */
public class FileCacheHelper {

    public static SingleChunkInfo composeSingleChunkInfo(String app, String id, byte[] unzippedData, FileLoader theLoader){
        //1. Zipping and calculating MD5
        //System.out.println("UNZIPPED DATA LENGHT = "+unzippedData.length);
        byte[] zippedData = FileHelper.zip(unzippedData);
        //System.out.println("ZIPPED DATA LENGHT = "+zippedData.length);
        String chunkMd5 = calcMD5WithoutException(zippedData);
        String unzippedChunkMd5 = calcMD5WithoutException(unzippedData);

        //2. Composing file info - first step
        FileInfo fileInfo = new FileInfo();
        fileInfo.setApp(app);
        fileInfo.setId(id);
        fileInfo.setName(id);
        fileInfo.setCaption(id);
        fileInfo.setGroup(id);
        fileInfo.setFileLength(unzippedData.length);
        fileInfo.setDataMD5(unzippedChunkMd5);
        fileInfo.setChunkCount("1");

        //2. Composing chunk info
        int chunkIndex = 0;
        int unzippedLength = unzippedData.length;
        int unzippedOffset = 0;
        ChunkInfo chunkInfo = new ChunkInfo(chunkIndex, unzippedOffset, unzippedLength, zippedData, chunkMd5,unzippedChunkMd5,fileInfo, theLoader);

        //3. Compose file info - second step - complementing by MD5
        String sLastModified = FormatHelper.formatDateTimeWithTimeZone(new Date());
        fileInfo.setLastModified(sLastModified);
        String[] md5s = AbstractFileLoader.calcMD5OfMD5(Collections.singleton(chunkInfo));
        fileInfo.setMd5OfMd5(md5s[0]);
        fileInfo.setUnzippedMd5OfMd5(md5s[1]);

        //4. Composing result
        //System.out.println("FILE INFO = "+fileInfo);
        //System.out.println("CHUNK INFO = "+chunkInfo);
        return new SingleChunkInfo(fileInfo, chunkInfo);
    }

    public static class SingleChunkInfo{
        public FileInfo  fileInfo;
        public ChunkInfo chunkInfo;

        public SingleChunkInfo(FileInfo fileInfo, ChunkInfo chunkInfo) {
            this.fileInfo = fileInfo;
            this.chunkInfo = chunkInfo;
        }

        public FileInfo getFileInfo() {
            return fileInfo;
        }

        public void setFileInfo(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        public ChunkInfo getChunkInfo() {
            return chunkInfo;
        }

        public void setChunkInfo(ChunkInfo chunkInfo) {
            this.chunkInfo = chunkInfo;
        }
    }

    private static String calcMD5WithoutException(byte[] data){
        try {
            return MD5Helper.getCheckSumAsString(data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * This method could be used for small files - significantlty less than 2Gb.
     * It won't work with files greater than 2Gb and it is very memory-angry
     * @param cache
     * @param app
     * @param id
     * @return
     */
    public static byte[] getSmallFileData(FileCache cache, String app, String id){
        //1. Getting data from cache
        FileInfo fileInfo = cache.getFileInfo(app, id);
        Map<Integer, ChunkInfo>  chunks = cache.getAllChunks(app, id);
        if(fileInfo==null || chunks==null){
            return null;
        }

        //2. Unzipping
        byte[][] data = new byte[chunks.size()][];
        int approximateFileLength = (int) fileInfo.getFileLength();
        //the length is approximate because FileInfo could change before chunks received
        // and because it is assumed that this method is used for small files
        ByteArrayOutputStream os = new ByteArrayOutputStream(approximateFileLength);
        try {
            for(int i=0; i<chunks.size(); i++){
                ChunkInfo info = chunks.get(new Integer(i));
                data[i] = FileHelper.unzip(info.getChunkContent());
                os.write(data[i]);
            }
            return os.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(os);
        }
        return null;
    }
}
