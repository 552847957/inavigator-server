package ru.sberbank.syncserver2.service.pub.xml;

import javax.xml.bind.annotation.XmlRootElement;

import ru.sberbank.syncserver2.service.pushnotifications.model.OperationSystemTypes;

@XmlRootElement(name="registerClientTokenRequest")
public class RegisterClientTokenRequest {
	
	private String userEmail;

	private OperationSystemTypes osCode;

	private String userDeviceName;

	private String userDeviceCode;
	
	private String userApplicationCode;

	private String userApplicationVersion;
	
	private String userToken;

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public OperationSystemTypes getOsCode() {
		return osCode;
	}

	public void setOsCode(OperationSystemTypes osCode) {
		this.osCode = osCode;
	}

	public String getUserDeviceName() {
		return userDeviceName;
	}

	public void setUserDeviceName(String userDeviceName) {
		this.userDeviceName = userDeviceName;
	}

	public String getUserApplicationCode() {
		return userApplicationCode;
	}

	public void setUserApplicationCode(String userApplicationCode) {
		this.userApplicationCode = userApplicationCode;
	}

	public String getUserApplicationVersion() {
		return userApplicationVersion;
	}

	public void setUserApplicationVersion(String userApplicationVersion) {
		this.userApplicationVersion = userApplicationVersion;
	}

	public String getUserToken() {
		return userToken;
	}

	public void setUserToken(String userToken) {
		this.userToken = userToken;
	}

	public String getUserDeviceCode() {
		return userDeviceCode;
	}

	public void setUserDeviceCode(String userDeviceCode) {
		this.userDeviceCode = userDeviceCode;
	}


	
	
}
