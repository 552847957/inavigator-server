package ru.sberbank.syncserver2.gui.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MobileLogMode {
	private String userEmail;
	private String device;
	private int mode;
	
	public MobileLogMode() {
		this.userEmail = "";
		this.device = "";
		this.mode = 0;
	}
	
	public String getUserEmail() {
		return userEmail;
	}
	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}
	public String getDevice() {
		return device;
	}
	public void setDevice(String device) {
		this.device = device;
	}
	public int getMode() {
		return mode;
	}
	public void setMode(int mode) {
		this.mode = mode;
	}
	public MobileLogMode(ResultSet rs) throws SQLException {
		this.userEmail = rs.getString("USER_EMAIL");
		this.device = rs.getString("DEVICE_MODEL");
		this.mode = rs.getInt("MODE");
	}
	
	@Override
	public String toString() {
		return userEmail+": "+device+" - "+mode;
	}
	
	static SQLDescriptor descriptor = new MobileLogModeSQLDescriptor();
	
	private static class MobileLogModeSQLDescriptor implements SQLDescriptor {

		@Override
		public String composeSQL(Object o, int queryType) {
			switch(queryType){
            case SQLDescriptor.SQL_SELECT :
                return "SELECT USER_EMAIL, DEVICE_MODEL, MODE FROM SYNC_MOBILE_MODE";
            case SQLDescriptor.SQL_GET_CURRVAL :
                return "";
            default :
                throw new IllegalArgumentException("Unexpected query type - "+queryType);
                }			
		}

		@Override
		public String composePrepareSQL(int queryType) {
			switch(queryType){
            case SQLDescriptor.SQL_DELETE  :
                return "DELETE FROM SYNC_MOBILE_MODE WHERE USER_EMAIL = ? AND DEVICE_MODEL = ?";
            default :
                throw new IllegalArgumentException("Unexpected query type - "+queryType);
                }	
		}

		@Override
		public void setParameters(Object o, PreparedStatement st, int queryType)
				throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public Object newInstance(ResultSet rs) throws SQLException {
			return new MobileLogMode(rs);		
		}
		
	}
}
