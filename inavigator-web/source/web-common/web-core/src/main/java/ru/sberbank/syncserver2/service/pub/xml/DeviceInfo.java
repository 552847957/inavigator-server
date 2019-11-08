package ru.sberbank.syncserver2.service.pub.xml;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "deviceInfo")
public class DeviceInfo {

	private String deviceInfoSourceId;
	private String deviceModel;
	private String iOsVersion;
	private String appVersion;
	private String bundleId;
	private String updateTime;
	public String getDeviceInfoSourceId() {
		return deviceInfoSourceId;
	}
	public void setDeviceInfoSourceId(String deviceInfoSourceId) {
		this.deviceInfoSourceId = deviceInfoSourceId;
	}
	public String getDeviceModel() {
		return deviceModel;
	}
	public void setDeviceModel(String deviceModel) {
		this.deviceModel = deviceModel;
	}
	public String getiOsVersion() {
		return iOsVersion;
	}
	public void setiOsVersion(String iOsVersion) {
		this.iOsVersion = iOsVersion;
	}
	public String getAppVersion() {
		return appVersion;
	}
	public void setAppVersion(String appVersion) {
		this.appVersion = appVersion;
	}
	public String getBundleId() {
		return bundleId;
	}
	public void setBundleId(String bundleId) {
		this.bundleId = bundleId;
	}
	public String getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}
	
	
}
