package ru.sberbank.syncserver2.service.monitor.check;

import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.FileCertificateCheckHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ServerPushCertificateCheck extends AbstractCheckAction {
	public static final String CHECK_RESULT_CODE_CERT_EXPIRED = "CERT_EXPIRED_";

	private static final Long DEFAULT_NOTIFY_WITHIN_DAYS = 30L;

	private volatile List<String> folders = new ArrayList<String>();

	private String notifyWithinDaysS = null;
	private Long notifyWithinDays = DEFAULT_NOTIFY_WITHIN_DAYS;

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

		tagLogger.log("Корневые папки с сертифкатами: "+folders);

		List<FileCertificateCheckHelper.CertificateInfo> certs = new ArrayList<FileCertificateCheckHelper.CertificateInfo>();
		for (String folder: folders) {
			FileCertificateCheckHelper.addCertificatesFromFolder(certs, new File(folder));
		}
		for (FileCertificateCheckHelper.CertificateInfo cert: certs) {
			tagLogger.log(cert.toString());
		}
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

		List<FileCertificateCheckHelper.CertificateInfo> certs = new ArrayList<FileCertificateCheckHelper.CertificateInfo>();
		for (String folder: folders) {
			FileCertificateCheckHelper.addCertificatesFromFolder(certs, new File(folder));
		}
		for (FileCertificateCheckHelper.CertificateInfo cert: certs) {
			String code = CHECK_RESULT_CODE_CERT_EXPIRED+cert.getName();
			boolean pass = cert.getExpirationDate().getTime() > System.currentTimeMillis() + notifyWithinDays*24*60*60*1000L;
			composeAndAddResultWithLog(result, pass, code,
					"Обнаружен истекающий сертификат! "+cert.toString(),
					"Сертификат обновлен! "+cert.toString());
		}
		return result;
	}


	public void setFolders(String folders) {
		List<String> local = new ArrayList<String>();
		for (String s: folders.split(";")) {
			String trimmed = s.trim().toLowerCase();
			if (!trimmed.isEmpty())
				local.add(trimmed);
		}
		this.folders = local;
	}

	
	@Override
	public String getDescription() {
		return "Чекер для проверки Push сертификатов";
	}

	public void setNotifyWithinDays(String notifyWithinDays) {
		this.notifyWithinDaysS = notifyWithinDays;
	}

}
