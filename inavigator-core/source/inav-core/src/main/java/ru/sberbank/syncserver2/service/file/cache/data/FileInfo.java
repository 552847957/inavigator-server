package ru.sberbank.syncserver2.service.file.cache.data;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * Created by sbt-kozhinsky-lb on 21.02.14.
 */
@XmlType(name="report-status",propOrder = {"dataMD5","md5OfMd5","unzippedMd5OfMd5","publishStatus","name","lastModified","app", "id", "group", "caption", "chunkCount", "previewImage"})
public class FileInfo implements Serializable, Cloneable {
    private static final String DRAFT_FLAG = "_DRAFT";
    public static final String REMOVED_FLAG = "file_removed";

	private static final long serialVersionUID = 3251716522162939693L;

    private String app;
    private String id;
    private String name;
    private String caption;
    private String group;
    private String dataMD5;

    private String lastModified;
    private String chunkCount;
    private String md5OfMd5;
    private String unzippedMd5OfMd5;
    
    private long fileLength;
    
    private Boolean isDraft = null;

    transient private BufferedImage preview = null;
    private byte[] previewImage = null;

    public FileInfo() {
    }

    public FileInfo(String app, String reportId, String name) {
        this.app = app;
        this.id = reportId;
        this.name = name;
    }

    public FileInfo(String app, String reportId, String group, String caption, String md5) {
        this.app = app;
        this.id = reportId;
        this.group = group;
        this.caption = caption;
        this.dataMD5 = md5;
    }

	@XmlAttribute(name = "app")
    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    @XmlAttribute(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlAttribute(name = "group")
    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    @XmlAttribute(name = "caption")
    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    @XmlAttribute(name = "dataMD5")
    public String getDataMD5() {
        return dataMD5;
    }

    public void setDataMD5(String dataMD5) {
        this.dataMD5 = dataMD5;
    }

    @XmlAttribute(name = "lastModified")
    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    @XmlAttribute(name = "chunk-count")
    public String getChunkCount() {
        return chunkCount;
    }

    public void setChunkCount(String chunkCount) {
        this.chunkCount = chunkCount;
    }

    @XmlAttribute(name = "publishStatus")
    public String getPublishStatus() {
        return "end_user";
    }

    @XmlAttribute(name = "md5OfMd5")
    public String getMd5OfMd5() {
        return md5OfMd5;
    }

    public void setMd5OfMd5(String md5OfMd5) {
        this.md5OfMd5 = md5OfMd5;
    }


    @XmlAttribute(name = "unzippedMd5OfMd5")
    public String getUnzippedMd5OfMd5() {
        return unzippedMd5OfMd5;
    }

    public void setUnzippedMd5OfMd5(String unzippedMd5OfMd5) {
        this.unzippedMd5OfMd5 = unzippedMd5OfMd5;
    }

    @XmlAttribute(name = "fileLength")
    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public boolean isRemoved(){
        return REMOVED_FLAG.equals(lastModified);
    }

    public Object clone(){
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    @XmlTransient
    public boolean isDraft() {
		return isDraft == null?false:isDraft;
	}

	public void setDraft(boolean isDraft) {
		this.isDraft = isDraft;
	}
	
	public boolean isDraftStatusInitialised() {
		return isDraft != null;
	}
	
	/**
	 * Сгенерировать идентификатор файла для статуса черновика
	 * @param fileId
	 * @return
	 */
	public static String generateDraftFileId(String fileId) {
		return fileId + DRAFT_FLAG;
	}
	
	public String getFullFileId() {
		if (isDraft())
			return FileInfo.generateDraftFileId(getId());
		else
			return getId();
	}

	public String getIdForDraft() {
		return generateDraftFileId(getId()); 
	}

	@XmlTransient
	public BufferedImage getPreview() {
        return preview;
    }

    public void setPreview(BufferedImage preview) {
        this.preview = preview;
    }

    @XmlAttribute(name = "previewImage")
    public byte[] getPreviewImage() {
        return previewImage;
    }

    public void setPreviewImage(byte[] previewImage) {
        this.previewImage = previewImage;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "app='" + app + '\'' +
                ", id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", caption='" + caption + '\'' +
                ", group='" + group + '\'' +
                ", dataMD5='" + dataMD5 + '\'' +
                ", lastModified='" + lastModified + '\'' +
                ", chunkCount='" + chunkCount + '\'' +
                '}';
    }

}
