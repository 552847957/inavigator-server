package ru.sberbank.syncserver2.service.monitor.check;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WritePermissionCheck extends AbstractCheckAction {
	
	public static final String CHECK_RESULT_CODE_CAN_NOT_CREATE_DIRECTORY = "CAN_NOT_CREATE_DIRECTORY";
	public static final String CHECK_RESULT_CODE_CAN_NOT_CREATE_FILE = "CAN_NOT_CREATE_FILE";
	public static final String CHECK_RESULT_CODE_CAN_NOT_DELETE_EXISTING_DIRECTORY = "CAN_NOT_DELETE_EXISTING_DIRECTORY";
	public static final String CHECK_RESULT_CODE_CAN_NOT_DELETE_CREATED_FILE = "CAN_NOT_DELETE_FILE";
	public static final String CHECK_RESULT_CODE_CAN_NOT_DELETE_CREATED_DIRECTORY = "CAN_NOT_DELETE_CREATED_DIRECTORY";
	public final String uuid = UUID.randomUUID().toString();
	
	private String includeFolders  = "";

	public String getIncludeFolders() {
		return includeFolders;
	}

	public void setIncludeFolders(String includeFolders) {
		this.includeFolders = includeFolders;
	}
	
	private class WritePermissionCheckerException extends IOException {

		public WritePermissionCheckerException(String message) {
			super(message);
		}
		
	}
	
	@Override
	public List<CheckResult> doCheck() {
		List<CheckResult> results = new ArrayList<CheckResult>();
		
		String[] includeNames = includeFolders.split(";");
        for (int i = 0; i < includeNames.length; i++) {
            includeNames[i] = includeNames[i].toLowerCase();
            
            try {
            	File folder = new File(includeNames[i],"testDirectory_"+uuid);

            	// шаг 1. если папка существует удаляем ее.
            	if (folder.exists())
            		if (!deleteFile(folder))
            			throw new WritePermissionCheckerException("can't delete old temp directory "+folder.getAbsolutePath());
            	
            	// шаг 2 - пытаемся создать папку.
            	if (!folder.mkdir())
            		throw new WritePermissionCheckerException("can't create temp directory "+folder.getAbsolutePath());
            	
            	File tempFile = new File(folder,"testFile.tmp");

            	// шаг 3 пытаемся создать временный файл
            	if (!tempFile.createNewFile())
            		throw new WritePermissionCheckerException("can't create temp file "+tempFile.getAbsolutePath());
            	
            	// шаг 4. Удаляем созданный файл
            	if (!deleteFile(tempFile))
            		throw new WritePermissionCheckerException("can't delete temp file "+tempFile.getAbsolutePath());

            	// шаг 5. Удаляем директорию
            	if (!deleteFile(folder))
            		throw new WritePermissionCheckerException("can't delete temp directory "+folder.getAbsolutePath());
            	
            	// все этапы успешно пройдены - проверка прав закончилась успешно
            	if (addSuccessfullCheckResultIfPreviousFailed(results, "INC_FOLDER_"+i, LOCAL_HOST_NAME + " says: Write permissions check passed succesfully.")) {
            		tagLogger.log("Write permissions check passed succesfully.");
            	}

            } catch (WritePermissionCheckerException ex) {
            	results.add(new CheckResult("INC_FOLDER_"+i,false,LOCAL_HOST_NAME + " says: " + ex.getMessage() + "."));
            	tagLogger.log(ex.getMessage());
            } catch (IOException ex) {
            	ex.printStackTrace();
            	results.add(new CheckResult("INC_FOLDER_"+i,false,LOCAL_HOST_NAME + " says: Write permissions check passed failed. Please analyze server logs."));
            }
        }

        return results;
	}
	
	private boolean deleteFile(File root) {
		File[] files = root.listFiles();
		if (files == null) 	{	
			if (!root.delete()) 
				return false; 
			else
				return true;	
		}
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory())
				deleteFile(file);
			if (!file.delete()) 
				return false;			
		}
		if (!root.delete()) 
			return false;
		return true;
	}
	
	@Override
	public String getDescription() {
		return "Чекер для проверки прав на ФП";
	}
}
