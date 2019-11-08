package ru.sberbank.syncserver2.service.core.config;


/**
 * @author Sergey Erin
 *
 */

public class SQLiteConfigLoader extends AbstractConfigLoader {

    public SQLiteConfigLoader() {
        QUERY_LIST_FOLDERS                		  = "SELECT * FROM SYNC_FOLDERS";
        QUERY_LIST_SERVICES               		  = "SELECT * FROM SYNC_SERVICES WHERE SYNC_FOLDER_CODE = ?";
        QUERY_LIST_PROPERTIES             		  = "SELECT * FROM SYNC_SERVICE_PROPERTIES  WHERE SYNC_FOLDER_CODE = ? and BEAN_CODE = ?";
        QUERY_SET_PROPERTY                		  = "UPDATE SYNC_SERVICE_PROPERTIES SET VALUE = ? WHERE SYNC_FOLDER_CODE = ? and BEAN_CODE = ? and TEMPLATE_CODE = ? ";
        QUERY_RESET_PROPERTIES            		  = "UPDATE SYNC_SERVICE_PROPERTIES SET VALUE = '' WHERE SYNC_FOLDER_CODE = ? and BEAN_CODE = ?";
        QUERY_MACROS                      		  = "SELECT PROPERTY_KEY, PROPERTY_VALUE FROM SYNC_CONFIG\n" +
                                            		"UNION ALL\n" +
                                            		"SELECT 'DB_NAME', 'passport'";
        QUERY_LIST_STATIC_FILES           		  = "SELECT APP_CODE,FILE_ID,FILE_NAME,HOSTS,IS_AUTO_GEN_ENABLED from SYNC_CACHE_STATIC_FILES";
    }

    /**
     * @param folderCode
     * @param beanCode
     * @param property
     */
    @Override
    public void setServiceProperty(String folderCode,
                                   String beanCode,
                                   BeanProperty property) {
        //UPDATE SYNC_SERVICE_PROPERTIES SET VALUE = 'C:\\qqq' WHERE SYNC_FOLDER_CODE = 'mis' and BEAN_CODE = 'misNetworkMover' and TEMPLATE_CODE = 'srcFolder'
        jdbcTemplate.update(QUERY_SET_PROPERTY,
                property.getValue(),
                folderCode,
                beanCode,
                property.getCode());
    }

}
