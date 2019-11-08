package com.sberbank.downloadtest;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Logger;

import com.sberbank.vmo.syncserv.data.RDReportStatus;
import com.sberbank.vmo.syncserv.data.RDReportStatusList;
import com.sberbank.vmo.syncserv.data.RDZippedReport;

/**
 * Syncserver files validation.<br/>
 *
 *
 * @author Sergey Erin
 *
 */
public class SyncDownloadHelper {
	private static Logger log = Logger.getLogger(SyncDownloadHelper.class);
	
	public static final String URL_LIST_PROPERTY            = "URL_LIST";
	public static final String URL_CHUNK_PATTERN_PROPERTY   = "URL_CHUNK_PATTERN";
	public static final String RDREPORTSTATUS_TYPE_PROPERTY = "RDREPORTSTATUS_TYPE";
	public static final String SEPARATOR = "==================================================";
	
	private Properties properties;

	/**
	 * @param properties
	 */
	public SyncDownloadHelper(Properties properties) {
		this.properties = properties;
	}

	public DownloadStatus downloadAll() {
		log.info("Sending list request...");

		RDReportStatusList list = requestObject(properties.getProperty(URL_LIST_PROPERTY), RDReportStatusList.class);

		if (list == null) {
			log.error("No list returned");
			return DownloadStatus.FAIL;
		}

		ArrayList<RDReportStatus> statuses = list.getReportStatuses();
		if (statuses == null) {
			log.error("No statuses returned in list");
			return DownloadStatus.FAIL;
		}

		log.debug("Looking for '" + properties.getProperty(RDREPORTSTATUS_TYPE_PROPERTY) + "'...");
		for (RDReportStatus status : statuses) {
			if (!properties.getProperty(RDREPORTSTATUS_TYPE_PROPERTY).equalsIgnoreCase(status.getType())) {
				continue;
			}

			String dataFileName = status.getDataFile();
			String initialMD5 = status.getDataMD5();
			int chunkCount = Integer.valueOf(status.getChunkCount());

			log.info("Ready to download chunks");
			log.info(SEPARATOR);
			log.info("File         : " + dataFileName);
			log.info("Last modified: " + status.getLastModified());
			log.info("MD5          : " + initialMD5);
			log.info("Chunk count  : " + chunkCount);
			log.info(SEPARATOR);

			byte[] buffer = new byte[1024];
			File newFile = new File(dataFileName);
			FileOutputStream newFileStream = null;
			try {
				newFileStream = new FileOutputStream(newFile);

			for(int chunkIndex = 0; chunkIndex < chunkCount; chunkIndex++) {
				log.info("Downloading chunk " + (chunkIndex + 1) + " of " + chunkCount);
				RDZippedReport report = getBinaryData(properties.getProperty(URL_CHUNK_PATTERN_PROPERTY),chunkIndex); // requestObject(properties.getProperty(URL_CHUNK_PATTERN_PROPERTY), RDZippedReport.class, chunkIndex);
				
				

				if (report == null) {
					log.error("Empty report returned");
					return DownloadStatus.FAIL;
				}

				Inflater inflater = new Inflater();
				inflater.setInput(report.getBinaryContent());
				try {
					int len;
					while ((len = inflater.inflate(buffer)) > 0) {
						newFileStream.write(buffer, 0, len);
					}
				} catch (IOException e) {
					log.error(e, e);
					return DownloadStatus.FAIL;
				} catch (DataFormatException e) {
					log.error(e, e);
					return DownloadStatus.INCORRECT_DATA;
				}
			}

			} catch (FileNotFoundException e) {
				log.error(e, e);
				return DownloadStatus.FAIL;
			} finally {
				try {
					newFileStream.close();
				} catch (IOException e) {
					log.error(e, e);
				}
				
			}
			
			

			log.debug("Checking md5...");
			String newFileMd5 = null;
			try {
				newFileMd5 = MD5Helper.getCheckSumAsString(dataFileName);
			} catch (Exception e) {
				log.error(e, e);
				return DownloadStatus.FAIL;
			}

			log.info(SEPARATOR);
			if (ObjectUtils.equals(initialMD5, newFileMd5)) {
				System.out.println("Remote and local files are identical");
				System.out.println("File name : " + dataFileName);
				System.out.println("MD5       : " + initialMD5);
			} else {
				System.out.println("Remote and local files are different");
				System.out.println("File name  : " + dataFileName);
				System.out.println("Remote MD5 : " + initialMD5);
				System.out.println("Local MD5  : " + initialMD5);
			}
		}

		return DownloadStatus.OK;
	}

	private RDZippedReport getBinaryData(String query,int chunkIndex) {
		log.debug("New binary data request");

		try {
			query = String.format(query, chunkIndex);
			log.debug("Request url: " + query);
			URL url = new URL(query);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Content-Type", "text/xml");
			connection.setRequestProperty("Connection","keep-alive");
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			byte[] buffer = new byte[4096];
			int n = 0;
			while( (n = connection.getInputStream().read(buffer)) != -1) {
				bos.write(buffer,0,n);
			}
			System.out.println(chunkIndex + ": " + bos.toByteArray().length + "b");
			RDZippedReport result = new RDZippedReport(bos.toByteArray(),"" + chunkIndex); 
			
			return result;
		} catch (MalformedURLException e) {
			log.error(e, e);
		} catch (IOException e) {
			log.error(e, e);
		}
		return null;
	}
	
	private <T> T requestObject(String query, Class<T> clazz, Object... params) {
		log.debug("New object request");
		log.debug("Object request params: " + query + ", class: " + clazz + ", params: " + params);
		T obj = null;

		try {
			query = String.format(query, params);
			log.debug("Request url: " + query);
			URL url = new URL(query);
			HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			connection.setRequestProperty("Accept", "application/xml");
			connection.setHostnameVerifier(new HostnameVerifier() {
				
				@Override
				public boolean verify(String arg0, SSLSession arg1) {
					// TODO Auto-generated method stub
					return true;
				}
			});
			
			JAXBContext context = JAXBContext.newInstance(clazz);
			System.out.println(connection.getResponseCode());
			obj = (T) context.createUnmarshaller().unmarshal(connection.getInputStream());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			log.error(e, e);
		} catch (IOException e) {
			e.printStackTrace();
			log.error(e, e);
		} catch (JAXBException e) {
			e.printStackTrace();
			log.error(e, e);
		}

		log.debug("Object request finish");
		if (obj != null && log.isDebugEnabled()) {
			log.debug("object: " + obj);
		}

		return obj;
	}

}
