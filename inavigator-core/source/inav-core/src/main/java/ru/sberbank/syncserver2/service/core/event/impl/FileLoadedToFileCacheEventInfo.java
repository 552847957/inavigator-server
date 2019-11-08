package ru.sberbank.syncserver2.service.core.event.impl;

import ru.sberbank.syncserver2.service.core.event.AbstractEventInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;

public class FileLoadedToFileCacheEventInfo extends AbstractEventInfo {

	private FileInfo fileInfo;
	
	public FileLoadedToFileCacheEventInfo(FileInfo fileInfo) {
		super();
		this.fileInfo = fileInfo;
	}

	public FileInfo getFileInfo() {
		return fileInfo;
	}

	public void setFileInfo(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}


}
