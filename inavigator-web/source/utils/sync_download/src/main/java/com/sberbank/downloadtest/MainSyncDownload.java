package com.sberbank.downloadtest;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class MainSyncDownload {
	private static Logger log = Logger.getLogger(SyncDownloadHelper.class);

	public static final String APP_PROPERTIES = "app.properties";

	public static final String DEFAULT_URL_LIST             = "https://config2.mobile-test.sbrf.ru:9443/syncserver/single/file.do?command=list&app=iNavigator";
	public static final String DEFAULT_URL_CHUNK_PATTERN    = "https://config2.mobile-test.sbrf.ru:9443/syncserver/single/file.do?command=data&app=iNavigator&id=mis_navigator_kpi&chunkIndex=%d";
	public static final String DEFAULT_RDREPORTSTATUS_TYPE  = "MIS_NAVIGATOR_KPI";


	public static void main(String[] args) {
		Properties defaults = new Properties();

		defaults.setProperty(SyncDownloadHelper.URL_LIST_PROPERTY, DEFAULT_URL_LIST);
		defaults.setProperty(SyncDownloadHelper.URL_CHUNK_PATTERN_PROPERTY, DEFAULT_URL_CHUNK_PATTERN);
		defaults.setProperty(SyncDownloadHelper.RDREPORTSTATUS_TYPE_PROPERTY, DEFAULT_RDREPORTSTATUS_TYPE);

		Properties prop = new Properties(defaults);

		try {
			log.info("Reading application properties file: " + APP_PROPERTIES);
			FileInputStream fis = new FileInputStream(APP_PROPERTIES); 
			prop.load(fis);
			fis.close();
			log.info("Application properties: " + prop);
		} catch (FileNotFoundException e) {
			log.warn("Application properties file not found");
			log.info("Deafult application properties: " + defaults);
		} catch (IOException e) {
			log.error(e, e);
		}

		SyncDownloadHelper main = new SyncDownloadHelper(prop);

		DownloadStatus status = null;
		while (status == null || status == DownloadStatus.MD5CHANGED || status == DownloadStatus.INCORRECT_DATA) {
			log.info("Downloading data...");
			status = main.downloadAll();
			log.info("Download complete: " + status);
		}
	}
	
	
}
