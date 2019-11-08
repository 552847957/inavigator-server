package ru.sberbank.syncserver2.service.core.config;

import org.springframework.jdbc.core.ResultSetExtractor;

import java.util.List;

/**
 * @author Sergey Erin
 *
 */
public interface ConfigLoader {

        /**
         * @return name of the database and it is equal server instance
         */
        String getDatabaseName();

       /**
         * @return list of folders
         */
        List<Folder> getFolders();

        /**
         * @param folderCode
         * @return
         */
        List<Bean> getBeans(String folderCode);

        /**
         * @param folderCode
         * @param beanCode
         * @param reloadMacros TODO
         * @return
         */
        List<BeanProperty> getBeanProperties(String folderCode, String beanCode, boolean reloadMacros);

        /**
         * @param folderCode
         * @param beanCode
         * @param property
         */
        void setServiceProperty(String folderCode,String beanCode, BeanProperty property);

        /**
         * @param folderCode
         * @param beanCode
         */
        void resetServiceProperties(String folderCode,String beanCode);

        /**
         *
         * @return list of files for file cache
        */
        public List<StaticFileInfo> getStaticFileList();

        /**
        * Execute pattern
        * @param sql prepared statement sql
        * @param values values
        */
        public void executePattern(String sql, Object... values);

        /**
         * Access to dataase with resultset extractor
         * @param sql
         * @param resultSetExtractor
         * @param values
         * @param <T>
         * @return
         */
        public <T> T getValue(final String sql, ResultSetExtractor<T> resultSetExtractor, final Object... values);

        /**
         * Returns property from SYNC_CONFIG
         * @return
         */
        public String getSyncConfigProperty(String propertyName);
        
        
        /**
         * Set state to service
         * @param folderCode
         * @param beanCode
         * @param stopped
         * @throws Exception 
         */
        public void setStoppedState(String folderCode, String beanCode, Boolean stopped) throws Exception;

        
}
