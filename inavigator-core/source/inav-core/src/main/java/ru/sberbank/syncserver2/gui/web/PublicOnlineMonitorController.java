package ru.sberbank.syncserver2.gui.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.log.TagHelper;
import ru.sberbank.syncserver2.service.monitor.MonitorService;
import ru.sberbank.syncserver2.service.monitor.check.AbstractCheckAction.CheckResultWrapper;
import ru.sberbank.syncserver2.service.monitor.check.CheckAction;
import ru.sberbank.syncserver2.service.monitor.check.ICheckResult;
import ru.sberbank.syncserver2.service.monitor.check.LDAPAvailabilityCheck;
import ru.sberbank.syncserver2.service.monitor.check.OnlineInfrastructureCheck;
import ru.sberbank.syncserver2.service.monitor.check.PushNotificationChecker;

@Controller
public class PublicOnlineMonitorController {
	protected OnlineInfrastructureCheck checker;
	protected LDAPAvailabilityCheck ldapChecker;
	protected PushNotificationChecker pnsChecker;
	protected MonitorService monitor;
	protected List<String> checkers;

	@RequestMapping(value="/show.public-monitor.gui")
	public String show() {
		return "onlineMonitorStatus";		
	}
	
	@ModelAttribute("targets")
	public String[] getTargets() {
		if (getChecker() != null)
			return getChecker().getAlphaSourceServices();
		return null;
	}
	
	@ModelAttribute("ldap")
	public boolean showLDAP() {
		return getLDAPChecker()==null ? false : true;
	}
	
	@ModelAttribute("pns")
	public boolean showPNS() {
		return getPNSChecker()==null ? false : true;
	}
		
	@RequestMapping(value="/status.public-monitor.gui",method = RequestMethod.GET,produces = "application/json")
	@ResponseBody
	public Result getStatus() {		
		return new Result(getLastUpdate(), getMonitorResult(), getFailedCheckers());
	}
	
	@ModelAttribute("checkers")
	protected List<String> getNames() {
		if (checkers != null)
			return checkers;
		List<String> result = new ArrayList<String>();
		for (CheckAction ca: getMonitorService().getActions()) {
			result.add(ca.getName());
		}
		checkers = result;
		return checkers;
	}
	
	protected List<String[]> getMonitorResult() {
		List<String[]> result = new ArrayList<String[]>();
		
		addToResult(result, getChecker().getFailedCheckResult());
		if (getLDAPChecker() != null) {
			addToResult(result, getLDAPChecker().getFailedCheckResult());
		}
		if (getPNSChecker() != null) {
			addToResult(result, getPNSChecker().getFailedCheckResult());
		}
		return result;
	}
	
	protected List<String> getFailedCheckers() {
		List<String> result = new ArrayList<String>();
		for (CheckAction ca: getMonitorService().getActions()) {
			for (ICheckResult cr: ca.getAllCheckResults()) {
				CheckResultWrapper real = (CheckResultWrapper) cr;
				if (!real.getRealResult().isPassed()) {
					result.add(ca.getName());
					break;
				}
			}
		}
		return result;
	}
	
	protected void addToResult(List<String[]> result, List<ICheckResult> checkResults) {
		for (ICheckResult checkResult: checkResults) {
			String tag = TagHelper.getTagByCheckCodeForOnlineMonitor(checkResult.getCode());
			if (tag != null) {
				result.add(new String[]{tag,""});
			}
		}
	}

	public Long getLastUpdate() {
		Long lastTime = getChecker().getLastUpdate();
		lastTime = getMinLastUpdate(getLDAPChecker(), lastTime);
		lastTime = getMinLastUpdate(getPNSChecker(), lastTime);
		return lastTime;
	}
	
	protected Long getMinLastUpdate(CheckAction checker, Long lastUpdate) {
		if (checker != null) {
			return checker.getLastUpdate() < lastUpdate ? checker.getLastUpdate() : lastUpdate;
		} else {
			return lastUpdate;
		}
	}
	
	protected OnlineInfrastructureCheck getChecker() {
		if (checker == null) {			
			checker = (OnlineInfrastructureCheck) ServiceManager.getInstance().findFirstServiceByClassCode(OnlineInfrastructureCheck.class);
		}
		return checker;
	}
	
	protected LDAPAvailabilityCheck getLDAPChecker() {
		if (ldapChecker == null) {			
			ldapChecker = (LDAPAvailabilityCheck) ServiceManager.getInstance().findFirstServiceByClassCode(LDAPAvailabilityCheck.class);
		}
		return ldapChecker;
	}
	
	protected PushNotificationChecker getPNSChecker() {
		if (pnsChecker == null) {			
			pnsChecker = (PushNotificationChecker) ServiceManager.getInstance().findFirstServiceByClassCode(PushNotificationChecker.class);
		}
		return pnsChecker;
	}
	
	protected MonitorService getMonitorService() {
		if (monitor == null) {
			monitor = (MonitorService) ServiceManager.getInstance().findFirstServiceByClassCode(MonitorService.class);
		}
		return monitor;
	}
	
	public static class Result {
		public final Long lastUpdate;
		public final List<String[]> result;
		public final List<String> additionals;
		public Result(Long lastUpdate, List<String[]> result) {
			super();
			this.lastUpdate = lastUpdate;
			this.result = result;
			additionals = null;
		}
		public Result(Long lastUpdate, List<String[]> result, List<String> additionals) {
			super();
			this.lastUpdate = lastUpdate;
			this.result = result;
			this.additionals = additionals;
		}
	}

}
