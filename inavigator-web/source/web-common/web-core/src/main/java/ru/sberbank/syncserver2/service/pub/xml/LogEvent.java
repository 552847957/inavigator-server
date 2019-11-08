package ru.sberbank.syncserver2.service.pub.xml;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlRootElement;

import ru.sberbank.syncserver2.gui.data.SQLDescriptor;

@XmlRootElement(name = "logEvent")
public class LogEvent {
	
	private EventInfo eventInfo;
	
	private DeviceInfo deviceInfo;

	public EventInfo getEventInfo() {
		return eventInfo;
	}

	public void setEventInfo(EventInfo eventInfo) {
		this.eventInfo = eventInfo;
	}

	public DeviceInfo getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(DeviceInfo deviceInfo) {
		this.deviceInfo = deviceInfo;
	}
	
	public LogEvent() {
	}
	
	public LogEvent(ResultSet rs) throws SQLException {
		eventInfo = new EventInfo();
		deviceInfo = new DeviceInfo();
		this.deviceInfo.setDeviceInfoSourceId(rs.getString("DEVICE_INFO_SOURCE_ID"));
		this.deviceInfo.setDeviceModel(rs.getString("DEVICE_MODEL"));
		this.deviceInfo.setiOsVersion(rs.getString("IOS_VERSION"));
		this.deviceInfo.setAppVersion(rs.getString("APP_VERSION"));
		this.deviceInfo.setBundleId(rs.getString("BUNDLE_ID"));
		this.deviceInfo.setUpdateTime(rs.getString("UPDATE_TIME"));
		
		this.eventInfo.setEventSourceId(rs.getString("EVENT_SOURCE_ID"));
		this.eventInfo.setEventTime(rs.getString("EVENT_TIME"));
		this.eventInfo.setTimeZone(rs.getString("TIME_ZONE"));
		this.eventInfo.setUserEmail(rs.getString("USER_EMAIL"));
		this.eventInfo.setEventType(rs.getString("EVENT_TYPE"));
		this.eventInfo.setEventDesc(rs.getString("EVENT_DESC"));
		this.eventInfo.setIpAddress(rs.getString("IP_ADDRESS"));
		this.eventInfo.setDataServer(rs.getString("DATA_SERVER"));
		this.eventInfo.setDistribServer(rs.getString("DISTRIB_SERVER"));
		this.eventInfo.setEventInfo(rs.getString("EVENT_INFO"));
		this.eventInfo.setErrorStackTrace(rs.getString("ERROR_STACK_TRACE"));
		this.eventInfo.setConfigurationServer(rs.getString("CONFIGURATION_SERVER"));
	}
	
	@XmlTransient
	static public SQLDescriptor descriptor = new LogSQLDescriptor();
	
	private static class LogSQLDescriptor implements SQLDescriptor {

		@Override
		public String composeSQL(Object o, int queryType) {
			switch(queryType){
            case SQLDescriptor.SQL_SELECT :
                return "SELECT * FROM SYNC_MOBILE_LOG";
            case SQLDescriptor.SQL_GET_CURRVAL :
                return null;
            default :
                throw new IllegalArgumentException("Unexpected query type - "+queryType);
        }
		}

		@Override
		public String composePrepareSQL(int queryType) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void setParameters(Object o, PreparedStatement st, int queryType)
				throws SQLException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public LogEvent newInstance(ResultSet rs) throws SQLException {
			return new LogEvent(rs);
		}
		
	}
	
	
}
