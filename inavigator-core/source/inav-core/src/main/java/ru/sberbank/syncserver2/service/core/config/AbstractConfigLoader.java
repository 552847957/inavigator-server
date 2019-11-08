package ru.sberbank.syncserver2.service.core.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;

import java.sql.*;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by sbt-kozhinsky-lb on 20.03.14.
 */
public abstract class AbstractConfigLoader implements ConfigLoader {
    protected String QUERY_LIST_FOLDERS                			;//= "exec SP_SYNC_LIST_FOLDERS";
    protected String QUERY_LIST_SERVICES               			;//= "exec SP_SYNC_LIST_SERVICES ?";
    protected String QUERY_LIST_PROPERTIES             			;//= "exec SP_SYNC_LIST_PROPERTIES ?, ?";
    protected String QUERY_SET_PROPERTY                			;//= "exec SP_SYNC_SET_PROPERTY ?, ?, ?, ?";
    protected String QUERY_RESET_PROPERTIES            			;//= "exec SP_SYNC_RESET_PROPERTIES ?, ?";
    protected String QUERY_MACROS                      			;//= "select ROOT_FOLDER,DB_NAME() dbname from SYNC_INSTANCE_CONFIG";
    protected String QUERY_LIST_STATIC_FILES          			;//= "SELECT APP_CODE,FILE_ID,FILE_NAME from SYNC_CACHE_STATIC_FILES";

    private DataSource dataSource;
    private String databaseName;
    private String rootFolder;
    private Properties macros;

    protected JdbcTemplate jdbcTemplate;

    public void init() {
        jdbcTemplate = new JdbcTemplate(dataSource);
        databaseName = "syncserver"; //TODO - should be changed
    }


    public DataSource getDataSource() {
        return dataSource;
    }

    @Qualifier("configSource")
    public void setDataSource(DataSource dataSource) {
        //1. Set datasource
        this.dataSource = dataSource;

        //2. Get database name
        reloadMacros();

        //3. Set default value if required
        if(databaseName==null){
            databaseName = "syncserver";
        }
    }


	private void reloadMacros() {
		Connection conn = null;
        Statement st = null;
        ResultSet rs = null;
        Properties localMacros = null;
        try {
            conn = dataSource.getConnection();
            st = conn.createStatement();
            rs = st.executeQuery(QUERY_MACROS);
            localMacros = new Properties();
            while(rs.next()){
                String key = rs.getString(1);
                String value = rs.getString(2);
                localMacros.put(key,value);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if(rs!=null){
                try { rs.close(); } catch(Exception e){}
            }
            if(st!=null){
                try { st.close(); } catch(Exception e){}
            }
            if(conn!=null){
                try { conn.close(); } catch(Exception e){}
            }
        }
        
        synchronized(this){
        	macros = localMacros;
        }
	}

    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @return folder list
     */
    @Override
    public List<Folder> getFolders() {
        return jdbcTemplate.query(QUERY_LIST_FOLDERS, new RowMapper<Folder>() {

            @Override
            public Folder mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new Folder(rs.getString("SYNC_FOLDER_CODE"), rs.getInt("START_ORDER"), rs.getString("SYNC_FOLDER_DESC"));
            }

        });
    }

