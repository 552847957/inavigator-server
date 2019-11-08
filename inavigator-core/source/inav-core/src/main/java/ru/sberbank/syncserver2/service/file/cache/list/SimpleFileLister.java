package ru.sberbank.syncserver2.service.file.cache.list;

import java.util.Map;
import java.util.UUID;

import ru.sberbank.syncserver2.service.core.config.StaticFileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;

public class SimpleFileLister extends FileLister  {

	public String quottesApplicationId = "QuottesApplication";  
	
	@Override
	protected Map<String, StaticFileInfo> doLoadAll() {
		return null;
	}

	@Override
	public StaticFileInfo getFileInfo(String fileName) {
		return new StaticFileInfo(quottesApplicationId,UUID.randomUUID().toString(),fileName);
	}

	public String getQuottesApplicationId() {
		return quottesApplicationId;
	}

	public void setQuottesApplicationId(String quottesApplicationId) {
		this.quottesApplicationId = quottesApplicationId;
	}

	
	
}
