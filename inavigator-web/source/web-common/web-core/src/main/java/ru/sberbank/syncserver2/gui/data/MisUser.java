package ru.sberbank.syncserver2.gui.data;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect
public class MisUser {
	
	private String userName;
	
	private String ip;
	
	private Long userId;
	
	private String terrbankShortName;
	
	private String businessUnitId;
	
	private String email;
	
	private String position;
	
	private String terrbankId;
	
	private String businessBlockId;
	
	private String comment;
	
	private String createDate;
	
	private String emailAD;
	
	private List<Long> roles;
	
	private boolean hasMdmRights;
	
	public boolean isHasMdmRights() {
		return hasMdmRights;
	}

	public void setHasMdmRights(boolean hasMdmRights) {
		this.hasMdmRights = hasMdmRights;
	}

	public List<Long> getRoles() {
		return roles;
	}

	public void setRoles(List<Long> roles) {
		this.roles = roles;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getTerrbankShortName() {
		return terrbankShortName;
	}

	public void setTerrbankShortName(String terrbankShortName) {
		this.terrbankShortName = terrbankShortName;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getBusinessUnitId() {
		return businessUnitId;
	}

	public void setBusinessUnitId(String businessUnitId) {
		this.businessUnitId = businessUnitId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public String getTerrbankId() {
		return terrbankId;
	}

	public void setTerrbankId(String terrbankId) {
		this.terrbankId = terrbankId;
	}

	public String getBusinessBlockId() {
		return businessBlockId;
	}

	public void setBusinessBlockId(String businessBlockId) {
		this.businessBlockId = businessBlockId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getEmailAD() {
		return emailAD;
	}

	public void setEmailAD(String emailAD) {
		this.emailAD = emailAD;
	}
	
	
}
