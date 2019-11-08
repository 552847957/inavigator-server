package ru.sberbank.syncserver2.gui.db.dao;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import ru.sberbank.syncserver2.gui.db.DatabaseServices;

public class BaseDao extends DatabaseServices {
	
    private DataSource configDataSource;

	protected JdbcTemplate jdbcTemplate;
	
	public DataSource getConfigSource() {
		return configDataSource;
	}
	
    @Autowired
    @Qualifier("configSource")
    public void setConfigSource(DataSource configDataSource) {
        this.configDataSource = configDataSource;
        if ((configDataSource != null) && (jdbcTemplate == null))
        	jdbcTemplate = new JdbcTemplate(configDataSource);
    }	
	

}
