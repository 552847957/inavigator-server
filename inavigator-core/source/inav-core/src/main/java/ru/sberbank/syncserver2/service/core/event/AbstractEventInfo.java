package ru.sberbank.syncserver2.service.core.event;

/**
 * Базовый класс для всех сообщений системы
 * @author sbt-gordienko-mv
 *
 */
public class AbstractEventInfo {
	/**
	 * Код события 
	 */
	private String code;
	
	/**
	 * Имя события
	 */
	private String name;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
	
}
