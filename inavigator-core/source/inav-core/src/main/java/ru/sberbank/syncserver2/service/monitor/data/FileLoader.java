package ru.sberbank.syncserver2.service.monitor.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

/**
 * класс для загрузки файла из рабочей папки
 * если заданного файла нет, то файл сначала скопируется из исходников в рабочую папку
 * @author Shakhov Ilya
 *
 */
public abstract class FileLoader<T> {
	private final String warResourceFile;
	private final File workingFile;
	public FileLoader(String warResourceFile, String workingFile) {
		this.warResourceFile = warResourceFile;
		this.workingFile = new File(workingFile);
	}
	
	public T load() throws Exception {
		if (!workingFile.exists() && warResourceFile != null && !warResourceFile.trim().equals(""))
			copy(this.getClass().getResourceAsStream(warResourceFile), workingFile);
		FileReader fr = null;
		T result;
		try {
			result = readFromFile(workingFile);
		} finally {
			if (fr!=null) 
				fr.close();
		}
		
		return result;		
	}
		
	protected abstract T readFromFile(File fileReader) throws Exception;
	
	private void copy(InputStream is, File destFile) throws IOException {
		if (is == null)
			throw new FileNotFoundException("File "+warResourceFile+" does not exist in the Resources.");
	    if(!destFile.exists()) {
	    	destFile.getParentFile().mkdirs();
	        destFile.createNewFile();
	    }
	    FileOutputStream fos = null;
	    try {
	    	fos = new FileOutputStream(destFile);
	        IOUtils.copy(is, fos);
	    } finally {
	        if(is != null) {
	            is.close();
	        }
	        if(fos != null) {
	            fos.close();
	        }
	    }
	}
	
}
