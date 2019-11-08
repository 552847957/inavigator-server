package ru.sberbank.syncserver2.service.monitor.check;

import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.ServersCertificateCheckHelper;
import ru.sberbank.syncserver2.util.ServersCertificateCheckHelper.CertificateChainInformation;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ServerTLSCertificateCheck extends AbstractCheckAction {
	public static final String CHECK_RESULT_CODE_COULD_NOT_GET_CERT_FROM_HOST = "COULD_NOT_GET_CERT_FROM_HOST_";
	public static final String CHECK_RESULT_CODE_CERT_EXPIRED_ON_HOST = "CERT_EXPIRED_ON_HOST_";

	private static final Long DEFAULT_TIME_INTERVAL = 24*60*60L;
	private static final Long DEFAULT_NOTIFY_WITHIN_DAYS = 30L;

	private volatile List<String> hosts = new ArrayList<String>();
	private Long timeInterval = DEFAULT_TIME_INTERVAL;
	private String timeIntervalS = null;
	private ServersCertificateCheckHelper certificateCheckHelper;
	private String SSLProtocol = "TLSv1.2";
	
	private volatile Map<String, CertificateCheckResult> certsExpirationResult;

	private Long notifyWithinDays = DEFAULT_NOTIFY_WITHIN_DAYS;
	private String notifyWithinDaysS = null;


	@Override
	protected void doStart() {
		logServiceMessage(LogEventType.SERV_START, "starting service");

		if (notifyWithinDaysS != null) {
			try {
				this.notifyWithinDays = Long.parseLong(notifyWithinDaysS);
			} catch (Exception e) {
				logger.error(e, e);
				tagLogger.log("Неверно задан параметр notifyWithinDays. Будет использовано значение по умолчанию "+this.notifyWithinDays);
			}
		}

		if (timeIntervalS != null) {
			try {
				this.timeInterval = Long.parseLong(timeIntervalS);
			} catch (Exception e) {
				logger.error(e, e);
				tagLogger.log("Неверно задан параметр timeInterval. Будет использовано значение по умолчанию "+this.timeInterval);
			}
		}

		tagLogger.log("Мониторинг хостов: "+hosts);

		try {
			certificateCheckHelper = new ServersCertificateCheckHelper(SSLProtocol);
		} catch (Exception e) {
			logger.error(e, e);
			tagLogger.log("Ошибка при создании SSLSocketFactory: "+e.getMessage());
		}

		certsExpirationResult = new HashMap<String, CertificateCheckResult>();
		logServiceMessage(LogEventType.SERV_START, "started service");
	}

	@Override
	protected void doStop() {
		super.doStop();
		clearAllCheckResults();
	}

	@Override
	protected List<CheckResult> doCheck() {
		final List<CheckResult> result = new ArrayList<CheckResult>();
		for (String hostAndPort: hosts) {
			String[] split = hostAndPort.split(":");
			String host = split[0];
			int port = 443;
			if (split.length > 1) {
				try {
					port = Integer.parseInt(split[1]);
				} catch (Exception e) {
					//ignore
				}
			}
			checkHost(result, host, port);
		}

		return result;
	}

	private void checkHost(List<CheckResult> result, String host, int port) {
		String compHost = host+":"+port;
		if (certsExpirationResult.containsKey(compHost) && certsExpirationResult.get(compHost).nextCheck.before(new Date()))
			return;
		CertificateChainInformation check = null;
		Exception temp = null;
		try {
			check = certificateCheckHelper.check(host, port);
		} catch (Exception e) {
			logger.error(e, e);
			temp = e;
		}
		composeAndAddResultWithLog(result, check != null, CHECK_RESULT_CODE_COULD_NOT_GET_CERT_FROM_HOST+compHost,
				"Не удалось получить сертифкаты от сервера "+compHost+(temp == null ? "" : ". "+temp.toString()),
				"Получен сертифкат от сервера "+compHost);

		if (check == null)
			return;

		CertificateCheckResult old = certsExpirationResult.get(compHost);
		if (old == null || !old.expired.equals(check.getExpiredDate())) {
			tagLogger.log(check.toString());
			old = new CertificateCheckResult(check.getExpiredDate(), compHost);
			certsExpirationResult.put(compHost, old);
		}


		String code = CHECK_RESULT_CODE_CERT_EXPIRED_ON_HOST+compHost;

		boolean passed = check.getExpiredDate().getTime() > System.currentTimeMillis() + notifyWithinDays*24*60*60*1000L;
		composeAndAddResultWithLog(result, passed, code,
				"Обнаружен истекающий сертификат! "+check.toString(),
				"Сертификат сервера был обновлен! "+check.toString());

		if (passed) {
			certsExpirationResult.get(compHost).nextCheck = new Date(System.currentTimeMillis() + timeInterval);
		}

	}


	public void setMonitoredHosts(String hosts) {
		List<String> local = new ArrayList<String>();
		for (String s: hosts.split(";")) {
			String trimmed = s.trim().toLowerCase();
			if (!trimmed.isEmpty())
				local.add(trimmed);
		}
		this.hosts = local;
	}
	
	
	public void setTimeInterval(String timeInterval) {
		this.timeIntervalS = timeInterval;
	}
	
	@Override
	public String getDescription() {
		return "Чекер для проверки SSL сертификатов указанных хостов";
	}

	public String getSSLProtocol() {
		return SSLProtocol;
	}

	public void setSSLProtocol(String SSLProtocol) {
		this.SSLProtocol = SSLProtocol;
	}

	public void setNotifyWithinDays(String notifyWithinDays) {
		this.notifyWithinDaysS = notifyWithinDays;
	}

	private class CertificateCheckResult {
		final Date expired;
		Date nextCheck = new Date();
		final String host;

		public CertificateCheckResult(Date expired, String host) {
			this.expired = expired;
			this.host = host;
		}
	}

	public static void main(String[] args) throws KeyManagementException, NoSuchAlgorithmException, IOException {
		ServersCertificateCheckHelper helper = new ServersCertificateCheckHelper("TLSv1.2");
		CertificateChainInformation result = helper.check("config1.mobile-test.sbrf.ru", 9443);
		System.out.println(result.getClosestCert().getSubjectDN());
	}

}
