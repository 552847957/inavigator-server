package ru.sberbank.syncserver2.service.pub.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="doAuthentication")
public class Authentication extends Message {
	private String login;
	private String password;
	public Authentication(String login, String password) {
		super();
		this.login = login;
		this.password = password;
	}
	public Authentication() {
	}
	@XmlElement
	public String getLogin() {
		return login;
	}
	public void setLogin(String login) {
		this.login = login;
	}
	@XmlElement
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	

}
