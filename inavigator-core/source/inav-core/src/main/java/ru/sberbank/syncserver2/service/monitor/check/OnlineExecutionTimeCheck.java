package ru.sberbank.syncserver2.service.monitor.check;

import ru.sberbank.syncserver2.service.monitor.data.OnlineRequestSettingsLoader;
import ru.sberbank.syncserver2.service.monitor.data.RequestInfo;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;

public class OnlineExecutionTimeCheck extends OnlineRequestCheck {
	private String settingsFile;
	private String hosts;
	private volatile List<RequestInfoWithMaxTime> requests = new ArrayList<RequestInfoWithMaxTime>();
	public static final String CHECK_RESULT_CODE_NO_SETTINGS_FILE = "SETTINGS_FILE_NOT_LOAD";
	public static final String CHECK_RESULT_CODE_REQUEST_ERROR = "_REQUEST_ERROR_";
	public static final String CHECK_RESULT_CODE_TAKES_MUCH_TIME = "_REQUEST_TAKES_MUCH_TIME_";
	private long timeInterval = 5*60*1000; //время между успешными проверками запроса

	{
		setDefaultDoNotNotifyCount(2);
	}

	@Override
	protected void init() {
		List<RequestInfoWithMaxTime> local = null;
		try {
			local = new SettingsLoader(settingsFile).load();
			clearAllCheckResults();
			tagLogger.log("Из файла "+settingsFile+" загружены "+local.size()+" запросов.");
		} catch (Exception e) {
			logger.error(e, e);
			tagLogger.log("Не удалось загрузить настройки из файла "+settingsFile+": "+e.toString());
		}
		this.requests = local;
	}

	@Override
	protected List<CheckResult> doInternalCheck() {
		List<CheckResult> result = new ArrayList<CheckResult>();
		if (requests==null) {
			result.add(new CheckResult(CHECK_RESULT_CODE_NO_SETTINGS_FILE, false, LOCAL_HOST_NAME+" says: Can't read checker settings from "+settingsFile));
			writeFailedToLog(CHECK_RESULT_CODE_NO_SETTINGS_FILE, "Не удалось загрузить запросы из файла "+settingsFile);
			return result;
		} else {
			if (addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_NO_SETTINGS_FILE, LOCAL_HOST_NAME+" says: Checker settings have been successfully read from "+settingsFile)) {
				tagLogger.log("Запросы успешно загружены");
			}
		}

		for (String host: hosts.split(";")) {
			doChecksForHost(host.trim(), result);
		}
		return result;
	}

	private void doChecksForHost(String host, List<CheckResult> result) {
		for (RequestInfoWithMaxTime info: requests) {
			if (info.shouldBeExecutedNowForHost(host)) {
				String error = null;
				try {
					long time = System.currentTimeMillis();
					DataResponse response = doRequest(host,info.getRequest());
					if (response.getResult() == Result.OK) {
						time = System.currentTimeMillis() - time;

						if (addSuccessfullCheckResultIfPreviousFailed(result, host+CHECK_RESULT_CODE_REQUEST_ERROR + info.getName(), LOCAL_HOST_NAME+" says: request "+info.getName()+" to host "+host+" was successfully completed")) {
							tagLogger.log("Запрос "+info.getName()+" на "+host+" выполнился успешно");
						}

						if (time>info.maxTime) {
							result.add(new CheckResult(host+CHECK_RESULT_CODE_TAKES_MUCH_TIME + info.getName(), false, LOCAL_HOST_NAME+" says: Request "+info.getName()+" to host "+host+" takes "+time+" ms and it is more than "+info.maxTime));
							writeFailedToLog(host+CHECK_RESULT_CODE_TAKES_MUCH_TIME + info.getName(), "Запрос "+info.getName()+" на "+host+" занял "+time+" мс и это больше, чем "+info.maxTime);
						} else {
							if (addSuccessfullCheckResultIfPreviousFailed(result, host+CHECK_RESULT_CODE_TAKES_MUCH_TIME + info.getName(), LOCAL_HOST_NAME+" says: Request "+info.getName()+" to host "+host+" takes less than "+info.maxTime+" ms"))
								tagLogger.log("Запрос "+info.getName()+" на "+host+" занял "+time+" мс и это меньше, чем "+info.maxTime);
							info.setNextExecuteAfterForHost(host, new Date(System.currentTimeMillis()+timeInterval));
						}

						continue;

					} else {
						error = response.getError();
						writeFailedToLog(host+CHECK_RESULT_CODE_REQUEST_ERROR+ info.getName(), "Запрос "+info.getName()+" на "+host+" завершился ошибкой: "+error);
					}
				} catch (Exception e) {
					error = e.toString();
					writeFailedToLog(host+CHECK_RESULT_CODE_REQUEST_ERROR+ info.getName(), "Запрос "+info.getName()+" на "+host+" завершился ошибкой: "+error, e);
				}
				result.add(new CheckResult(host+CHECK_RESULT_CODE_REQUEST_ERROR+info.getName(), false,  LOCAL_HOST_NAME+" says: Error while performing request "+info.getName()+" to "+host+": "+error));
			}
		}
	}

	public String getSettingsFile() {
		return settingsFile;
	}

	public void setSettingsFile(String requestsFile) {
		this.settingsFile = requestsFile;
	}

	public String getHosts() {
		return hosts;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts;
	}

	public long getTimeInterval() {
		return timeInterval;
	}

	public void setTimeInterval(String timeInterval) {
		try {
			this.timeInterval = Long.parseLong(timeInterval);
		} catch (Throwable th) {
			tagLogger.log("Не удалось привести занчение timeInterval "+timeInterval+" к целому. Будет использоваться значение по умолчанию "+this.timeInterval);
		}
	}

	private class RequestInfoWithMaxTime extends RequestInfo {
		public final Long maxTime;
		public RequestInfoWithMaxTime(String name, String request, Long maxTime) {
			super(name, request);
			this.maxTime = maxTime;
		}
		private Map<String,Date> executionDates = new HashMap<String, Date>();

		public boolean shouldBeExecutedNowForHost(String host) {
			Date date = executionDates.get(host.toUpperCase());
			return date==null || date.before(new Date());
		}

		public void setNextExecuteAfterForHost(String host, Date date) {
			executionDates.put(host.toUpperCase(), date);
		}
	}

	private class SettingsLoader extends OnlineRequestSettingsLoader<RequestInfoWithMaxTime> {
		private Set<String> names = new HashSet<String>();

		public SettingsLoader(String workingFile) {
			super("/queries.txt", workingFile);
		}

		@Override
		protected RequestInfoWithMaxTime getRequestInfo(BufferedReader reader)
				throws IOException {
			StringBuilder query  = new StringBuilder();
			String name = reader.readLine();
			String time = reader.readLine();
			String temp = reader.readLine();
			while (temp != null && !temp.startsWith("---")) {
				query.append(temp);
				temp = reader.readLine();
			}
			if (name==null || time==null || query.length()==0)
				return null;
			name = name.trim();
			if (!names.add(name.toLowerCase())) {
				tagLogger.log("Найдено несколько запрсов с именем "+name+". Загрузится только один запрос с таким именем.");
				return null;
			}
			String queryStr = replaceParameters(query.toString());
			if (queryStr == null)
				return null;
			return new RequestInfoWithMaxTime(name, queryStr, Long.parseLong(time));
		}

	}

	@Override
	public String getDescription() {
		return "Чекер для проверки максимального времени выполнения онлайн запросов";
	}

}
