package ru.sberbank.syncserver2.service.file.cache.data;

/**
 * Информации о статусах файла
 * @author sbt-gordienko-mv
 *
 */
public class FileStatusInfo {
	
	/**
	 * Идентификатор приложения
	 */
	private String appId;
	
	/**
	 * Идентификатор файла
	 */
	private String fileId;
	
	
	/**
	 * MD5 текущего опубликованного файла
	 */
	private String publishMd5;
	
	/**
	 * MD5 текущего драфта
	 */
	private String draftMd5;
	
	/**
	 * Является ли режим генерации файла - черновиком
	 */
	private boolean isGenerationModeDraft;
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((appId == null) ? 0 : appId.hashCode());
		result = prime * result
				+ ((draftMd5 == null) ? 0 : draftMd5.hashCode());
		result = prime * result + ((fileId == null) ? 0 : fileId.hashCode());
		result = prime * result + (isGenerationModeDraft ? 1231 : 1237);
		result = prime * result
				+ ((publishMd5 == null) ? 0 : publishMd5.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FileStatusInfo other = (FileStatusInfo) obj;
		if (appId == null) {
			if (other.appId != null)
				return false;
		} else if (!appId.equals(other.appId))
			return false;
		if (draftMd5 == null) {
			if (other.draftMd5 != null)
				return false;
		} else if (!draftMd5.equals(other.draftMd5))
			return false;
		if (fileId == null) {
			if (other.fileId != null)
				return false;
		} else if (!fileId.equals(other.fileId))
			return false;
		if (isGenerationModeDraft != other.isGenerationModeDraft)
			return false;
		if (publishMd5 == null) {
			if (other.publishMd5 != null)
				return false;
		} else if (!publishMd5.equals(other.publishMd5))
			return false;
		return true;
	}

	public FileStatusInfo() {
		super();
	}

	public FileStatusInfo(String appId, String fileId, String publishMd5,
			String draftMd5,boolean isGenerationModeDraft) {
		super();
		this.appId = appId;
		this.fileId = fileId;
		this.publishMd5 = publishMd5;
		this.draftMd5 = draftMd5;
		this.isGenerationModeDraft = isGenerationModeDraft;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public String getPublishMd5() {
		return publishMd5;
	}

	public void setPublishMd5(String publishMd5) {
		this.publishMd5 = publishMd5;
	}

	public String getDraftMd5() {
		return draftMd5;
	}

	public void setDraftMd5(String draftMd5) {
		this.draftMd5 = draftMd5;
	}

	public boolean isGenerationModeDraft() {
		return isGenerationModeDraft;
	}

	public void setGenerationModeDraft(boolean isGenerationModeDraft) {
		this.isGenerationModeDraft = isGenerationModeDraft;
	}
	
	
}
