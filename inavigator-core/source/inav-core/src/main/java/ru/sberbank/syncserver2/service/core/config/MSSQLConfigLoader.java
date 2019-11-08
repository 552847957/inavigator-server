package ru.sberbank.syncserver2.service.core.config;

import ru.sberbank.syncserver2.util.ClusterHookProvider;


/**
 * @author Sergey Erin
 *
 */

public class MSSQLConfigLoader extends AbstractConfigLoader {

    public static final String MSSQL_QUERY_MACROS = "SELECT PROPERTY_KEY, PROPERTY_VALUE, PROPERTY_DESC FROM SYNC_CONFIG\n" +
            "UNION ALL\n" +
            "SELECT 'DB_NAME', db_name(), 'Имя базы MSSQL. Соотвествует db_name() '";
    public static final String EXEC_SP_SYNC_LIST_FOLDERS = "exec SP_SYNC_LIST_FOLDERS";
    public static final String EXEC_SP_SYNC_LIST_SERVICES = "exec SP_SYNC_LIST_SERVICES ?";
    public static final String EXEC_SP_SYNC_LIST_PROPERTIES = "exec SP_SYNC_LIST_PROPERTIES ?, ?";

    @Override
	public void init() {
		super.init();
		if (ClusterHookProvider.isClusterHooked()) {
			String dbNameForMacros = "db_name()+'_"+ClusterHookProvider.getSuffixForHook()+"'";
			QUERY_MACROS = "SELECT PROPERTY_KEY, PROPERTY_VALUE, PROPERTY_DESC FROM SYNC_CONFIG\n" +
                    		"UNION ALL\n" +
                    		"SELECT 'DB_NAME', "+dbNameForMacros+", 'Имя базы MSSQL. Соотвествует db_name() '";
		}
	}

    public MSSQLConfigLoader() {
        QUERY_LIST_FOLDERS                = EXEC_SP_SYNC_LIST_FOLDERS;
        QUERY_LIST_SERVICES               = EXEC_SP_SYNC_LIST_SERVICES;
        QUERY_LIST_PROPERTIES             = EXEC_SP_SYNC_LIST_PROPERTIES;
        QUERY_SET_PROPERTY                = "exec SP_SYNC_SET_PROPERTY ?, ?, ?, ?";
        QUERY_RESET_PROPERTIES            = "exec SP_SYNC_RESET_PROPERTIES ?, ?";
        QUERY_MACROS                      = MSSQL_QUERY_MACROS;
        QUERY_LIST_STATIC_FILES           = "SELECT APP_CODE,FILE_ID,FILE_NAME,HOSTS,IS_AUTO_GEN_ENABLED,GENERATION_MODE,DRAFT_FILE_MD5,PUBLISHED_FILE_MD5 from SYNC_CACHE_STATIC_FILES";
        
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

        // exec SP_SYNC_SET_PROPERTY 'mis_syncserver_mirrow', 'mis','misNetworkMover','srcFolder','C:\\qqq'
        jdbcTemplate.update(QUERY_SET_PROPERTY,
                folderCode,
                beanCode,
                property.getCode(),
                property.getValue());
    }

}
