package ru.sberbank.syncserver2.service.monitor.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class OnlineRequestSettingsLoader<R> extends FileLoader<List<R>> {
	
	public OnlineRequestSettingsLoader(String resourceFile, String workingFile) {
		super(resourceFile, workingFile);
	}

	@Override
	protected List<R> readFromFile(File file) throws IOException {
		List<R> result = new ArrayList<R>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(file));
			R info;
			while ((info = getRequestInfo(reader))!=null) {
				result.add(info);
			}		
		
		} finally {
			if (reader!=null)
				reader.close();
		}		
		return result;
	}
	
	/**
	 * сформировать объект типа RequestInfo
	 * @param reader
	 * @return
	 * @throws IOException
	 */
	protected abstract R getRequestInfo(BufferedReader reader) throws IOException;
	
}
