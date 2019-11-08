package ru.sberbank.syncserver2.gui.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.sberbank.syncserver2.gui.util.format.JSPFormatPool;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.monitor.MonitorService;
import ru.sberbank.syncserver2.service.monitor.check.AbstractCheckAction.CheckResultWrapper;
import ru.sberbank.syncserver2.service.monitor.check.CheckAction;
import ru.sberbank.syncserver2.service.monitor.check.ICheckResult;


@Controller
public class CheckerStatusesController {
	private List<CheckAction> checkers;
	
	public List<CheckAction> getCheckers() {
		if (checkers == null) {			
			MonitorService monitor = (MonitorService) ServiceManager.getInstance().findFirstServiceByClassCode(MonitorService.class);			
			if (monitor == null || monitor.getActions() == null) {
				checkers = Collections.emptyList();
			} else {
				checkers = monitor.getActions();
			}
		}
		return checkers;
	}
	
	@RequestMapping(value="/status.checkers.gui",method = RequestMethod.GET,produces = "application/json")
	@ResponseBody
	public List<Checker> getState() {
		List<Checker> result = new ArrayList<CheckerStatusesController.Checker>(getCheckers().size());
		for (CheckAction checker: checkers) {
			List<CheckerStatus> inner = new ArrayList<CheckerStatus>();			
			for (ICheckResult checkResult: checker.getAllCheckResults()) {
				CheckResultWrapper wrapper = (CheckResultWrapper) checkResult;
				inner.add(new CheckerStatus(wrapper));
			}
			result.add(new Checker(checker.getName(), inner, JSPFormatPool.formatDateAndTime2(checker.getLastUpdate())));
		}
		return result;
	}
	
	@RequestMapping(value="/show.checkers.gui",method = RequestMethod.GET)
	public String show() {
		return "checkers";
	}
	
	@ModelAttribute("names")
	public List<String[]> getNames() {
		List<String[]> result = new ArrayList<String[]>();
		for (CheckAction checker: getCheckers()) {
			result.add(new String[] {checker.getName(), checker.getDescription()});
		}
		return result;
	}
	
	public static class Checker {
		public final String name;
		public final List<CheckerStatus> statuses;
		public final String lastUpdate;
		public Checker(String name, List<CheckerStatus> statuses, String lastUpdate) {
			super();
			this.name = name;
			this.statuses = statuses;
			this.lastUpdate = lastUpdate;
		}
	}

	public static class CheckerStatus {
		public final String code;
		public final int status;
		public final String txt;
		
		public CheckerStatus(CheckResultWrapper wrapper) {
			this.code = wrapper.getCode();
			this.txt = wrapper.getErrorMessage();
			if (wrapper.getRealResult().isPassed()) {
				this.status = 0;
			} else if (wrapper.isPassed()) {
				this.status = 1;
			} else {
				this.status = 2;
			}
		}		
	}
}
