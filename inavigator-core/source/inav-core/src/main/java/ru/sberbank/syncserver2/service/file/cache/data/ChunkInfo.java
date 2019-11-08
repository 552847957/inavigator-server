package ru.sberbank.syncserver2.service.file.cache.data;

import ru.sberbank.syncserver2.service.file.cache.FileLoader;
import ru.sberbank.syncserver2.service.log.TagLogger;

import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.text.MessageFormat;

import javax.xml.bind.DatatypeConverter;


/**
 * Created by IntelliJ IDEA.
 * User: sbt-kozhinskiy-lb
 * Date: 13.02.2012
 * Time: 17:35:54
 * To change this template use File | Settings | File Templates.
 */
public class ChunkInfo implements Serializable, Cloneable {
	private static final long serialVersionUID = -5803927182728693740L;

    private int                chunkIndex;
    private int                unzippedLength;
    private long               unzippedOffset;
    private transient          FileLoader loader;
    private transient SoftReference<byte[]>   chunkContent;
    private String             chunkMd5;
    private String             unzippedChunkMd5;
    private String             chunkTitle;
    private FileInfo           fileInfo;
    private boolean            loaded;

    public ChunkInfo(int chunkIndex, long unzippedOffset, int unzippedLength, byte[] chunkContent, String chunkMd5, String unzippedChunkMd5, FileInfo fileInfo, FileLoader theLoader) {
        this.loader = theLoader;
        this.chunkIndex = chunkIndex;
        this.unzippedOffset = unzippedOffset;
        this.unzippedLength = unzippedLength;
        this.chunkContent = new SoftReference<byte[]>(chunkContent);
        this.chunkMd5 = chunkMd5;
        this.unzippedChunkMd5 = unzippedChunkMd5;
        this.fileInfo = fileInfo;
        this.loaded = true;
        createChunkTitle(fileInfo);
    }

    public void setLoader(FileLoader theLoader) {
        this.loader = theLoader;
    }

    public int getChunkIndex() {
        return chunkIndex;
    }

    public void setChunkIndex(int chunkIndex) {
        this.chunkIndex = chunkIndex;
    }

    public byte[] getChunkContent() {
        byte[] result = chunkContent.get();
        if (result == null) {
            result = this.loader.loadSingleChunk(this.fileInfo.getName(), this.chunkIndex);
            if (result != null) {
                this.chunkContent = new SoftReference<byte[]>(result);
                TagLogger tagLogger = this.loader.getTagLogger();
                if (tagLogger != null) {
                    tagLogger.log(MessageFormat.format("ChunkInfo: Loaded chank {0}", this));
                }
            } else {
                this.loaded = false;
            }
        }
        return result;
    }

    public void setChunkContent(byte[] chunkContent) {
        this.chunkContent = new SoftReference<byte[]>(chunkContent);
    }

    public synchronized void loadChunkContent(byte[] chunkContent) {
        this.chunkContent = new SoftReference<byte[]>(chunkContent);
        this.loaded = chunkContent!=null;
    }

    public String getChunkMd5() {
        return chunkMd5;
    }

    public void setChunkMd5(String chunkMd5) {
        this.chunkMd5 = chunkMd5;
    }

    public String getUnzippedChunkMd5() {
        return unzippedChunkMd5;
    }

    public void setUnzippedChunkMd5(String unzippedChunkMd5) {
        this.unzippedChunkMd5 = unzippedChunkMd5;
    }

    public String getChunkTitle() {
        if(chunkTitle!=null){
            return chunkTitle;
        } else {
            createChunkTitle(fileInfo);
            return chunkTitle;
        }
    }

    public void setChunkTitle(String chunkTitle) {
        this.chunkTitle = chunkTitle;
    }

    public FileInfo getFileInfo() {
        return fileInfo;
    }

    public void setFileInfo(FileInfo fileInfo) {
        this.fileInfo = fileInfo;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    private void createChunkTitle(FileInfo info) {
        String s = MessageFormat.format("app=\"{0}\" id=\"{1}\" chunkIndex=\"{2}\" md5=\"{3}\" chunkMd5=\"{4}\" unzippedChunkMd5=\"{5}\" unzippedLength=\"{6}\" unzippedOffset=\"{7}\"", info.getApp(), info.getId(), String.valueOf(chunkIndex), info.getDataMD5(), chunkMd5, unzippedChunkMd5, String.valueOf(unzippedLength), String.valueOf(unzippedOffset));
        chunkTitle = DatatypeConverter.printBase64Binary(s.getBytes());
    }


    public ChunkInfo getHeaderInfoOnly() {
        try {
            ChunkInfo copy = (ChunkInfo) clone();
            copy.setChunkContent(null);
            copy.setFileInfo(null);
            copy.setLoaded(false);
            return copy;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void clear(){
        fileInfo = null;
    }

    @Override
    public String toString() {
        return "ChunkInfo{" +
                "chunkIndex=" + chunkIndex +
                ", unzippedLength=" + unzippedLength +
                ", unzippedOffset=" + unzippedOffset +
                ", chunkMd5='" + chunkMd5 + '\'' +
                ", chunkMd5='" + chunkMd5 + '\'' +
                ", chunkTitle='" + chunkTitle + '\'' +
                ", fileInfo=" + fileInfo +
                ", loaded=" + loaded +
                '}';
    }
}
