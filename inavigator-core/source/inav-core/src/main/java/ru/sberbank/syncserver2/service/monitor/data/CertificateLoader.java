package ru.sberbank.syncserver2.service.monitor.data;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Properties;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CertificateLoader {
	
	private final FileLoader<String> keyLoader;
	private final FileLoader<SSLSocketFactory> certificateLoader;
	private final String protocol;
	public CertificateLoader(String certFile, String passwordFile, String protocol) {
		this(certFile, passwordFile, protocol, null);
	}
	public CertificateLoader(String certFile, String passwordFile, String protocol, final TrustManager[] trustManagers) {
		super();
		this.protocol = protocol;
		this.keyLoader = new FileLoader<String>("/settings.properties",passwordFile) {
			@Override
			protected String readFromFile(File file) throws IOException {
				BufferedReader reader = null;
				try {
					reader = new BufferedReader(new FileReader(file));
					Properties properties = new Properties();
					properties.load(reader);
					return properties.getProperty("password");
				} finally {
					if (reader!=null)
						reader.close();
				}
				
			}
		};
		this.certificateLoader = new FileLoader<SSLSocketFactory>("/certificate.p12",certFile) {			
			@Override
			protected SSLSocketFactory readFromFile(File file) throws Exception{
				FileInputStream fis = null;
				byte[] b = new byte[(int) file.length()];
				try {
					fis = new FileInputStream(file);					
					fis.read(b);			
				} finally {
					if (fis!=null)
						fis.close();
				}					
				return getFactory(new ByteArrayInputStream(b), keyLoader.load(),
						(trustManagers != null ? trustManagers : trustAllCerts));
			}
		};
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
	private SSLSocketFactory getFactory( InputStream is,String password,  TrustManager[] managers) throws CertificateException,NoSuchAlgorithmException,KeyStoreException,IOException,UnrecoverableKeyException,KeyManagementException  {
		  KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		  KeyStore keyStore = KeyStore.getInstance("PKCS12");
		  keyStore.load(is, password.toCharArray());
		  keyManagerFactory.init(keyStore, password.toCharArray());
		  SSLContext context = SSLContext.getInstance(protocol);
		  context.init(keyManagerFactory.getKeyManagers(), managers, new SecureRandom());
		  return context.getSocketFactory();
	}
	
	private TrustManager[] trustAllCerts = new TrustManager[] {
			new X509TrustManager() {
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
	
	public SSLSocketFactory getSSLSocetFactory() throws Exception {
		return certificateLoader.load();
	}

}
