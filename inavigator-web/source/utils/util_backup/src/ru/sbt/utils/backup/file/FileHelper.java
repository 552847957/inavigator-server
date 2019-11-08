package ru.sbt.utils.backup.file;

import ru.sbt.utils.backup.configuration.DatabaseInfo;
import ru.sbt.utils.backup.configuration.DatabaseServerInfo;
import ru.sbt.utils.backup.configuration.XmlConfiguration;
import ru.sbt.utils.backup.model.SQLScript;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileHelper {
    public final static String CSV_FILE_EXTENSION = ".csv";
	
	 public static File getBackupFolder(XmlConfiguration configuration, String timestamp,DatabaseServerInfo dbsinfo, DatabaseInfo dbinfo) {
	      return new File(configuration.getBackupPath()+"/backup_" + timestamp+"/[" + dbsinfo.getHost() + "]." + dbinfo.getName());				
	 }	
	 
	 public static File getFileForTable(File root, String tableName) {
		 return new File(root, "dbo." + tableName + CSV_FILE_EXTENSION);
	 }
	 
	 public static boolean createFolder(File folder) {
		 if (!folder.exists()) 
			 return folder.mkdirs();
		 else
			 return true;
		 
	 }
	 
	 public static boolean createFile(File file) {
		 try {			 
			 return createFolder(file.getParentFile()) && file.createNewFile();
		} catch (IOException e) {
			return false;
		}			 
	 }
	 
	 public static void close(Closeable c) throws IOException {
		 if (c!=null)
			 c.close();
	 }

	 public static Map<String, List<SQLScript>> readScriptsFromDirectory(File directory) throws IOException {
	 	Map<String, List<SQLScript>> result = new HashMap<String, List<SQLScript>>();
		 File[] files = directory.listFiles();
		 if (files == null) {
		 	return result;
		 }
		 for (File file : files) {
		 	if (file.getName().endsWith(".sql")) {
		 		result.put(file.getName(), SQLScript.getScriptsFromFile(file));
			}
		 }
		 return result;
	 }

}
