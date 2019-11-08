package ru.sberbank.syncserver2.service.xmlhttp;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;

/**
 * Created by sbt-kozhinsky-lb on 06.03.15.
 */
public class XMLHttpResender extends AbstractService implements XMLHttpReceiverListener  {
    private String senderBeanCode;
    private XMLHttpSender sender;

    public XMLHttpResender() {
    }

    public String getSenderBeanCode() {
        return senderBeanCode;
    }

    public void setSenderBeanCode(String senderBeanCode) {
        this.senderBeanCode = senderBeanCode;
    }

    private void resolveSender() {
        if(sender==null){
            ServiceManager sm = ServiceManager.getInstance();
            ServiceContainer sc = sm.findServiceByBeanCode(senderBeanCode);
            sender = (XMLHttpSender) sc.getService();
        }
    }

    @Override
    public Object receive(Object data) {
        //1. Finding sender
        resolveSender();

        //2. Sending
        try {
            tagLogger.log("Start sending");
            return sender.send(data);
        } catch(Exception e){
            tagLogger.log("Sending error: " + e.getMessage());
            return null;
        } finally {
            tagLogger.log("Finish sending");
        }
    }

    @Override
    protected void doStop() {

    }

    @Override
    protected void waitUntilStopped() {

    }
}
