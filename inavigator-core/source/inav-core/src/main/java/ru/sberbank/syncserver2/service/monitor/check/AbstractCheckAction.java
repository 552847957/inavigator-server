package ru.sberbank.syncserver2.service.monitor.check;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import ru.sberbank.syncserver2.service.core.AbstractService;

/**
 * Created by sbt-kozhinsky-lb on 26.04.14.
 */
public abstract class AbstractCheckAction extends AbstractService implements CheckAction {		
	
	/**
	 * Карта результатов последних проверок для данного action
	 * Пример корректной работы с lastCheckResultMap см. в ru.sberbank.syncserver2.service.monitor.check.TestCheck
	 *  
	 */
	private ConcurrentMap<String, CheckResultWrapper> lastCheckResultMap;

    private String hostnames;
    
    // обычное количество игнорирования неуспешных проверок
    // переопределяется в наследниках, в коде остается константой
    private int defaultDoNotNotifyCount = 0;
    
    private volatile long lastUpdate = 0L;
    
    @Override
    public Long getLastUpdate() {
    	return lastUpdate;
    }

    protected AbstractCheckAction() {
    	lastCheckResultMap = new ConcurrentHashMap<String, CheckResultWrapper>();
    }
    
    public ICheckResult getLastCheckResult(ICheckResult newResult) {
    	return getLastCheckResult(newResult.getCode());
    }

    public ICheckResult getLastCheckResult(String code) {
    	return lastCheckResultMap.get(code);
    }
    
    public ICheckResult getRealLastCheckResult(String code) {
    	CheckResultWrapper result = lastCheckResultMap.get(code);
    	return result == null ? null : result.getRealResult();
    }
    
    public boolean isLastFailed(String code) {
    	CheckResultWrapper result = lastCheckResultMap.get(code);
    	return result == null ? false : !result.getRealResult().isPassed();
    }
    
    /**
     * метод для записи в теглог, исключающий дублирование сообщения
     */
    public void writeFailedToLog(String code, String msg, Throwable th) {
    	writeFailedToLog(code, msg);
    	if (th != null) {
    		logger.error(msg, th);
    	}
    }
    
    /**
     * метод для записи в теглог, исключающий дублирование сообщения
     */
    public void writeFailedToLog(String code, String msg) {
    	if (!isLastFailed(code)) {
    		tagLogger.log(msg);
    	}
    }
    
    /**
     * метод для записи в теглог, исключающий дублирование сообщения
     */
    public void writeSuccessToLog(String code, String msg) {
    	if (isLastFailed(code)) {
    		tagLogger.log(msg);
    	}
    }
    
    public String getName() {
    	return serviceBeanCode;
    }
    
    /**
     * Добавить успешную проверку в список. в случае если последняя проверка с данным кодом уже была успешной, 
     * то добавление не произойдет
     * @param code
     * @return true - if was added, false - otherwise
     */
    protected boolean addSuccessfullCheckResultIfPreviousFailed(List<CheckResult> outputList,String code,String message) {
    	CheckResultWrapper previousResult =  lastCheckResultMap.get(code);
    	if (previousResult != null) {
    		if (!previousResult.getRealResult().isPassed()) {
    			// добавляем результат в outputList, но не трогаем внутреннее состояние 
    			// (т.к. этот результат придет в setNextCheckResult() из MonitorService)
    			outputList.add(new CheckResult(code, true, message));
    			return true;
    		}
    	}
    	return false;
    	
    }

    public void clearAllCheckResults() {
    	lastCheckResultMap.clear();
    	lastUpdate = 0L;
    }
    
    public ICheckResult getLastCheckResult() {
    	return lastCheckResultMap.get(ICheckResult.DEFAULT_CHECK_CODE);
    }
    
    /**
     * Обновить состояние чекера и вернуть необходимость в уведомлении и результат проверки (сообщение с подсказкой)
     */
    public Notification setNextCheckResult(ICheckResult nextResult) {
    	// опускаем проверку на null, т.к. эта проверка либо будет сделана в MonitorService, либо там вылетет NullPointerException
    	CheckResultWrapper result =  lastCheckResultMap.get(nextResult.getCode());

    	if (result == null) {
    		//просто запоминаем последний результат
    		result = new CheckResultWrapper(nextResult);
    		CheckResultWrapper old = lastCheckResultMap.putIfAbsent(nextResult.getCode(), result);
    		
    		if (old != null)		// кто-то успел положить результат? тогда надо это учесть
    			return new Notification(old.setCheckResult(nextResult), old);
    		else 
    			return new Notification(!result.isPassed(), result);
    	} else {
    		// запоминаем и обновляем счетчик
    		return new Notification(result.setCheckResult(nextResult), result);
    	}
    }    

    /**
     * Метод проверки, который должен быть реализован в классе-наследнике
     * @return
     */
    abstract protected List<? extends ICheckResult> doCheck();  

    @Override
	public List<? extends ICheckResult> check() {
    	List<? extends ICheckResult> result = null;
    	//0. Check if this server should monitor
    	if(!isServerMonitored()){    		
    		result = Arrays.asList(new CheckResult(true,LOCAL_HOST_NAME+" says at "+new Date()+": check is switched off"));
    	} else {
    		result = doCheck();
    	}
    	lastUpdate = System.currentTimeMillis();
    	return result;
	}

