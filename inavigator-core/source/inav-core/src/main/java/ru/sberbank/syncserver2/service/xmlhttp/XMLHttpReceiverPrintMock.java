package ru.sberbank.syncserver2.service.xmlhttp;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.util.XMLEscapeHelper;
import ru.sberbank.syncserver2.util.XMLHelper;


import java.util.Date;

/**
 * Created by User on 27.02.15.
 */
public class XMLHttpReceiverPrintMock extends AbstractService implements XMLHttpReceiverListener {
    private String sampleResponseClass;

    public String getSampleResponseClass() {
        return sampleResponseClass;
    }

    public void setSampleResponseClass(String sampleResponseClass) {
        this.sampleResponseClass = sampleResponseClass;
    }

    private void logSample(Object data){
        StringBuilder sb = new StringBuilder();
        sb.append("============================================== RECEIVED DATA ==================");
        //Class c = Class.forName(sampleResponseClass);
        //String s = XMLHelper.writeXMLToString(data, true, c);
        sb.append(data);
        sb.append("==============================================================================");
        //String str = XMLEscapeHelper.escapeCharacters(s);
        //System.out.println(data);
        tagLogger.log(sb.toString());
    }

    @Override
    public Object receive(Object data) {
        logSample(data);
        try {
            Object result = Class.forName(sampleResponseClass).newInstance();
            return result;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        //RDReport sampleResponse = new RDReport();
        //sampleResponse.setCaption("Success at "+new Date());
        //return sampleResponse;
        return null;
    }

    @Override
    protected void doStop() {

    }

    @Override
    protected void waitUntilStopped() {

    }
}
