package ru.sberbank.syncserver2.service.monitor.check;

import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.log.TagHelper;
import ru.sberbank.syncserver2.service.monitor.data.CertificateLoader;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.util.URLConnectionHelper;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.net.ssl.SSLSocketFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlineInfrastructureCheck extends AbstractCheckAction {
	public static final String CHECK_RESULT_CODE_NO_CERTIFICATE = "CERTIFICATE_DID_NOT_LOAD";
	public static final String CHECK_RESULT_CODE_FOR_SYNCSERVER = "REQUEST_ERROR_FOR_SYNCSERVER";
	public static final String CHECK_RESULT_CODE_FOR_DP = "REQUEST_ERROR_FOR_DP";
	public static final String CHECK_RESULT_CODE_FOR_PROXY = "REQUEST_ERROR_FOR_PROXY";
	public static final String CHECK_RESULT_CODE_FOR_MSSQL = "REQUEST_ERROR_FOR_MSSQL_";

	private SSLSocketFactory socketFactory;
	private String certificateFile;
	private String passwordFile;
	private String[] hosts;
	private String onlineSqlUrl = "/syncserver/online/online.sql";
	private String SSLProtocol = "TLSv1.2";

	/**
	 * шаблон запроса
	 */
	private String request;
	private String sqlStoredProcedure = "SELECT 1";
	private String provider = "DATAPOWER";
	private String[] alphaSourceServices;

	{
		// устанавливаем максимальное значение, чтобы монитор никогда не уведомлял о результате
		setDefaultDoNotNotifyCount(Integer.MAX_VALUE);
	}

	@Override
	protected void doStart() {
		logServiceMessage(LogEventType.SERV_START, "starting service");

		try {
			socketFactory = new CertificateLoader(certificateFile, passwordFile, SSLProtocol).getSSLSocetFactory();
			tagLogger.log("Сертификат "+certificateFile+" успешно загружен.");
		} catch (Exception e) {
			logger.error(e, e);
			tagLogger.log("Не удалось загрузить сертификат "+certificateFile+" с ключем в "+passwordFile+": "+e.getMessage());
		}

		clearAllCheckResults();

		// создаем заново шаблон, т.к. некоторые поля могли поменяться
		OnlineRequest request = new OnlineRequest();
		request.setStoredProcedure(sqlStoredProcedure);
		request.setProvider(provider);
		request.setService("%s");		// для кастомизации будем использовать formatter
		try {
			this.request = XMLHelper.writeXMLToString(request, false, OnlineRequest.class);
		} catch (Exception e) {
			logger.error(e, e);
			tagLogger.log("Не удалось преобразовать запрос "+request+" в строку: "+e);
		}

		logServiceMessage(LogEventType.SERV_START, "started service");
	}

	@Override
	protected List<CheckResult> doCheck() {
		List<CheckResult> result = new ArrayList<CheckResult>();
		boolean skipCheck =false;

		if (socketFactory==null) {
			result.add(new CheckResult(CHECK_RESULT_CODE_NO_CERTIFICATE, false, LOCAL_HOST_NAME+" says: Can't load checker certificate from "+certificateFile));
			skipCheck = true;
		} else {
			addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_NO_CERTIFICATE, LOCAL_HOST_NAME+" says: Checker certificate have been successfully loaded from "+certificateFile);
		}

		if (skipCheck)
			return result;

		// неуспешные ответы с каждой БД
		Map<String, DataResponse> failedMSSQLResponses = new HashMap<String, DataResponse>();

		DataResponse errorResponse = null;
		for (String source: alphaSourceServices) {
			DataResponse response = doRequest(String.format(request, source));
			if (response.getResult() == Result.OK)
				continue;

			if (response.getResult() == Result.FAIL_ACCESS || response.getResult() == Result.FAIL_DB)
				failedMSSQLResponses.put(source, response);
			else {
				errorResponse = response;
				break; // канал неисправный, прекращаем проверку для различных источников
			}

		}

		if (errorResponse != null) {
			// канал неисправный
			if (errorResponse.getResult() != null)
				switch (errorResponse.getResult()) {
					case FAIL_DP: failDP(result, errorResponse); break;
					case FAIL: failProxy(result, errorResponse); break;
					default: failSyncserver(result, errorResponse);
				}
			else
				failSyncserver(result, errorResponse);
		} else {
			// все ок или какая-то БД недоступна
			failOrOkMSSQL(result, failedMSSQLResponses);
		}

		TagHelper.writeToOnlineMonitorTag(result); // запись в журнал

		return result;
	}

	private void failSyncserver(List<CheckResult> result, DataResponse response) {
		result.add(new CheckResult(CHECK_RESULT_CODE_FOR_SYNCSERVER, false, "Syncserver недоступен ("+response.getError()+")"));
		// другие ошибки не могут быть проверены
	}

	private void failDP(List<CheckResult> result, DataResponse response) {
		result.add(new CheckResult(CHECK_RESULT_CODE_FOR_DP, false, "Datapower недоступен ("+response.getError()+")"));
		// syncserver точно доступен
		addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_FOR_SYNCSERVER, "Syncserver стал доступным");
	}

	private void failProxy(List<CheckResult> result, DataResponse response) {
		result.add(new CheckResult(CHECK_RESULT_CODE_FOR_PROXY, false, "Proxyserver недоступен ("+response.getError()+")"));
		addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_FOR_SYNCSERVER, "Syncserver стал доступным");
		addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_FOR_DP, "Datapower стал доступным");
	}

	private void failOrOkMSSQL(List<CheckResult> result, Map<String, DataResponse> failedResponses) {
		for (String source: alphaSourceServices) {
			DataResponse response = failedResponses.get(source);
			if (response != null)
				result.add(new CheckResult(CHECK_RESULT_CODE_FOR_MSSQL+source, false, "MSSQL ("+source+") недоступен ("+response.getError()+")"));
			else
				addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_FOR_MSSQL+source, "MSSQL ("+source+") стал доступным");
		}
		addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_FOR_SYNCSERVER, "Syncserver стал доступным");
		addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_FOR_DP, "Datapower стал доступным");
		addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_FOR_PROXY, "Proxyserver стал доступным");
	}

	/**
	 * Получить наилучший ответ от запроса на все хосты.
	 * Если вернулся DataResponse c Result=null - это наихудший вариант (не работает syncserver)
	 * @param request
	 * @return
	 */
	private DataResponse doRequest(String request) {
		DataResponse response = null;
		Exception exception = null;
		for (String host: hosts) {
			try {
				//if (true) throw new Exception("ha");
				DataResponse localResponse = doRequest(host.trim(), request);

				if (localResponse.getResult() == Result.OK)
					return localResponse; // это и будет наилучшим ответом

				// если localResponse более лучший вариант, то запоминаем его
				// enum DataResponse.Result упорядочен соответственно
				if (response == null || response.getResult().compareTo(localResponse.getResult())>0) {
					response = localResponse;
				}
			} catch (Exception e) {
				exception = new Exception("Request to host "+host+" failed. "+e.getMessage(), e);
			}
		}

		// НЕ логируем ошибку от Syncserver, если хоть 1 из хостов работает (вернулся результат)
		if (response == null && exception != null) {
			logger.error(exception, exception);
			response = new DataResponse();
			response.setError(exception.getMessage());
		}
		return response;
	}


	/**
	 * Выполнить запрос на определенном хосту и вернуть результат или бросить Exception
	 * @param host
	 * @param request
	 * @return
	 * @throws Exception
	 */
	private DataResponse doRequest(String host, String request) throws Exception {
		return URLConnectionHelper.doRequest(host+onlineSqlUrl, request, socketFactory);
	}

	public String getCertificateFile() {
		return certificateFile;
	}

	public void setCertificateFile(String certificateFile) {
		this.certificateFile = certificateFile;
	}

	public String getPasswordFile() {
		return passwordFile;
	}

	public void setPasswordFile(String passwordFile) {
		this.passwordFile = passwordFile;
	}

	public void setHosts(String hosts) {
		this.hosts = hosts.split(";");
	}

	public String[] getAlphaSourceServices() {
		return alphaSourceServices;
	}

	public void setAlphaSourceServices(String alphaSourceServices) {
		this.alphaSourceServices = alphaSourceServices.split(";");
	}

	public String getOnlineSqlUrl() {
		return onlineSqlUrl;
	}

	public void setOnlineSqlUrl(String onlineSqlUrl) {
		this.onlineSqlUrl = onlineSqlUrl;
	}

	public String getSqlStoredProcedure() {
		return sqlStoredProcedure;
	}

	public void setSqlStoredProcedure(String sqlStoredProcedure) {
		this.sqlStoredProcedure = sqlStoredProcedure;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public static void main(String[] args) throws Exception {
	}

	public String getSSLProtocol() {
		return SSLProtocol;
	}

	public void setSSLProtocol(String protocol) {
		this.SSLProtocol = protocol;
	}

	@Override
	public String getDescription() {
		return "Чекер для сквозной проверки визуальной инфраструктуры (без уведомлений)";
	}

}
