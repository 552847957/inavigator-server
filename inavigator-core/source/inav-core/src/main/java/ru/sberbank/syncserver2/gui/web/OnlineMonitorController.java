package ru.sberbank.syncserver2.gui.web;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.sberbank.syncserver2.service.log.TagHelper;
import ru.sberbank.syncserver2.service.monitor.check.ICheckResult;

@Controller
public class OnlineMonitorController extends PublicOnlineMonitorController{
	

	@RequestMapping(value="/show.online-monitor.gui")
	public String show() {
		return "onlineMonitor";		
	}
	
	@Override
	@RequestMapping(value="/status.online-monitor.gui",method = RequestMethod.GET,produces = "application/json")
	@ResponseBody
	public Result getStatus() {
		
		return new Result(getLastUpdate(), getMonitorResult());
	}
	
	@Override
	protected void addToResult(List<String[]> result, List<ICheckResult> checkResults) {
		for (ICheckResult checkResult: checkResults) {
			String tag = TagHelper.getTagByCheckCodeForOnlineMonitor(checkResult.getCode());
			if (tag != null) {
				result.add(new String[]{tag,checkResult.getErrorMessage()});
			}
		}
	}
	
	
	
	
	@RequestMapping(value="/run.online-monitor.gui",method = RequestMethod.POST,produces = "application/json")
	@ResponseBody
	public Result manualRun() {
		getMonitorService().doManualCheck(getChecker());
		if (getLDAPChecker() != null)
			getMonitorService().doManualCheck(getLDAPChecker());
		if (getPNSChecker() != null)
			getMonitorService().doManualCheck(getPNSChecker());
		return getStatus();
	}

}
