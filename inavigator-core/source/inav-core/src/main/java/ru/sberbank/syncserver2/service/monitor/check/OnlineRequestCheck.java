package ru.sberbank.syncserver2.service.monitor.check;

import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.monitor.data.CertificateLoader;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.util.URLConnectionHelper;

import javax.net.ssl.SSLSocketFactory;
import java.util.ArrayList;
import java.util.List;

public abstract class OnlineRequestCheck extends AbstractCheckAction {
	public static final String CHECK_RESULT_CODE_NO_CERTIFICATE = "CERTIFICATE_DID_NOT_LOAD";
	protected SSLSocketFactory socketFactory;
	protected String certificateFile;
	protected String passwordFile;
	protected String onlineSqlUrl = "/syncserver/online/online.sql";
	protected String SSLProtocol = "TLSv1.2";
	protected String replaceParameters = "@service@;inavdev.SBT@sberbank.ru";
	protected String replaceValues = "finik2-new;inavdev.SBT@sberbank.ru";

	@Override
	protected void doStart() {
		logServiceMessage(LogEventType.SERV_START, "starting service");

		try {
			socketFactory = new CertificateLoader(certificateFile, passwordFile, SSLProtocol).getSSLSocetFactory();
			tagLogger.log("Сертификат "+certificateFile+" успешно загружен.");
		} catch (Exception e) {
			logger.error(e, e);
			socketFactory = null;
			tagLogger.log("Не удалось загрузить сертификат "+certificateFile+" с ключем в "+passwordFile+": "+e.getMessage());
		}

		init();

		logServiceMessage(LogEventType.SERV_START, "started service");
	}

	/**
	 * аналог doStart() для наследников
	 */
	protected abstract void init();

	/**
	 * аналог doCheck() для наследников
	 * @return
	 */
	protected abstract List<CheckResult> doInternalCheck();

	@Override
	protected List<CheckResult> doCheck() {
		List<CheckResult> result = new ArrayList<CheckResult>();
		boolean skipCheck =false;

		if (socketFactory==null) {
			result.add(new CheckResult(CHECK_RESULT_CODE_NO_CERTIFICATE, false, LOCAL_HOST_NAME+" says: Can't load checker's certificate from "+certificateFile));
			writeFailedToLog(CHECK_RESULT_CODE_NO_CERTIFICATE, "Сертификат не загружен");
			skipCheck = true;
		} else {
			if (addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_NO_CERTIFICATE, LOCAL_HOST_NAME+" says: Checker's certificate have been successfully loaded from "+certificateFile))
				tagLogger.log("Сертификат "+certificateFile+" успешно загружен");
		}

		if (skipCheck)
			return result;

		result.addAll(doInternalCheck());

		return result;
	}

	/**
	 * Выполнить запрос на определенном хосту и вернуть результат или бросить Exception
	 * @param host
	 * @param request
	 * @return
	 * @throws Exception
	 */
	protected DataResponse doRequest(String host, String request) throws Exception {
		return URLConnectionHelper.doRequest(host+onlineSqlUrl, request, socketFactory);
	}

	protected String replaceParameters(String query) {
		try {
			String[] params = this.replaceParameters.split(";");
			String[] values = this.replaceValues.split(";");

			for (int i = 0; i < params.length; i++) {
				query = query.replaceAll(params[i],values[i]);
			}

			return query;
		} catch (Exception e) {
			tagLogger.log("Ошибка при замене параметров "+this.replaceParameters+" в запросе значениями "+this.replaceValues+": "+e.toString());
			logger.error(e, e);
		}
		return null;
	}

	public String getSSLProtocol() {
		return SSLProtocol;
	}

	public void setSSLProtocol(String protocol) {
		this.SSLProtocol = protocol;
	}

	public String getOnlineSqlUrl() {
		return onlineSqlUrl;
	}

	public void setOnlineSqlUrl(String onlineSqlUrl) {
		this.onlineSqlUrl = onlineSqlUrl;
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

	public String getReplaceParameters() {
		return replaceParameters;
	}

	public void setReplaceParameters(String replaceParameters) {
		this.replaceParameters = replaceParameters;
	}

	public String getReplaceValues() {
		return replaceValues;
	}

	public void setReplaceValues(String replaceValues) {
		this.replaceValues = replaceValues;
	}
}