    /**
     * @param folderCode
     * @return beans list
     */
    @Override
    public List<Bean> getBeans(final String folderCode) {
        List<Bean> beans = jdbcTemplate.query(QUERY_LIST_SERVICES,
                new PreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement preparedStatement) throws SQLException {
                        preparedStatement.setString(1, folderCode);
                    }
                },
                new RowMapper<Bean>() {

                    @Override
                    public Bean mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Bean bean = new Bean(
                                rs.getLong("SYNC_SERVICE_ID"),
                                rs.getString("BEAN_CODE"),
                                rs.getString("BEAN_CLASS"),
                                rs.getString("PARENT_BEAN_CODE"),
                                rs.getString("PARENT_BEAN_PROPERTY"),
                                rs.getInt("START_ORDER"),
                                rs.getString("PUBLIC_SERVLET_PATH"),
                                rs.getString("BEAN_DESC"));
                        try {
                        	bean.setStopped(rs.getBoolean("STOPPED"));
                        } catch (Exception e) {
                        	//ignore (default false)
                        }
                        return bean;
                    }

                });
        for (int i = 0; i < beans.size(); i++) {
            Bean bean =  beans.get(i);
            List<BeanProperty> properties = getBeanProperties(folderCode, bean.getCode(), false);
            bean.setBeanProperties(properties);
        }
        return beans;
    }

    /**
     * @param folderCode
     * @param beanCode
     * @return beans properties
     */
    @Override
    public List<BeanProperty> getBeanProperties(final String folderCode,
                                                final String beanCode, boolean reloadMacros) {

        // exec SP_SYNC_LIST_PROPERTIES 'mis_syncserver_mirrow', 'mis','misNetworkMover'
    	if (reloadMacros) {
    		reloadMacros();
    	}

        return jdbcTemplate.query(QUERY_LIST_PROPERTIES,
                new PreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement preparedStatement) throws SQLException {
                        preparedStatement.setString(1, folderCode);
                        preparedStatement.setString(2, beanCode);
                    }
                },
                new RowMapper<BeanProperty>() {

                    @Override
                    public BeanProperty mapRow(ResultSet rs, int rowNum) throws SQLException {
                        BeanProperty bp =  new BeanProperty(
                                rs.getLong("VALUE_ID"),
                                rs.getString("TEMPLATE_CODE"),
                                rs.getString("VALUE"),
                                rs.getString("TEMPLATE_DESC")
                                );

                        String value = bp.getValue();
                        if(value!=null){
//                            for (java.util.Iterator iterator = macros.entrySet().iterator(); iterator.hasNext(); ) {
//                                Map.Entry entry = (Map.Entry) iterator.next();
//                                String propKey = "@" + String.valueOf(entry.getKey())+ "@";
//                                String propValue = String.valueOf(entry.getValue());
//                                value = value==null ? null : value.replaceAll(propKey, propValue);
//                            }
                            value = applyMacrosToString(value);
                        }
                        bp.setValue(value);
                        return bp;
                    }

                });
    }

    /**
     * Reset or create bean properties
     *
     * @param folderCode
     * @param beanCode
     */
    @Override
    public void resetServiceProperties(String folderCode, String beanCode) {
        // exec SP_SYNC_RESET_PROPERTIES 'mis_syncserver_mirrow', 'mis','misNetworkMover'
        jdbcTemplate.update(QUERY_RESET_PROPERTIES, folderCode, beanCode);
    }

    /**
     * @return folder list
     */
    @Override
    public List<StaticFileInfo> getStaticFileList() {
        return jdbcTemplate.query(QUERY_LIST_STATIC_FILES, new RowMapper<StaticFileInfo>() {

            @Override
            public StaticFileInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
                return new StaticFileInfo(
                		rs.getString("APP_CODE") 
                		,rs.getString("FILE_ID") 
                		,rs.getString("FILE_NAME") 
                		,rs.getString("HOSTS")
                		,(rs.getInt("IS_AUTO_GEN_ENABLED") == 1)
                		,(rs.getInt("GENERATION_MODE") == 1)
                		,(rs.getString("DRAFT_FILE_MD5") != null && !rs.getString("DRAFT_FILE_MD5").equals(""))
                		,(rs.getString("PUBLISHED_FILE_MD5") != null && !rs.getString("PUBLISHED_FILE_MD5").equals(""))
                );            
            }

        });
    }

    public void executePattern(String sql, Object... values){
        jdbcTemplate.update(sql, values);
    }

    @Override
    public <T> T getValue(final String sql, ResultSetExtractor<T> resultSetExtractor, final Object... values) {
        return jdbcTemplate.query(
                sql,
                new PreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement preparedStatement) throws SQLException {
                        if (values != null && values.length > 0) {
                            for (int i = 0; i < values.length; i++) {
                                if (values[i] instanceof Timestamp) {
                                    preparedStatement.setTimestamp(i + 1, (Timestamp) values[i]);
                                } else if (values[i] instanceof Date) {
                                    preparedStatement.setDate(i + 1, new java.sql.Date(((Date) values[i]).getTime()));
                                } {
                                    preparedStatement.setObject(i + 1, values[i]);
                                }
                            }
                        }
                    }

                },
                resultSetExtractor);
    }

    @Override
    public String getSyncConfigProperty(String propertyName) {
        String sql = "SELECT PROPERTY_VALUE FROM SYNC_CONFIG WHERE PROPERTY_KEY = '"+propertyName+"'";
        return getValue(sql, new ResultSetExtractor<String>() {
            @Override
            public String extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                if(resultSet.next()){
                    return resultSet.getString(1);
                } else {
                    return "";
                }
            }
        });
    }
    
    /**
     * Применить все загруженные в конфиги макросы к входной строке
     * @param source
     * @return
     */
    public synchronized String applyMacrosToString(String source) {
        for (java.util.Iterator iterator = macros.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String propKey = "@" + String.valueOf(entry.getKey())+ "@";
            String propValue = String.valueOf(entry.getValue());
            source = source==null ? null : source.replaceAll(propKey, propValue);
        }
        return source;
    }
    
    /**
     * Перечить данные конфигурации макросов
     */
    public void updateMacros() {
    	reloadMacros();
    }


	@Override
	public void setStoppedState(String folderCode, String beanCode,	Boolean stopped) throws Exception {
		jdbcTemplate.update("UPDATE SYNC_SERVICES SET STOPPED= ? WHERE SYNC_FOLDER_CODE= ? AND BEAN_CODE= ?", new Object[] {stopped, folderCode, beanCode});
	}    
    
}