	@Override
    protected void doStop() {
    }

    @Override
    protected void waitUntilStopped() {
    }
    
    /**
     * выставить количество пропусков непрошедших проверок
     * метод может быть вызван в наследнике либо в конструкторе, либо до него
     * 
     * если поставить Integer.MAX_VALUE, то уведомления будут пропускаться всегда
     * (ну или пока счетчик не уменьшится до 0)
     * @param defaultDoNotNotifyCount
     */
    protected void setDefaultDoNotNotifyCount(int defaultDoNotNotifyCount) {
		this.defaultDoNotNotifyCount = defaultDoNotNotifyCount;
	}
    
    /**
     * Находится ли данный хост в списке разрешенных хостов для данного Check-а
     * @return
     */
	public boolean isServerMonitored(){
        String[] hosts = (hostnames == null || "".equals(hostnames))?null:hostnames.trim().split(";");
        
        // если список хостов не задан, то мониторим
        if (hosts == null)
        	return true;
        
        for (int i = 0; i < hosts.length; i++) {
            if("localhost".equalsIgnoreCase(hosts[i]) || hosts[i].equalsIgnoreCase(LOCAL_HOST_NAME)){
                return true;
            }
        }
		return false;
	}
	
	public String getHostnames() {
		return hostnames;
	}

	public void setHostnames(String hostnames) {
		this.hostnames = hostnames;
	}
	
	public List<ICheckResult> getFailedCheckResult() {
		List<ICheckResult> result = new ArrayList<ICheckResult>();
		for (CheckResultWrapper resultWrapper: lastCheckResultMap.values()) {
			ICheckResult checkResult = resultWrapper.getRealResult();
			if (!checkResult.isPassed())
				result.add(checkResult);
		}
		return result;
	}
	
	public List<ICheckResult> getAllCheckResults() {		
		List<ICheckResult> localList = new ArrayList<ICheckResult>();		
		for (CheckResultWrapper wrapper: lastCheckResultMap.values()) {
			localList.add(wrapper);
		}
		return localList;
	}

	protected void composeAndAddResultWithLog(List<CheckResult> result, boolean passed, String code, String errMessage, String okMessage) {
		if (passed) {
			if (addSuccessfullCheckResultIfPreviousFailed(result, code, LOCAL_HOST_NAME+" says: "+okMessage))
				tagLogger.log(okMessage);
		} else {
			result.add(new CheckResult(code, false, LOCAL_HOST_NAME+" says: "+errMessage));
			if (!isLastFailed(code))
				tagLogger.log(errMessage);
		}
	}
	
	/**
	 * класс обертка для результата проверки с счетчиком для
	 * устранения кратковременных сбоев 
	 *
	 */
	public class CheckResultWrapper implements ICheckResult {
		private volatile ICheckResult checkResult;
		private volatile int doNotNotifyCount;
		
		public CheckResultWrapper(ICheckResult checkResult, int doNotNotifyCount) {
			this.checkResult = checkResult;
			this.doNotNotifyCount = doNotNotifyCount;
		}
		
		public CheckResultWrapper(ICheckResult checkResult) {
			this(checkResult, checkResult.isPassed() ? defaultDoNotNotifyCount : defaultDoNotNotifyCount - 1);
		}

		@Override
		public boolean isPassed() {
			// если счетчик > 0, то результат всегда будет true
			return checkResult.isPassed() || doNotNotifyCount >= 0;
		}

		@Override
		public String getErrorMessage() {
			return checkResult.getErrorMessage();
		}
		
		/**
		 * метод получения реального результата, минуя механизм
		 * исключения кратковременных сбоев
		 * @return
		 */
		public ICheckResult getRealResult() {
			return checkResult;
		}

		@Override
		public String getCode() {
			return checkResult.getCode();
		}
		
		/**
		 * Атомарно запомнить результат и обновить счетчик в соответствии с ним
		 * @param checkResult
		 * @return true - if status have changed
		 */
		private synchronized boolean setCheckResult(ICheckResult checkResult) {
			boolean lastState = isPassed(); // запоминаем состояние

			// обновляем счетчик: на дефолтный - если проверка успешная, иначе вычитаем 1
			doNotNotifyCount = checkResult.isPassed() ? defaultDoNotNotifyCount : doNotNotifyCount - 1;
			
			this.checkResult = checkResult;
			
			return lastState ^ isPassed();	// XOR операция, изменилось ли состояние?
		}

		@Override
		public int hashCode() {
			return checkResult.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return checkResult.equals(obj);
		}
		
		@Override
		public String toString() {
			return checkResult.toString()+getNotifyCountMsg();
		}
		
		/**
		 * показать счетчик, если необходимо
		 * @return
		 */
		private String getNotifyCountMsg() {			
			if (defaultDoNotNotifyCount == Integer.MAX_VALUE) {
				return " (skipped)";
			} else {
				return (checkResult.isPassed() ^ isPassed()) ? 
						" (" + doNotNotifyCount + " times left)" : "";
			}					
		}
	}	
}
