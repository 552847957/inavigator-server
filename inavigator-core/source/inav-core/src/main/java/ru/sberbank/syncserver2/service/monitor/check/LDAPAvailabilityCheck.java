package ru.sberbank.syncserver2.service.monitor.check;

import java.util.ArrayList;
import java.util.List;

import ru.sberbank.syncserver2.service.ldap.LdapGroupManagementService;
import ru.sberbank.syncserver2.service.log.TagHelper;

public class LDAPAvailabilityCheck extends AbstractCheckAction {
	
	public static final String CHECK_RESULT_CODE_LDAP_SERVICE_IS_NULL = "LDAP_SERVICE_IS_NULL";
	public static final String CHECK_RESULT_CODE_REQUEST_TO_LDAP_WAS_FAILED = "LDAP_NOT_AVAILABLE";
	
	private static final String CHECK_EMAIL = "inavdev.sbt@sberbank.ru"; // email для тестовой проверки
	
	private LdapGroupManagementService ldapService;
	
	{
		setDefaultDoNotNotifyCount(2);
	}

	@Override
	protected List<CheckResult> doCheck() {
		List<CheckResult> result = new ArrayList<CheckResult>();
		
		if (ldapService == null) {
			result.add(new CheckResult(CHECK_RESULT_CODE_LDAP_SERVICE_IS_NULL, false, LOCAL_HOST_NAME+" says: LdapGroupManagementService is null"));
			tagLogger.log("LDAP сервис неопределен. Возможно, ошибка в конфигурации приложения.");
			return result;
		} else {
			if (addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_LDAP_SERVICE_IS_NULL, LOCAL_HOST_NAME+" says: LdapGroupManagementService has been found"))
				tagLogger.log("LDAP сервис найден");
		}
		
		try {
			ldapService.hasInavUserGroup(CHECK_EMAIL);
			if (addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_REQUEST_TO_LDAP_WAS_FAILED, LOCAL_HOST_NAME+" says: Ldap is available")) {
				tagLogger.log("LDAP стал доступным");
			}
		} catch (Exception e) {
			result.add(new CheckResult(CHECK_RESULT_CODE_REQUEST_TO_LDAP_WAS_FAILED, false, LOCAL_HOST_NAME+" says: Ldap is not available"));
			tagLogger.log("LDAP недоступен: "+e);
		}
		
		TagHelper.writeToOnlineMonitorTag(result); // запись в журнал
		return result;
	}

	public LdapGroupManagementService getLdapService() {
		return ldapService;
	}

	public void setLdapService(LdapGroupManagementService ldapService) {
		this.ldapService = ldapService;
	}
	
	@Override
	public String getDescription() {	
		return "Чекер для проверки доступности LDAP";
	}
	
	public static void main(String[] args) throws Exception {
		LdapGroupManagementService service = new LdapGroupManagementService();
		service.setSettings("IncidentManagement//c,th,fyr2013//CN=i-Navigator-U,OU=NetApp,DC=sigma,DC=sbrf,DC=ru//DC=sigma,DC=sbrf,DC=ru");
		service.setProvider("ldap://lake1.sigma.sbrf.ru:389;ldap://lake2.sigma.sbrf.ru:389");
		service.setDomain("SIGMA");
		System.out.println(service.hasInavUserGroup("inavdev.sbt@sberbank.ru"));
	}


}
