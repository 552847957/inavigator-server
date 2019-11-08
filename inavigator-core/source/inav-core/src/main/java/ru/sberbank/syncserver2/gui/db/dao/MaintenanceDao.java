package ru.sberbank.syncserver2.gui.db.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceDao extends BaseDao {
	
	public Object[] getMaintenanceStatus() {
		return jdbcTemplate.query("select top 1 * from SYNC_MAINTENANCE", new ResultSetExtractor<Object[]>() {

			@Override
			public Object[] extractData(ResultSet rs) throws SQLException, DataAccessException {
				if (rs.next())
					return new Object[] {rs.getBoolean("ACTIVE"), rs.getNString("MESSAGE_TEXT"), rs.getString("EMAILS"),  rs.getTimestamp("DATE")};
				else
					return new Object[] {false};
			}
			
		});
	}
	
	public void updateMaintenanceStatus(Boolean active, String msg, String emails) {
		jdbcTemplate.execute("exec UPDATE_MAINTENANCE " + (active ? 1 : 0) + ", '" + msg + "', '" + emails + "'");
	}
}
