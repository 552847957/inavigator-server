package ru.sberbank.syncserver2.service.xmlhttp;

import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.PublicService;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.util.XMLHelper;

/**
 * Created by User on 27.02.15.
 */
public class XMLHttpReceiver extends AbstractService implements PublicService {
    private String listenerBeanCode;
    private XMLHttpReceiverListener listener;
    private String configBeanCode;
    private XMLHttpJAXBConfig config;

    public String getListenerBeanCode() {
        return listenerBeanCode;
    }

    public void setListenerBeanCode(String listenerBeanCode) {
        this.listenerBeanCode = listenerBeanCode;
    }

    public XMLHttpReceiverListener getListener() {
        return listener;
    }

    public void setListener(XMLHttpReceiverListener listener) {
        this.listener = listener;
    }

    public String getConfigBeanCode() {
        return configBeanCode;
    }

    public void setConfigBeanCode(String configBeanCode) {
        this.configBeanCode = configBeanCode;
    }

    public XMLHttpJAXBConfig getConfig() {
        return config;
    }

    public void setConfig(XMLHttpJAXBConfig config) {
        this.config = config;
    }

    @Override
    public void request(HttpServletRequest request, HttpServletResponse response) {
    	tagLogger.log(getServiceBeanCode(), "New request");
        //1. Resolving config if necessary
        //System.out.println("RECEIVE 1");
        if(config==null){
            ServiceManager sm = ServiceManager.getInstance();
            ServiceContainer sc = sm.findServiceByBeanCode(configBeanCode);
            config = (XMLHttpJAXBConfig) sc.getService();
        }

        //2. Unmarshalling data from input stream
        //System.out.println("RECEIVE 2");
        Object requestObject = null;
        try {
            requestObject = XMLHelper.readXML(request.getInputStream(), config.getRequestClasses());
        } catch (Exception e) {
            e.printStackTrace();
        }


        //2. Notify listener
        //2.1. Find listener if necessary
        //System.out.println("RECEIVE 3");
        Object resultObject = null;
        if(listener==null){
            ServiceManager sm = ServiceManager.getInstance();
            ServiceContainer sc = sm.findServiceByBeanCode(listenerBeanCode);
            listener = (XMLHttpReceiverListener) sc.getService();
        }
        //System.out.println("RECEIVE 4");
        if(listener!=null){
            //System.out.println("RECEIVE 5");
        	tagLogger.log(getServiceBeanCode(), "notify listener");
            resultObject = listener.receive(requestObject);
        }

        //3. Send reply back
        //System.out.println("RECEIVE 6");
        ServletOutputStream output = null;
        try {
            response.setContentType("text/xml");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            output = response.getOutputStream();
            XMLHelper.writeXMLWithBuffer(output,resultObject, true, config.getResponseClasses());
            //output.write(result.getBytes("UTF-8"));
            output.flush();
        	tagLogger.log(getServiceBeanCode(), "request proceed");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(output);
        }
        return;
    }

    @Override
    protected void doStop() {

    }

    @Override
    protected void waitUntilStopped() {

    }
}
