package ru.sberbank.syncserver2.gui.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class AuditRecord {
	protected int eventId;
	protected Date date;	
	protected String email;
	protected String module;
	protected String eventType;
	protected String description;
	protected String host;
		
	public AuditRecord() {
		super();
	}
	
	public AuditRecord(Date date, String email,
			String module, String eventType, String description) {
		super();
		this.date = date;
		this.email = email;
		this.module = module;
		this.eventType = eventType;
		this.description = description;
	}

	public AuditRecord(ResultSet rs) throws SQLException {
		this.eventId = rs.getInt("EVENT_ID");
		this.date = rs.getTimestamp("EVENT_TIME");
		this.email = rs.getString("USER_EMAIL");
		this.module = rs.getString("MODULE");
		this.eventType = rs.getString("EVENT_TYPE");
		this.description = rs.getString("EVENT_DESC");
		this.host = rs.getString("HOST");
	}
	public int getEventId() {
		return eventId;
	}
	
	public Date getDate() {
		return date;
	}

	public String getEmail() {
		return email;
	}

	public String getModule() {
		return module;
	}

	public String getEventType() {
		return eventType;
	}
	
	public String getDescription() {
		//возвращаем только первые 40 символов
		String descr;	
		if (description.length()>42) {
			descr = description.substring(0, 40)+"...";
		} else
			descr = description;
		return descr;		
	}
	
	public void setDescription(String s) {
		this.description = s;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	static SQLDescriptor descriptor = new AuditSQLDescriptor();
	
	private static class AuditSQLDescriptor implements SQLDescriptor{

		@Override
		public String composeSQL(Object o, int queryType) {
			switch (queryType) {
			case SQLDescriptor.SQL_SELECT :
                return "SELECT EVENT_ID, EVENT_TIME, HOST, USER_EMAIL, MODULE, EVENT_TYPE, EVENT_DESC FROM SYNC_AUDIT";
			case SQLDescriptor.SQL_GET_CURRVAL:
				return null;
			default :
                throw new IllegalArgumentException("Unexpected query type - "+queryType);
			}
		}

		@Override
		public String composePrepareSQL(int queryType) {
			return null;
		}

		@Override
		public void setParameters(Object o, PreparedStatement st, int queryType)
				throws SQLException {
						
		}

		@Override
		public Object newInstance(ResultSet rs) throws SQLException {			
			return new AuditRecord(rs);
		}
		
	}	
	

}
