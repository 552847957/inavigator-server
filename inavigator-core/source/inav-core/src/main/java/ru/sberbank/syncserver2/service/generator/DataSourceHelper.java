package ru.sberbank.syncserver2.service.generator;

import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by Admin on 05.04.14.
 */
public class DataSourceHelper {
    private static Map<Key,DataSource> dataSources = new HashMap<Key,DataSource>();

    public static DataSource getOrCreateDataSource(String jndiURl){
        DataSource ds = null;
        try {
            Context initContext = new InitialContext();
            Context envContext  = (Context)initContext.lookup("java:/comp/env");
            ds = (DataSource)envContext.lookup(jndiURl);
        } catch (NamingException e) {
            try {
                Context context = new InitialContext();
                ds  = (DataSource) context.lookup(jndiURl);
            } catch (NamingException e1) {
                e.printStackTrace();
            }
        }

        return ds;
    }

    public static DataSource getOrCreateDataSource(String driver, String url, String username, String password){
        //1. Building combined key
        Key key = new Key(driver,url,username,password);
        DataSource dataSource = dataSources.get(key);
        if(dataSource!=null){
            return dataSource;
        }

        //2. Creating datasource
        Properties props = new Properties();
        props.put("driverClassName", driver);
        props.put("url", url);
        props.put("username", username);
        props.put("password", password);
        try {
            dataSource = new DriverManagerDataSource(driver, url, username, password);
            dataSources.put(key, dataSource);
            return dataSource;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
        return null;
    }

    private static class Key {
        private String driver;
        private String url;
        private String username;
        private String password;

        private Key(String driver, String url, String username, String password) {
            this.driver = driver;
            this.url = url;
            this.username = username;
            this.password = password;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Key key = (Key) o;

            if (!driver.equals(key.driver)) return false;
            if (!password.equals(key.password)) return false;
            if (!url.equals(key.url)) return false;
            if (!username.equals(key.username)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = driver.hashCode();
            result = 31 * result + url.hashCode();
            result = 31 * result + username.hashCode();
            result = 31 * result + password.hashCode();
            return result;
        }
    }

}
