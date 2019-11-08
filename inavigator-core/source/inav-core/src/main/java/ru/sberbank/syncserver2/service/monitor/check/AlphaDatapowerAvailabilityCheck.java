package ru.sberbank.syncserver2.service.monitor.check;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;

import ru.sberbank.syncserver2.service.core.ServiceManager;

public class AlphaDatapowerAvailabilityCheck extends AbstractCheckAction {
	public static final String CHECK_RESULT_CODE_DATAPOWER_IS_NOT_AVAILABLE = "DATAPOWER_IS_NOT_AVAILABLE";
	public static final String CHECK_RESULT_CODE_HOST_DID_NOT_PING = "HOST_DID_NOT_PING_";
	private final String sqlRequest = "SP_GET_HOSTS_DO_NOT_PING ?";
	
	private JdbcTemplate jdbcTemplate;
	private List<String> sigmaHosts = new ArrayList<String>();
	private String timeInterval = "300";	
	
	private List<String> lastDidNotPing = new ArrayList<String>();
	
	@Override
	protected void doStart() {
		DataSource dataSource = ServiceManager.getInstance().getConfigSource();
    	jdbcTemplate = new JdbcTemplate(dataSource);
		super.doStart();		
	}


	@Override
	protected List<CheckResult> doCheck() {
		final List<CheckResult> result = new ArrayList<CheckResult>();			
		// получаем список хостов без пингов
		List<String> listHosts = jdbcTemplate.queryForList(sqlRequest, String.class, timeInterval);
		Set<String> hosts = new HashSet<String>(listHosts.size());
		// загоняем их в сет
		for (String host: listHosts) {
			hosts.add(host.toLowerCase());
		}
		
		// добавляем уведомление на каждый хост из хостов без пингов
		for (String host: listHosts) {
			result.add(new CheckResult(CHECK_RESULT_CODE_HOST_DID_NOT_PING+host, false, LOCAL_HOST_NAME+" says: did not find a ping from "+host));
			writeFailedToLog(CHECK_RESULT_CODE_HOST_DID_NOT_PING+host, "Не найден пиг от хоста "+host);
		}
		
		// проверяем каждый хост из предыдущего запуска (если его нет в новом сете - пинг появился)
		for (String host: lastDidNotPing) {
			if (!hosts.contains(host.toLowerCase())) {
				if (addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_HOST_DID_NOT_PING+host, LOCAL_HOST_NAME+" says: found a ping from "+host))
					tagLogger.log("Найден пинг от хоста "+host);
			}
		}
		
		// запоминаем список хостов
		lastDidNotPing = listHosts;
		
		// ищем в хостах без пинга все сигма хосты
		if (hosts.containsAll(sigmaHosts)) {
			result.add(new CheckResult(CHECK_RESULT_CODE_DATAPOWER_IS_NOT_AVAILABLE, false, LOCAL_HOST_NAME+" says: Datapower is not available."));
			writeFailedToLog(CHECK_RESULT_CODE_DATAPOWER_IS_NOT_AVAILABLE, "Datapower is not available.");					
		} else {
			if (addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_DATAPOWER_IS_NOT_AVAILABLE, LOCAL_HOST_NAME+" says: Datapower is available."))
				tagLogger.log("Datapower is available.");
		}
		
		return result;
	}

	public void setSigmaHosts(String sigmaHosts) {
		this.sigmaHosts.clear();
		for (String s: sigmaHosts.split(";")) {
			this.sigmaHosts.add(s.toLowerCase());
		}
	}
	
	
	public void setTimeInterval(String timeInterval) {
		this.timeInterval = timeInterval;
	}
	
	@Override
	public String getDescription() {
		return "Чекер для проверки пинга хостов и доступности DataPower";
	}
	
	
}
