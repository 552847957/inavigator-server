package ru.sberbank.syncserver2.service.sql;

import org.bouncycastle.util.Arrays;

import ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger;

public class RequestExecutionTimer {
	private long times[];
	private int maxSize = 100;
	private int size = 0;
	private int index = 0;
	private long sumTime = 0;
	private long maxAverage = 60000;
	private boolean previousCheck = false; //чтобы избежать уведомления при запуске сервиса, когда первые запросы могут заметно превышать среднее время запроса
	private DataPowerNotificationLogger dataPowerLogger;
	
	public RequestExecutionTimer(int maxSize, long maxAverage, DataPowerNotificationLogger dataPowerLogger) {
		if (maxSize>0) 
			this.maxSize = maxSize;
		times = new long [this.maxSize];
		this.dataPowerLogger = dataPowerLogger;
		this.maxAverage = maxAverage*1000; //т.к. пришло значение в сек
	}	
	
	public synchronized void addRequestTime(long time) {
//		sumTime = sumTime - times[index] + time;
//		times[index++] = time;
//		index = index % maxSize;
//		size = Math.min(size+1, maxSize);
//		boolean check = getAverageTime()<maxAverage;
//		
//		if (!check && previousCheck) {
//			previousCheck = false;
//			this.notify("Average online SQL request(syncserver) time is "+getAverageTime()+" and it is more than "+maxAverage);
//		}				
//		if (check && !previousCheck)
//			previousCheck = check;
	}	
	
	private long getAverageTime() {		
			return sumTime/size;
	}	
	
	private void notify(String text) {
        if(dataPowerLogger!=null){
        	dataPowerLogger.addError(text);
        }
    }

}
