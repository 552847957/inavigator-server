package ru.sberbank.syncserver2.gui.web.validator;

public class Error {
	
	private String fieldName;
	private String message;
	private boolean  critical;
	
	
	public Error(String fieldName, String message,boolean critical) {
		super();
		this.fieldName = fieldName;
		this.message = message;
		this.critical = critical;
	}
	public String getFieldName() {
		return fieldName;
	}
	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public boolean isCritical() {
		return critical;
	}
	public void setCritical(boolean critical) {
		this.critical = critical;
	}
	
	
}
