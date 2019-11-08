package ru.sberbank.syncserver2.service.xmlhttp;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by User on 27.02.15.
 */
public class XMLHttpSender extends AbstractService {
    private String url;
    private String configBeanCode;
    private XMLHttpJAXBConfig config;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getConfigBeanCode() {
        return configBeanCode;
    }

    public void setConfigBeanCode(String configBeanCode) {
        this.configBeanCode = configBeanCode;
    }

    public XMLHttpJAXBConfig getConfig() {
        resolveConfig();
        return config;
    }

    public void setConfig(XMLHttpJAXBConfig config) {
        this.config = config;
    }

    protected void doStart(){
        super.doStart();
        resolveConfig();
    }

    private synchronized void resolveConfig(){
        if(config==null){
            ServiceManager sm = ServiceManager.getInstance();
            ServiceContainer sc = sm.findServiceByBeanCode(configBeanCode);
            config = (XMLHttpJAXBConfig) sc.getService();
        }
    }

    public Object send(Object requestData){
        //1. Resolve config if necessary
        tagLogger.log("Started sending data to url " + url);
//        System.out.println("Started sending data to url " + url+" : "+requestData);
        resolveConfig();

        //2. Initialize connection
        HttpURLConnection httpConnection = null;
        try {
            URL urlObject = new URL(url);
            httpConnection = (HttpURLConnection) urlObject.openConnection();

            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);

            httpConnection.setRequestMethod("POST");
            httpConnection.setRequestProperty("Accept", "text/xml");
            httpConnection.setRequestProperty("Content-type", "text/xml");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw new IllegalStateException("Fail to initialize connection to " + url+ " . Please check configuration of "+getServiceBeanCode());
        } catch (ProtocolException e) {
            e.printStackTrace();
            throw new IllegalStateException("Fail to initialize connection to " + url+ " . Please check configuration of "+getServiceBeanCode());
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Fail to initialize connection to " + url+ " . Please check configuration of "+getServiceBeanCode());
        } finally {
        }

        //3. Sending
        Object response = null;
        try {
            //2.1. Sending requjest
            OutputStreamWriter out = new OutputStreamWriter(httpConnection.getOutputStream(), "UTF-8");
            String requestString = XMLHelper.writeXMLToString(requestData, true, config.getRequestClasses());
            out.write(requestString);
            out.close();
            if (httpConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IllegalStateException("Error receiving response from " + url);
            }

            //2.2. Reading response
            response = XMLHelper.readXML(httpConnection.getInputStream(), config.getResponseClasses());
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            throw new IllegalStateException("Fail to send request to " + url+ " . Please the sent data:\n "+requestData);
        } catch (IOException e) {
            e.printStackTrace();
            throw new IllegalStateException("Fail to send request to " + url+ " . Please the sent data:\n "+requestData);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new IllegalStateException("Fail to send request to " + url+ " . Please the sent data:\n "+requestData);
        } finally {
            try {
                httpConnection.getInputStream().close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
        }
        return response;
    }

    @Override
    protected void doStop() {

    }

    @Override
    protected void waitUntilStopped() {

    }

    //public static void main(String[] args) {
        //RDReport report = (RDReport) XMLHelper.readXML(new File("C:\\usr\\temp\\xmlhttp\\samples\\sample.xml"), RDReport.class);
        //System.out.println("REPORT "+report.getSeries());
    //}
}
