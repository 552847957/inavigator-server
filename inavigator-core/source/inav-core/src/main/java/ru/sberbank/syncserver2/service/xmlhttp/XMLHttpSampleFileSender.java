package ru.sberbank.syncserver2.service.xmlhttp;

import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.util.XMLEscapeHelper;
import ru.sberbank.syncserver2.util.XMLHelper;

import java.io.File;

/**
 * Created by User on 27.02.15.
 */
public class XMLHttpSampleFileSender extends SingleThreadBackgroundService {
    private String sampleFileName;
    private String senderBeanCode;
    private XMLHttpSender sender;

    public XMLHttpSampleFileSender() {
        super(60);
    }

    public String getSampleFileName() {
        return sampleFileName;
    }

    public void setSampleFileName(String sampleFileName) {
        this.sampleFileName = sampleFileName;
    }

    public String getSenderBeanCode() {
        return senderBeanCode;
    }

    public void setSenderBeanCode(String senderBeanCode) {
        this.senderBeanCode = senderBeanCode;
    }

    @Override
    public void doInit() {
    }

    private void resolveSender() {
        if(sender==null){
            ServiceManager sm = ServiceManager.getInstance();
            ServiceContainer sc = sm.findServiceByBeanCode(senderBeanCode);
            sender = (XMLHttpSender) sc.getService();
        }
    }

    @Override
    public void doRun() {
        //1. Getting sender in case of restart
        //System.out.println("doRun 1");
        resolveSender();

        //2. Reading files, marshalling them to XML and sending
        //System.out.println("doRun 2");
        XMLHttpJAXBConfig config = sender.getConfig();

        //3. Load sample
        //System.out.println("doRun 3 - reading data from "+sampleFileName);
        Object sample = XMLHelper.readXML(new File(sampleFileName), config.getRequestClasses());
        //System.out.println("doRun 4 - "+sample);
        logSample(sample);
        //System.out.println("doRun 5");
        sender.send(sample);
        //System.out.println("doRun 6");
    }

    private void logSample(Object data){
        StringBuilder sb = new StringBuilder();
        sb.append("============================================== START SENDING ==================");
        XMLHttpJAXBConfig config = sender.getConfig();
        //Class[] cc = config.getRequestClasses();
        //for (int i = 0; i < cc.length; i++) {
        //    System.out.println("Class name = "+cc[i].getName());
        //}
        String s = XMLHelper.writeXMLToString(data, true, config.getRequestClasses());
        sb.append(s);
        sb.append("==============================================================================");
        String str = XMLEscapeHelper.escapeCharacters(s);
        //System.out.println(data);
        tagLogger.log(str);
    }
}
