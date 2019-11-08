package ru.sberbank.syncserver2.service.core;

public enum StandartBeanCodes {
	/**
	 * Код бина отвечающего за обработку системных событий
	 */
	SYSTEM_EVENT_HANDLER("misEventHandler");
	
	private String beanCode;
	
	private StandartBeanCodes(String beanCode) {
		this.beanCode = beanCode;
	}

	public String getBeanCode() {
		return beanCode;
	}

	public void setBeanCode(String beanCode) {
		this.beanCode = beanCode;
	}
	
	
	
}
