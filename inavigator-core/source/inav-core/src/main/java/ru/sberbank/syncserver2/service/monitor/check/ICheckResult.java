package ru.sberbank.syncserver2.service.monitor.check;

public interface ICheckResult {
	
	boolean isPassed();
	
	String getErrorMessage();
	
	String getCode();
	
	/**
	 * Дефолтовый код првоерки (если код не указан явн)
	 */
	static final String DEFAULT_CHECK_CODE = "DEFAULT_CHECK_CODE";

}
