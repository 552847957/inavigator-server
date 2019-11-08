package ru.sberbank.syncserver2.service.log;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.sberbank.syncserver2.service.monitor.check.ICheckResult;
import ru.sberbank.syncserver2.service.monitor.check.LDAPAvailabilityCheck;
import ru.sberbank.syncserver2.service.monitor.check.OnlineInfrastructureCheck;
import ru.sberbank.syncserver2.service.monitor.check.PushNotificationChecker;

/**
 * 
 * @author sbt-Shakhov-IN
 * Класс для удобной записи в пользовательский тег лог. Используется для ведения журнала онлайн монитора
 */
public class TagHelper {
	public static final String[] ONLINE_MONITOR_TAGS = new String[] {"ONLINE MONITOR"};
	
	public static final Map<String, String> CODE_TO_TAG;
    static {
        Map<String, String> aMap = new HashMap<String, String>();
        aMap.put(OnlineInfrastructureCheck.CHECK_RESULT_CODE_NO_CERTIFICATE, "all");
        aMap.put(OnlineInfrastructureCheck.CHECK_RESULT_CODE_FOR_SYNCSERVER, "syncserver");
        aMap.put(OnlineInfrastructureCheck.CHECK_RESULT_CODE_FOR_DP, "datapower");
        aMap.put(OnlineInfrastructureCheck.CHECK_RESULT_CODE_FOR_PROXY, "proxyserver");
        aMap.put(LDAPAvailabilityCheck.CHECK_RESULT_CODE_LDAP_SERVICE_IS_NULL, "ldap");
        aMap.put(LDAPAvailabilityCheck.CHECK_RESULT_CODE_REQUEST_TO_LDAP_WAS_FAILED, "ldap");
        aMap.put(PushNotificationChecker.CHECK_RESULT_CODE_APPLE_SERVER_IS_NOT_AVAILABLE, "pns");
        CODE_TO_TAG = Collections.unmodifiableMap(aMap);
    }
    
    public static String getTagByCheckCodeForOnlineMonitor(String checkCode) {
    	String tag = CODE_TO_TAG.get(checkCode);
		if (tag == null && checkCode.startsWith(OnlineInfrastructureCheck.CHECK_RESULT_CODE_FOR_MSSQL)) {
			tag = checkCode.substring(OnlineInfrastructureCheck.CHECK_RESULT_CODE_FOR_MSSQL.length());
		}
		return tag;
    }
	
	public static void writeToTagLogger(LogAction msg, String... tags) {
		TagBuffers.log(tags , msg);
	}
	
	public static void writeToTagLogger(String source, String msg, String... tags) {
		TagBuffers.log(tags , new LogAction(new Date(), source, msg));
	}
	
	public static void writeToTagLogger(Date date, String source, String msg, String... tags) {
		TagBuffers.log(tags , new LogAction(date, source, msg));
	}
	
	/**
	 * метод для записи результатов проверки в журнал онлайн мониторинга
	 * @param results
	 */
	public static void writeToOnlineMonitorTag(List<? extends ICheckResult> results) {
		for (ICheckResult result: results)  {
			writeToTagLogger(getTagByCheckCodeForOnlineMonitor(result.getCode()), result.getErrorMessage(), ONLINE_MONITOR_TAGS);
		}
	}

}
