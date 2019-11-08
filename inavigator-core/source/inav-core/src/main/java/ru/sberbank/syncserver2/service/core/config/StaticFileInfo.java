package ru.sberbank.syncserver2.service.core.config;

import java.io.Serializable;

/**
 * Created by sbt-kozhinsky-lb on 04.03.14.
 */
public class StaticFileInfo implements Serializable {
    private String app;
    private String fileId;
    private String fileName;
    private String hostnames;
    private boolean autoGenEnabled;
    private boolean isGenerationModeDraft;
    private boolean isDraft;
    private boolean isCleanStatus;
    

    public StaticFileInfo(String app, String fileId, String fileName) {
        this.app = app;
        this.fileId = fileId;
        this.fileName = fileName;
    }


	public StaticFileInfo(String app, String fileId, String fileName, String hostnames) {
        this.app = app;
        this.fileId = fileId;
        this.fileName = fileName;
        this.hostnames = hostnames;
    }

    public StaticFileInfo(String app, String fileId, String fileName, String hostnames,boolean autoGenEnabled) {
        this.app = app;
        this.fileId = fileId;
        this.fileName = fileName;
        this.hostnames = hostnames;
        this.autoGenEnabled = autoGenEnabled;
    }
    
    
    
    public StaticFileInfo(String app, String fileId, String fileName,
			String hostnames, boolean autoGenEnabled,
			boolean isGenerationModeDraft, boolean isDraft,
			boolean isCleanStatus) {
		super();
		this.app = app;
		this.fileId = fileId;
		this.fileName = fileName;
		this.hostnames = hostnames;
		this.autoGenEnabled = autoGenEnabled;
		this.isGenerationModeDraft = isGenerationModeDraft;
		this.isDraft = isDraft;
		this.isCleanStatus = isCleanStatus;
	}


	public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getHostnames() {
        return hostnames;
    }

    public void setHostnames(String hostnames) {
        this.hostnames = hostnames;
    }

	public boolean isGenerationModeDraft() {
		return isGenerationModeDraft;
	}


	public void setGenerationModeDraft(boolean isGenerationModeDraft) {
		this.isGenerationModeDraft = isGenerationModeDraft;
	}


	public boolean isDraft() {
		return isDraft;
	}


	public void setDraft(boolean isDraft) {
		this.isDraft = isDraft;
	}


	public boolean isCleanStatus() {
		return isCleanStatus;
	}


	public void setCleanStatus(boolean isCleanStatus) {
		this.isCleanStatus = isCleanStatus;
	}


	public boolean isAutoGenEnabled() {
		return autoGenEnabled;
	}

	public void setAutoGenEnabled(boolean autoGenEnabled) {
		this.autoGenEnabled = autoGenEnabled;
	}
	
	
	
    /**
     * Получить название действия applicationCode_fileName
     * @return
     */
    public String getFullName() {
    	return (getApp()!=null?getApp().toUpperCase():"")
    			+ "_"
    			+ (getFileName()!=null?getFileName().toUpperCase():"");
    }
}
