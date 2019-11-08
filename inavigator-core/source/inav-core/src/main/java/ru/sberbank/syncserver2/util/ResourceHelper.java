package ru.sberbank.syncserver2.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ResourceHelper {
	
    public static final String TOMCAT_JNDI_PREFIX="java:comp/env/";

    /**
     * lookup JNDI datasource as is and if error try to add tomcat prefix
     * @return
     */
    public static DataSource lookup(String aJNDIName) throws NamingException {
        Context ctx = new InitialContext();
        try{
            return (DataSource) ctx.lookup(aJNDIName);
        }catch (NamingException ex){
            if (aJNDIName.startsWith(ResourceHelper.TOMCAT_JNDI_PREFIX)){
                throw ex;
            }
            return (DataSource) ctx.lookup(ResourceHelper.TOMCAT_JNDI_PREFIX+aJNDIName);
        }
    }
    

}
