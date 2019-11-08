package ru.sberbank.syncserver2.service.pub.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="responseStatus")
public class ResponseStatus {
	
	/**
	 * Код ошибки
	 */
	private String errorCode;
	
	/**
	 * Сообщение об ошибке
	 */
	private String errorText;

	@XmlAttribute
	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	@XmlAttribute
	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}


	
	
}
