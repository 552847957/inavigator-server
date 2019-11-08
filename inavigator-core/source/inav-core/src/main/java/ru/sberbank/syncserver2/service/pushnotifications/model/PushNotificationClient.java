package ru.sberbank.syncserver2.service.pushnotifications.model;

/**
 * Модель данных - клиент системы пуш уведомлений
 * @author sbt-gordienko-mv
 *
 */
public class PushNotificationClient {
	
	private Long clientId;
	
	private OperationSystemTypes osCode;
	
	private String applicationCode;
	
	private String applicationVersion;
	
	private String token;
	
	private String email;
	
	private String deviceName;

	private String deviceCode;
	
	private String status;

	public PushNotificationClient() {
		super();
	}
	
	
	public PushNotificationClient(Long clientId, OperationSystemTypes osCode,
			String applicationCode, String applicationVersion, String token) {
		super();
		this.clientId = clientId;
		this.osCode = osCode;
		this.applicationCode = applicationCode;
		this.applicationVersion = applicationVersion;
		this.token = token;
	}
	public PushNotificationClient(String email, String deviceName, String status) {
		super();
		this.email = email;
		this.deviceName = deviceName;
		this.status = status;
	}
	
	public PushNotificationClient(Long clientId, OperationSystemTypes osCode,
			String applicationCode, String applicationVersion, String token,
			String email, String deviceName, String status) {
		super();
		this.clientId = clientId;
		this.osCode = osCode;
		this.applicationCode = applicationCode;
		this.applicationVersion = applicationVersion;
		this.token = token;
		this.email = email;
		this.deviceName = deviceName;
		this.status = status;
	}


	public Long getClientId() {
		return clientId;
	}
	public void setClientId(Long clientId) {
		this.clientId = clientId;
	}
	public OperationSystemTypes getOsCode() {
		return osCode;
	}
	public void setOsCode(OperationSystemTypes osCode) {
		this.osCode = osCode;
	}
	public String getApplicationCode() {
		return applicationCode;
	}
	public void setApplicationCode(String applicationCode) {
		this.applicationCode = applicationCode;
	}
	public String getApplicationVersion() {
		return applicationVersion;
	}
	public void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getEmail() {
		return email;
	}


	public void setEmail(String email) {
		this.email = email;
	}


	public String getDeviceName() {
		return deviceName;
	}


	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}


	public String getDeviceCode() {
		return deviceCode;
	}


	public void setDeviceCode(String deviceCode) {
		this.deviceCode = deviceCode;
	}

	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}	
	
}
