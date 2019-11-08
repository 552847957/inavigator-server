package ru.sberbank.syncserver2.service.core.event.impl;

import ru.sberbank.syncserver2.service.core.event.AbstractEventInfo;

public class FileInflateFinishedEventInfo extends AbstractEventInfo {

	private String fileName;
	
	private String fileMd5;
	
	private boolean isSuccesfull;

	public FileInflateFinishedEventInfo(String fileName, String fileMd5,
			boolean isSuccesfull) {
		super();
		this.fileName = fileName;
		this.fileMd5 = fileMd5;
		this.isSuccesfull = isSuccesfull;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileMd5() {
		return fileMd5;
	}

	public void setFileMd5(String fileMd5) {
		this.fileMd5 = fileMd5;
	}

	public boolean isSuccesfull() {
		return isSuccesfull;
	}

	public void setSuccesfull(boolean isSuccesfull) {
		this.isSuccesfull = isSuccesfull;
	}
	
	
	
}
