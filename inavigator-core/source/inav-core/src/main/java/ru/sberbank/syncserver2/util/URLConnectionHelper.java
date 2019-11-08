package ru.sberbank.syncserver2.util;

import org.apache.commons.io.IOUtils;
import ru.sberbank.syncserver2.service.core.ResponseError;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class URLConnectionHelper {
	private URLConnectionHelper() {}

	public static HttpsURLConnection createSecureConnection(URL url, SSLSocketFactory socketFactory, String method, String contentType) throws IOException {
		HttpsURLConnection httpsConnection = (HttpsURLConnection) url.openConnection();
		httpsConnection.setSSLSocketFactory(socketFactory);
		httpsConnection.setRequestMethod(method);
		httpsConnection.setDoOutput("GET".equals(method) ? false : true);
		httpsConnection.setDoInput(true);
        httpsConnection.setRequestProperty("Content-type", contentType);
        httpsConnection.setHostnameVerifier(new HostnameVerifier() {

			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		});

        return httpsConnection;
	}

	public static HttpsURLConnection createPOST(URL url, SSLSocketFactory socketFactory) throws IOException {
		return createSecureConnection(url, socketFactory, "POST", "text/xml");
	}

	/**
	 * Выполнить запрос и вернуть результат или бросить Exception
	 * @param urlString
	 * @param request
	 * @param socketFactory
	 * @return DataResponse
	 * @throws Exception
	 */
	public static DataResponse doRequest(String urlString, String request, SSLSocketFactory socketFactory) throws Exception {
		URL url = new URL(urlString);
		HttpsURLConnection httpsConnection = URLConnectionHelper.createPOST(url, socketFactory);

        OutputStream output = null;
        InputStream input = null;
        try {
        	output = httpsConnection.getOutputStream();
        	output.write(request.getBytes("UTF-8"));
        	output.close();
        	if (httpsConnection.getResponseCode()==HttpURLConnection.HTTP_OK) {
        		input = httpsConnection.getInputStream();
        		Object o = XMLHelper.readXML(input, DataResponse.class, ResponseError.class);
        		if (o instanceof DataResponse) {
        			return (DataResponse) o;
    			}
        		if (o instanceof ResponseError) {
        			ResponseError er = (ResponseError) o;
        			throw new Exception("Error in received response from "+urlString+" "+er.getDescription());
        		}
        		throw new Exception("Can't convert response from host "+urlString+" to DataResponse or ResponseError");
        	} else {
        		throw new Exception("Can't perfom request to "+urlString+". Response code is "+httpsConnection.getResponseCode());
        	}
        } finally {
        	IOUtils.closeQuietly(output);
        	IOUtils.closeQuietly(input);
        }
	}

}
