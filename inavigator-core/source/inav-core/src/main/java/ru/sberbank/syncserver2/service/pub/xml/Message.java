package ru.sberbank.syncserver2.service.pub.xml;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;


public class Message {
	public enum Status {
		OK,
		FAILED,
		FORBIDDEN,
		UNAUTHORIZED,
		BAD_REQUEST
	}
	protected String noteMessage;
	protected Status code;
	@XmlElement
	public Status getCode() {
		return code;
	}
	public void setCode(Status code) {
		this.code = code;
	}
	@XmlElement
	public String getNoteMessage() {
		return noteMessage;
	}
	public void setNoteMessage(String noteMessage) {
		this.noteMessage = noteMessage;
	}	
}
