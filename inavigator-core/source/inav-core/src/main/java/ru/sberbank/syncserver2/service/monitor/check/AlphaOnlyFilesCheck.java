package ru.sberbank.syncserver2.service.monitor.check;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.xml.bind.JAXBException;
import javax.net.ssl.X509TrustManager;

import org.bouncycastle.util.encoders.Base64;

import com.sun.mail.util.BASE64DecoderStream;

import ru.sberbank.syncserver2.service.file.diff.FileDiff;
import ru.sberbank.syncserver2.service.file.diff.FileDiffList;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.XMLHelper;

public class AlphaOnlyFilesCheck extends AbstractCheckAction{
	public static final long ONE_MINUTE_MILLIS = 60*1000;
	public static final int STATUS_CHECK = FileDiff.EXISTS_IN_ALPHA;
	public static final String CHECK_RESULT_CODE_VALUE_INCLMAXHOURS_WRONG = "VALUE_INCLMAXHOURS_WRONG";
	public static final String CHECK_RESULT_CODE_VALUE_PROCMAXSEC_WRONG = "VALUE_PROCMAXSEC_WRONG";
	public static final String CHECK_RESULT_CODE_HOST_IS_BUSY = " HOST_IS_BUSY";
	public static final String CHECK_RESULT_CODE_INCORRECT_CERTIFICATE = "CHECK_RESULT_CODE_INCORRECT_CERTIFICATE";
	
	public final String FILECHECK_URL = "/syncserver/changeset/filecheck.do";
	
	private String hosts="";
	private String includeMaxHours = "60";
	private String processingMaxSec = "60";
	
	private String PKCS12="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";
	private String password="Rcbyfujuf01";
	private SSLSocketFactory socketFactory;
	
	private HashMap<String, HashMap<String, Long>> filesStatus = new HashMap<String, HashMap<String, Long>>(); //host->caption->(time)

	public AlphaOnlyFilesCheck() {
		super();
	}
		
	public String getPKCS12() {
		return PKCS12;
	}

