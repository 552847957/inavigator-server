package ru.sberbank.syncserver2.service.pushnotifications.model;

public class DictionaryItem {
	
	private String value;
	
	private String code;
	
	

	public DictionaryItem() {
		super();
	}

	public DictionaryItem(String value, String code) {
		super();
		this.value = value;
		this.code = code;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	
}
