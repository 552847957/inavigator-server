package ru.sberbank.syncserver2.gui.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;

import ru.sberbank.syncserver2.gui.util.format.JSPFormatPool;

public class CompleteAuditRecord extends AuditRecord implements Iterable<String>{

	protected String ipAddress;
	protected int code;	
		
	public CompleteAuditRecord(Date date, String ipAddress, String email,
			String module, String eventType, String description, int code) {
		super(date, email, module, eventType, description);
		this.ipAddress = ipAddress;
		this.code = code;		
	}

	public CompleteAuditRecord(ResultSet rs) throws SQLException {
		super(rs);
		this.ipAddress = rs.getString("IP_ADDRESS");
		this.code = rs.getInt("CODE");
	}

	@Override
	public String getDescription() {
		return description;
	}	
	

	public int getCode() {
		return code;
	}

	public String getIpAddress() {
		return ipAddress;
	}



	static SQLDescriptor descriptor = new AuditSQLDescriptor();
	
	private static class AuditSQLDescriptor implements SQLDescriptor{

		@Override
		public String composeSQL(Object o, int queryType) {
			switch (queryType) {
			case SQLDescriptor.SQL_SELECT :
                return "SELECT * FROM SYNC_AUDIT";
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
			return new CompleteAuditRecord(rs);
		}
		
	}
		
	@Override
	public Iterator<String> iterator() {
		return new Iterator<String>() {
			private int i = 0;

			@Override
			public boolean hasNext() {				
				return i<7;
			}

			@Override
			public String next() {
				i++;
				switch (i) {
				case 1: return JSPFormatPool.formatDateAndTime(date);
				case 2: return host;
				case 3: return ipAddress;
				case 4: return email;
				case 5: return module;
				case 6: return eventType;
				case 7: return description;
				case 8: return Integer.toString(code);			
				}
				return null;
			}

			@Override
			public void remove() {
			}
			
		};
	}

}
