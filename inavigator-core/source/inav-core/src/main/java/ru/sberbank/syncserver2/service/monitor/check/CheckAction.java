package ru.sberbank.syncserver2.service.monitor.check;

import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 25.04.14.
 */
public interface CheckAction {

    List<? extends ICheckResult> check();
    
    /**
     * Получить последний результат для текущей проверки
     * @param newResult
     * @return
     */
    ICheckResult getLastCheckResult(ICheckResult newResult);
    
    ICheckResult getLastCheckResult(String code);
    
    /**
     * Получить последний результат проверки для дефолтового кода
     * @return
     */
    ICheckResult getLastCheckResult();
    
    /**
     * Запомнить новый результат проверки, вернуть необходимость в уведомлении
     * @param nextCheckResult
     * @return Notification
     */
    Notification setNextCheckResult(ICheckResult nextCheckResult);
    
    /**
     * Метод очищает все результаты прошлых проверок для данного действия 
     */
    void clearAllCheckResults();
    
    /**
     * метод для получения результата всех проверок
     * @return
     */
    List<ICheckResult> getAllCheckResults();
    
    /**
     * Описание сервиса
     * @return
     */
    String getDescription();
    
    /**
     * получить время последнего обновления
     * @return
     */
    Long getLastUpdate();
    
    /**
     * Получить название бина сервиса для данного чекера
     * @return
     */
    String getName();
    
    class Notification {
    	public final boolean notify;
    	public final ICheckResult checkResult;
		public Notification(boolean notify, ICheckResult checkResult) {
			super();
			this.notify = notify;
			this.checkResult = checkResult;
		}   
    }

}
