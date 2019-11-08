package ru.sberbank.syncserver2.service.log;

import org.apache.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by sbt-kozhinsky-lb on 26.02.14.
 */
public class TagLogger {
    private Logger logger;
    private Class clazz;
    //private ThreadLocal<SimpleDateFormat> formatHolder = new ThreadLocal<SimpleDateFormat>();
    private String mandatoryTag;

    public static TagLogger getTagLogger(Class clazz){
        Logger logger = Logger.getLogger(clazz);
        return getTagLogger(clazz,logger);
    }

    public static TagLogger getTagLogger(Class clazz, Logger logger){
        return getTagLogger(clazz,logger,null);
    }

    public static TagLogger getTagLogger(Class clazz, Logger logger, String mandatoryTag){
        return new TagLogger(logger,clazz, mandatoryTag);
    }

    private TagLogger(Logger logger, Class clazz, String mandatoryTag) {
        this.logger = logger;
        this.clazz = clazz;
        this.mandatoryTag = mandatoryTag;
    }

    public void log(String[] tags, String text){
        //1. Anyway we should make usual logging
        logger.info(text);
        /*
        //2. Prepare date format holder
        SimpleDateFormat formatter = formatHolder.get();
        if(formatter==null){
            formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            formatHolder.set(formatter);
        }
        */

        //3. Prepare tagged message and log it
        //String taggedText = formatter.format(new Date())+" "+clazz.getSimpleName()+" "+text;
        LogAction logAction = new LogAction(new Date(), clazz.getSimpleName(), text);
        TagBuffers.log(tags, logAction);
        if(mandatoryTag!=null){
            TagBuffers.log(new String[]{mandatoryTag}, logAction);
        }
    }

    public void log(String tag, String text){
    	log(new String[]{tag},text);    	
    }

    public void log(String text){
        if(mandatoryTag!=null){
        	logger.info(text);
        	LogAction logAction = new LogAction(new Date(), clazz.getSimpleName(), text);
        	TagBuffers.log(new String[]{mandatoryTag}, logAction);        	
        } else {
            throw new UnsupportedOperationException("Operation is not allowed since no mandatory tag is defined.");
        }

    }

}
