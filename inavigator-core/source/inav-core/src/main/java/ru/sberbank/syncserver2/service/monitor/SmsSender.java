package ru.sberbank.syncserver2.service.monitor;

import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.log4j.Logger;
import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.monitor.data.SendSmsCommand;
import ru.sberbank.syncserver2.service.monitor.data.Target;

import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * User: SBT-Karmanov-AV
 * Date: 30.07.12
 * Time: 17:37
 */
public class SmsSender extends AbstractService{
    private static final Logger log = Logger.getLogger(SmsSender.class);
    protected String smsProxyUrl;

    public String getSmsProxyUrl() {
        return smsProxyUrl;
    }

    public void setSmsProxyUrl(String smsProxyUrl) {
        this.smsProxyUrl = smsProxyUrl;
    }

    public boolean sendSms(String text, Set<String> recips) {
        List<String> recipList = new ArrayList<String>();
        return sendSms(text, recipList);
    }

    public boolean sendSms(String text, List<String> recips) {
        List<String> recipients = new ArrayList<String>();
        boolean result = recips.isEmpty();
        HttpClient client = new DefaultHttpClient();
        try {            
            for (Iterator<String> recipient = recips.iterator(); recipient.hasNext(); ) {
                recipients.add(recipient.next());
                SendSmsCommand jaxbElement = new SendSmsCommand();
                jaxbElement.setTarget(Target.sms);
                jaxbElement.setText(DatatypeConverter.printHexBinary(text.getBytes("UTF-8")));
                jaxbElement.setAddress(recipients);
                ByteArrayOutputStream output;            
                
                try {
                    JAXBContext context = JAXBContext.newInstance(SendSmsCommand.class);
                    Marshaller marshaller = context.createMarshaller();
                    output = new ByteArrayOutputStream();
                    marshaller.marshal(jaxbElement, output);
                    
                    String proxyURL = smsProxyUrl;
                    HttpPost post = new HttpPost(proxyURL);
                    post.setHeader("Pragma", "no-cache");
                    post.setHeader("Expires", "0");
                    post.setHeader("Cache-Control", "no-cache");
                    post.setHeader("Content-Type", "application/xml");
                    post.setEntity(new ByteArrayEntity(output.toByteArray()));
                    int retries = 0;
                    boolean sucess = false;
                    while (retries < 10 && !sucess) {
                        try {
                            log.info("trying to SendSmsRequest: " + new String(output.toByteArray()));
                            StatusLine statusLine = client.execute(post).getStatusLine();
                            log.info("SendSmsResponse: " + statusLine.getStatusCode() + " - " + statusLine.getReasonPhrase());
                            if (statusLine.getStatusCode() == 200)
                                sucess = true;
                            else if (retries == 9)
                            	tagLogger.log("Can't send sms: SendSmsResponse: " + statusLine.getStatusCode() + " - " + statusLine.getReasonPhrase());
                        } catch (IOException e) {
                            log.error("cannot send sms to DP", e);
                            if (retries == 9)
                            	tagLogger.log("Can't send sms to DP: "+e.getMessage());
                        }
                        retries++;
                    }
                    result |= sucess;
                } catch (JAXBException e) {
                    log.error("cannot marshal to SendSmsCommand [text=" + text + ",recipients=" + recipients.toString() + " ] ", e);
                }
                recipients.clear();
            }
        } catch (Throwable e) {
        	e.printStackTrace();
        	tagLogger.log("Can't send sms: " + e.getMessage());
        	return false;
        }      
        return result;
    }

    @Override
    protected void doStop() {
    }

    @Override
    protected void waitUntilStopped() {
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        SmsSender sender = new SmsSender();
        sender.smsProxyUrl = "hello.sms";
        sender.sendSms("This is a test", Collections.singleton("+79857619675"));
    }


}