	public void setPKCS12(String pKCS12) {
		PKCS12 = pKCS12;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHosts() {
		return hosts;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}
	
	public String getIncludeMaxHours() {
		return includeMaxHours;
	}

	public void setIncludeMaxHours(String includeMaxHours) {
		this.includeMaxHours = includeMaxHours;
	}
	
	public String getProcessingMaxSec() {
		return processingMaxSec;
	}

	public void setProcessingMaxSec(String processingMaxSec) {
		this.processingMaxSec = processingMaxSec;
	}
	
	

	@Override
    public List<CheckResult> doCheck() {
		List<CheckResult> results = new ArrayList<CheckResult>();
		
		//1. Reading properties
		int includeMaxHours   = 1;
		int processingMaxSec = 60;
		
		try {
            includeMaxHours   = Integer.parseInt(this.includeMaxHours);
            addSuccessfullCheckResultIfPreviousFailed(results, CHECK_RESULT_CODE_VALUE_INCLMAXHOURS_WRONG,"The value for property includeMaxHours is correct");
        } catch (NumberFormatException e) {
            results.add(new CheckResult(CHECK_RESULT_CODE_VALUE_INCLMAXHOURS_WRONG,false,"The value for property includeMaxHours is wrong"));
        }
		try {
			processingMaxSec   = Integer.parseInt(this.processingMaxSec);
            addSuccessfullCheckResultIfPreviousFailed(results, CHECK_RESULT_CODE_VALUE_PROCMAXSEC_WRONG,"The value for property processingMaxSec is correct");
        } catch (NumberFormatException e) {
            results.add(new CheckResult(CHECK_RESULT_CODE_VALUE_PROCMAXSEC_WRONG,false,"The value for property processingMaxSec is wrong"));
        }
		if (socketFactory==null) {
			results.add(new CheckResult(CHECK_RESULT_CODE_INCORRECT_CERTIFICATE,false,"Can't load client certificate"));
			return results;
		} else {
			addSuccessfullCheckResultIfPreviousFailed(results, CHECK_RESULT_CODE_INCORRECT_CERTIFICATE,"Client certificate is loaded");
		}
		//2. Parsing hosts
		String[] hostNames = hosts.split(";");		
		
		//3. Make a check for all hosts
		for (int i=0; i < hostNames.length; i++) {
			//tagLogger.log("Start checking files loaded in cache at " + hostNames[i]);
			boolean successfulTest = true;
			try {				
				FileDiffList fileDiffList = null;
				long time = System.currentTimeMillis();
				fileDiffList = readXMLFromHost(hostNames[i]);
				time = (System.currentTimeMillis() - time)/1000;
				//tagLogger.log("Response time for " + hostNames[i] + " is " + time + " seconds");					
				if (time > processingMaxSec) {
					tagLogger.log("Response time for " + hostNames[i] + " is " + time + " seconds");
					results.add(new CheckResult(hostNames[i] + CHECK_RESULT_CODE_HOST_IS_BUSY,false,LOCAL_HOST_NAME+" says: it takes "+time+" seconds to generate the answer from " + hostNames[i] + " and it is more than "+processingMaxSec));
				} else {
					addSuccessfullCheckResultIfPreviousFailed(results, hostNames[i] + CHECK_RESULT_CODE_HOST_IS_BUSY, LOCAL_HOST_NAME+" says: it takes "+time+" seconds to generate the answer from " + hostNames[i] + " and it is less than "+processingMaxSec);
				}				
				List<FileDiff> diffs = fileDiffList.getDiffs();
				
				//View each file from diffs and add its diff in new cache with old time
				HashMap<String, Long> oldHostFiles = filesStatus.get(hostNames[i]);
				HashMap<String, Long> newHostFiles = new HashMap<String, Long>();
				Long t;							
				for (FileDiff diff: diffs) {						
					if (diff.getStatus() != STATUS_CHECK) continue;
					if (oldHostFiles != null && (t = oldHostFiles.get(diff.getCaption())) != null) {
						if (t < System.currentTimeMillis() - includeMaxHours * ONE_MINUTE_MILLIS) 
							successfulTest = false; 
						newHostFiles.put(diff.getCaption(), t);
					} else {
						newHostFiles.put(diff.getCaption(), new Long(System.currentTimeMillis()));
					}	
				}				
				//перезаписываем список файлов хоста
				filesStatus.put(hostNames[i], newHostFiles);				
			} catch (IOException e) {
				results.add(new CheckResult(hostNames[i], false, "Can't connect to " + hostNames[i] + FILECHECK_URL));
				//tagLogger.log("Can't connect to " + hostNames[i] + constPartOfURL + " because of " + e.getMessage());
				tagLogger.log("Skip checking "+ hostNames[i]);
				continue;
			} catch (KeyManagementException e) {
				results.add(new CheckResult(hostNames[i], false, "Can't connect to " + hostNames[i] + FILECHECK_URL));
				tagLogger.log("Skip checking "+ hostNames[i]);
				continue;
			} catch (NoSuchAlgorithmException e) {
				results.add(new CheckResult(hostNames[i], false, "Can't connect to " + hostNames[i] + FILECHECK_URL));
				tagLogger.log("Skip checking "+ hostNames[i]);
				continue;
			} catch (JAXBException e) {
				results.add(new CheckResult(hostNames[i], false, "Can't connect to " + hostNames[i] + FILECHECK_URL));
				tagLogger.log("Skip checking "+ hostNames[i]);
				continue;
			} 					
			if (successfulTest) {
				addSuccessfullCheckResultIfPreviousFailed(results, hostNames[i], LOCAL_HOST_NAME+" says: there are no files not existing in Sigma cache over " + includeMaxHours + " hour at " + hostNames[i]);
				//tagLogger.log(hostNames[i] + " passed check.");
			} else {
				String mess = LOCAL_HOST_NAME+" says: found file(s) older then "+includeMaxHours+" hour not existing in Sigma cache at "+hostNames[i];
				results.add(new CheckResult(hostNames[i], false, mess));
				tagLogger.log(mess);
			}			
		}			
		return results;
	}
	
	private TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}
		@Override
		public void checkClientTrusted(
				java.security.cert.X509Certificate[] arg0, String arg1)
				throws CertificateException {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void checkServerTrusted(
				java.security.cert.X509Certificate[] arg0, String arg1)
				throws CertificateException {
			// TODO Auto-generated method stub
			
		}
	}
	};
	
	private FileDiffList readXMLFromHost(String host) throws NoSuchAlgorithmException, KeyManagementException, JAXBException, IOException {
		FileDiffList fdl = null;
		URL url = new URL(host + FILECHECK_URL);
		HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();		
		httpsConnection.setSSLSocketFactory(socketFactory);
		httpsConnection.setDoInput(true);
		httpsConnection.setRequestMethod("POST");
        httpsConnection.setRequestProperty("Content-type", "text/xml");        
        
        try {
        	Object o = XMLHelper.readXML(httpsConnection.getInputStream(), FileDiff.class, FileDiffList.class);   
        	if (o instanceof FileDiffList) {
				fdl = (FileDiffList) o;
			}
        } finally {
        	httpsConnection.getInputStream().close();
        }
		return fdl;
	}	

	
	/**
	 * получить SSLSocketFactory, которая будет передавать в запросе сертифиикат клиента
	 * @param is - поток данных pkcs12 хранилища
	 * @param password - паролю к хранилищу
	 * @return
	 * @throws CertificateException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws IOException
	 * @throws UnrecoverableKeyException
	 * @throws KeyManagementException
	 */
	private SSLSocketFactory getFactory( InputStream is,String password ) throws CertificateException,NoSuchAlgorithmException,KeyStoreException,IOException,UnrecoverableKeyException,KeyManagementException  {
		  KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
		  KeyStore keyStore = KeyStore.getInstance("PKCS12");
		  keyStore.load(is, password.toCharArray());
		  keyManagerFactory.init(keyStore, password.toCharArray());
		  SSLContext context = SSLContext.getInstance("TLS");
		  context.init(keyManagerFactory.getKeyManagers(), trustAllCerts, new SecureRandom());
		  return context.getSocketFactory();
	}
	
	@Override
	protected void doStart() {
    	logServiceMessage(LogEventType.SERV_START, "starting service");
    	byte[] cert = Base64.decode(PKCS12);    	
    	InputStream input = new ByteArrayInputStream(cert);
    	try {
			socketFactory = getFactory(input, password);		
		} catch (Exception e) {
			//в случае ошибки socketFactory отсанется null и этот случай обработается в doCheck()
		}
        logServiceMessage(LogEventType.SERV_START, "started service");
	}
	
	@Override
	public String getDescription() {
		return "Чекер проверки состояния кеша";
	}
	
}

