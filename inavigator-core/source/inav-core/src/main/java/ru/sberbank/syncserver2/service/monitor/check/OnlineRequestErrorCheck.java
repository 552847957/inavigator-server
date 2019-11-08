package ru.sberbank.syncserver2.service.monitor.check;

import ru.sberbank.syncserver2.service.monitor.data.OnlineRequestSettingsLoader;
import ru.sberbank.syncserver2.service.monitor.data.RequestInfo;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.*;

public class OnlineRequestErrorCheck extends OnlineRequestCheck {
	private String settingsFolder;
	private String hosts;
	private volatile List<RequestGroup> requestGroups = new ArrayList<RequestGroup>();
	public static final String CHECK_RESULT_CODE_NO_SETTINGS_FILE = "SETTINGS_FILE_NOT_LOAD";
	public static final String CHECK_RESULT_CODE_REQUEST_ERROR = "REQUEST_ERROR_";
	private long timeInterval = 10*60; //время между успешными проверками запроса
	private String timeIntervalStr;
	private final String[] warFiles = new String[] {"iNavigatorGrants.txt", "PushNotificationGrants.txt"};

	{
		setDefaultDoNotNotifyCount(2);
	}

	public OnlineRequestErrorCheck() {
		super();
	}

	@Override
	protected void init() {
		if (timeIntervalStr != null && !timeIntervalStr.trim().isEmpty()) {
			try {
				this.timeInterval = Long.parseLong(timeIntervalStr);
			} catch (Throwable th) {
				tagLogger.log("Не удалось привести занчение timeInterval "+timeIntervalStr+" к целому. Будет использоваться значение по умолчанию "+this.timeInterval);
			}
		}
		File folder = new File(settingsFolder);
		if (!folder.exists() || !folder.isDirectory()) {
			if (!folder.mkdirs()) {
				tagLogger.log("Директория "+settingsFolder+" не существует и не может быть создана");
				return;
			} else {
				tagLogger.log("Создана директория для запросов "+settingsFolder);
			}
		}
		clearAllCheckResults();

		Set<String> fileNames = new HashSet<String>();
		fileNames.addAll(Arrays.asList(folder.list(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".txt");
			}
		})));

		fileNames.addAll(Arrays.asList(warFiles));

		List<RequestGroup> local = new ArrayList<RequestGroup>();

		for (String groupName: fileNames) {
			String file = "/"+groupName;
			try {
				// файл либо существует, либо должен быть в исходниках
				List<RequestInfo> requests = loadRequests(file, folder+file);
				local.add(new RequestGroup(groupName, requests));
				tagLogger.log("Загружено "+ requests.size() +" запросов для группы "+groupName+" из файла "+folder+file);
			} catch (Exception e) {
				logger.error(e, e);
				tagLogger.log("Не удалось прочитать запросы из файла "+folder+file+": "+e.toString());
			}
		}

		requestGroups = local;
	}

	private List<RequestInfo> loadRequests(String warFile, String workingFile) throws Exception {
		return new SettingsLoader(warFile, workingFile).load();
	}

	@Override
	protected List<CheckResult> doInternalCheck() {
		List<CheckResult> result = new ArrayList<CheckResult>();
		List<RequestGroup> requestGroupsLocal = this.requestGroups;
		if (requestGroupsLocal.size()==0) {
			result.add(new CheckResult(CHECK_RESULT_CODE_NO_SETTINGS_FILE, false, LOCAL_HOST_NAME+" says: Can't read checker settings from "+settingsFolder));
			writeFailedToLog(CHECK_RESULT_CODE_NO_SETTINGS_FILE, "Не удалось загрузить запросы из файла "+settingsFolder);
			return result;
		} else {
			if (addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_NO_SETTINGS_FILE, LOCAL_HOST_NAME+" says: Checker settings have been successfully read from "+settingsFolder)) {
				tagLogger.log("Запросы успешно загружены");
			}
		}

		for (RequestGroup group: requestGroupsLocal) {
			if (!group.shouldBeExecutedNow())
				continue;

			Result errorMsg = doChecksForGroup(group);
			if (errorMsg == null) {
				group.setNextExecuteAfter(new Date(System.currentTimeMillis()+(timeInterval*1000)));

				if (addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_REQUEST_ERROR + group.name, LOCAL_HOST_NAME+" says: all request from group "+group.name+" were successfully completed")) {
					tagLogger.log("Запросы из группы "+group.name+" были успешно выполнены");
				}
			} else {
				result.add(new CheckResult(CHECK_RESULT_CODE_REQUEST_ERROR+group.name, false,  LOCAL_HOST_NAME+" says: Error while performing request "+errorMsg.request.getName()+" from group "+group.name+": "+errorMsg.error));
				writeFailedToLog(CHECK_RESULT_CODE_REQUEST_ERROR+ group.name, "Запрос "+errorMsg.request.getName()+" из группы "+group.name+" завершился ошибкой: "+errorMsg.error);
			}
		}
		return result;
	}

	private Result doChecksForGroup(RequestGroup group) {
		Result error = null;
		for (String host: hosts.split(";")) {
			error = doChecksForHostAndGroup(host.trim(), group);
			if (error == null) {
				// успешно выполнены все запросы
				return null;
			}
		}
		return error;
	}

	/**
	 * выполнить запросы; вернуть описание ошибки, если хотя бы 1 запрос не выполнился
	 * @param host
	 * @param group
	 * @return
	 * @throws Exception
	 */
	private Result doChecksForHostAndGroup(String host, RequestGroup group) {
		for (RequestInfo requestInfo: group.requests) {
			try {
				DataResponse responce = doRequest(host,requestInfo.getRequest());
				if (responce.getResult() != DataResponse.Result.OK) {
					return new Result(requestInfo, responce.getError());
				}
			} catch (Exception e) {
				logger.error(e, e);
				return new Result(requestInfo, e.toString());
			}

		}
		return null;
	}

	private static class Result {
		public final RequestInfo request;
		public final String error;
		public Result(RequestInfo request, String error) {
			super();
			this.request = request;
			this.error = error;
		}
		@Override
		public String toString() {
			return error;
		}
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
		this.timeIntervalStr = timeInterval;
	}

	public String getSettingsFolder() {
		return settingsFolder;
	}

	public void setSettingsFolder(String settingsFolder) {
		this.settingsFolder = settingsFolder;
	}



	private class RequestGroup {
		public final String name;
		public final List<RequestInfo> requests;

		public RequestGroup(String name, List<RequestInfo> requests) {
			super();
			this.name = name;
			this.requests = requests;
		}

		private Date nextExecutionDate = new Date();

		public boolean shouldBeExecutedNow() {
			return nextExecutionDate.before(new Date());
		}

		public void setNextExecuteAfter(Date nextDate) {
			nextExecutionDate = nextDate;
		}
	}


	private class SettingsLoader extends OnlineRequestSettingsLoader<RequestInfo> {

		public SettingsLoader(String warFile, String workingFile) {
			super(warFile, workingFile);
		}

		@Override
		protected RequestInfo getRequestInfo(BufferedReader reader)
				throws IOException {
			StringBuilder query  = new StringBuilder();
			String name = reader.readLine();
			String temp = reader.readLine();
			while (temp != null && !temp.startsWith("---")) {
				query.append(temp);
				temp = reader.readLine();
			}
			if (name==null || query.length()==0 || name.trim().isEmpty())
				return null;
			String queryStr = replaceParameters(query.toString());
			if (queryStr == null)
				return null;
			return new RequestInfo(name, queryStr);
		}
	}

	@Override
	public String getDescription() {
		return "Чекер для отслеживания ошибок групп онлайн запросов, в том числе отсутсвие прав в БД";
	}

}
