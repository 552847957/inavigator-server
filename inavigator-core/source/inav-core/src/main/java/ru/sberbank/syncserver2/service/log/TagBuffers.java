package ru.sberbank.syncserver2.service.log;

import ru.sberbank.syncserver2.util.RingObjectBuffer;

import java.util.*;

/**
 * Created by sbt-kozhinsky-lb on 26.02.14.
 */
public class TagBuffers {
    private static Map<String,RingObjectBuffer> buffers = new HashMap();

    public static void log(String[] tags, LogAction text){
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            RingObjectBuffer buffer = getOrCreateBuffer(tag);
            buffer.add(text);
            //System.out.println(text);
        }
    }

    public static List<String> listTags(){
        synchronized (buffers){
            return new ArrayList(buffers.keySet());
        }
    }

    private static RingObjectBuffer getOrCreateBuffer(String tag){
        synchronized (buffers){
            RingObjectBuffer buffer = buffers.get(tag);
            if(buffer==null){
                buffer = new RingObjectBuffer(10,20000);
                buffers.put(tag,buffer);
            }
            return buffer;
        }
    }
    
    private static RingObjectBuffer getBuffer(String tag){
        synchronized (buffers){
            RingObjectBuffer buffer = buffers.get(tag);
            return buffer;
        }
    }


    public static List<LogAction> listActions(String tag) {
        RingObjectBuffer buffer = getBuffer(tag);
        if (buffer == null) {
            return Collections.EMPTY_LIST;
        } else {
            ArrayList<LogAction> arl = new ArrayList<LogAction>();
            Object[] lst = buffer.toArray();
            for (Object la: lst) {
                arl.add((LogAction)la);
            }
            return arl;
        }
    }
}
