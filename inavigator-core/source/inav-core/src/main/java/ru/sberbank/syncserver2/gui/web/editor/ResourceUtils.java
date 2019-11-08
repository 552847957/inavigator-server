package ru.sberbank.syncserver2.gui.web.editor;

import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: sbt-bubnov-vy
 * Date: 31.08.12
 * Time: 10:34
 */
public class ResourceUtils {
    private static Logger log = Logger.getLogger(ResourceUtils.class);

    public static final String TOMCAT_JNDI_PREFIX = "java:comp/env/";

    public static String VITO_CHECK_PERMISSIONS="vito.check_permissions";

    public static String JNDI_SYNC_SERVER_ROOT = "sync_server.root";
    public static String SYNC_SERVER_MANUAL_SERVLET = "ManualRunServlet";

    public static ConcurrentMap<String,String> resources = new ConcurrentHashMap<String, String>();
    public static AtomicReference<Context> ic = new AtomicReference<Context>();


    public static String getSyncServerURL() {
        String syncServerRoot = getJNDIResource(JNDI_SYNC_SERVER_ROOT,"/syncserver");
        return syncServerRoot+"/"+SYNC_SERVER_MANUAL_SERVLET;
    }

    public static String getJNDIResource(String jndi) throws NamingException {
        if (!resources.containsKey(jndi)){
            try {
                resources.put(jndi, (String) getContext().lookup(jndi));
            } catch (Exception ex) {
                resources.put(jndi,  (String) getContext().lookup(TOMCAT_JNDI_PREFIX + jndi) );
            }
        }
        return resources.get(jndi);
    }

    public static String getJNDIResource(String jndi, String defaultVal){
        if (!resources.containsKey(jndi)){
            try{
               return getJNDIResource(jndi);
            }catch (Exception e){
               if (resources.putIfAbsent(jndi,defaultVal)==null)
                log.warn("Error reading resource '"+jndi+"', default value '"+defaultVal+"' will be used", e);
            }
        }
        return resources.get(jndi);
    }

    static Context getContext() throws NamingException {
        if (ic.get() == null) synchronized (ic) {
            ic.set(new InitialContext());
        }
        return ic.get();
    }
}
