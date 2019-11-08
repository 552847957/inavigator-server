package ru.sberbank.syncserver2.gui.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import ru.sberbank.syncserver2.gui.data.MobileLogMode;
import ru.sberbank.syncserver2.service.pub.xml.LogEvent;

@Component
public class MobileLogDao extends BaseDao {
  
    public List<LogEvent> listLogEvents() {
    	return super.list(LogEvent.class, "");
    }
    
    public List<MobileLogMode> listLogModes() {
    	return super.list(MobileLogMode.class, "");
    }
    
    public void deleteMobileLogMode(String userEmail, String device) {
    	super.delete(MobileLogMode.class, new String[] {userEmail, device});
    	clearLogs(userEmail);
    }
    
    public List<Object[]> listLogEvents(String where, String order,Object[] args, int from, int length) {
    	List<Object[]> result = new ArrayList<Object[]>();
		String sql = length<0?"SELECT * FROM SYNC_MOBILE_LOG":"SELECT * FROM (SELECT [RANK] = ROW_NUMBER() OVER ("+order+"), "+ 
			    "* FROM SYNC_MOBILE_LOG "+where+" ) A WHERE A.[RANK] BETWEEN "+(from+1)+" AND "+(from+length);
		result = jdbcTemplate.query(sql,args, new RowMapper<Object[]>() {

			@Override
			public Object[] mapRow(ResultSet arg0, int arg1) throws SQLException {
				return new Object[]{
						arg0.getString("DEVICE_INFO_SOURCE_ID"),
						arg0.getString("DEVICE_MODEL"),
						arg0.getString("IOS_VERSION"),
						arg0.getString("APP_VERSION"),
						arg0.getString("BUNDLE_ID"),
						arg0.getString("UPDATE_TIME"),
						
						arg0.getString("EVENT_SOURCE_ID"),
						arg0.getTimestamp("EVENT_TIME"),
						arg0.getString("TIME_ZONE"),
						arg0.getString("USER_EMAIL"),
						arg0.getString("EVENT_TYPE"),
						arg0.getString("EVENT_DESC"),
						arg0.getString("IP_ADDRESS"),
						arg0.getString("DATA_SERVER"),
						arg0.getString("DISTRIB_SERVER"),
						arg0.getString("EVENT_INFO"),
						arg0.getString("ERROR_STACK_TRACE"),
						arg0.getString("CONFIGURATION_SERVER")
				};
			}
			
		});
		return result;
	}
    
    public List<LogEvent> listLogEvents(String where) {
    	List<LogEvent> result = new ArrayList<LogEvent>();
		String sql = "SELECT * FROM SYNC_MOBILE_LOG";
		result = jdbcTemplate.query(sql, new RowMapper<LogEvent>() {
			@Override
			public LogEvent mapRow(ResultSet arg0, int arg1) throws SQLException {
				return new LogEvent(arg0);
			}
			
		});
		return result;
	}

    public List<String> getAllDeviceModels() {
    	return jdbcTemplate.queryForList("SELECT DISTINCT DEVICE_MODEL FROM SYNC_MOBILE_LOG", String.class);
    }
    
    public List<String> getAllEmails() {
    	return jdbcTemplate.queryForList("SELECT DISTINCT USER_EMAIL FROM SYNC_MOBILE_MODE", String.class);
    }
    
    public void clearLogs(String email) {
    	jdbcTemplate.execute("DELETE FROM SYNC_MOBILE_LOG WHERE lower(USER_EMAIL)=lower('"+email+"')");
    }
    
    public int getLogsCount(String where, Object[] args) {
    	return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYNC_MOBILE_LOG "+where,args);
    }
}
