package ru.sberbank.syncserver2.gui.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.gui.db.dao.MaintenanceDao;


@Controller
public class MaintenanceController {
	private MaintenanceDao maintenanceDao;
	
	@Autowired
	public void setMaintenanceDao(MaintenanceDao maintenanceDao) {
		this.maintenanceDao = maintenanceDao;
	}
	
	@RequestMapping(value="/status.maintenance",method = RequestMethod.GET,produces = "application/json")
	@ResponseBody
	public Object[] getStatus() {
		return maintenanceDao.getMaintenanceStatus();
	}
	
	@RequestMapping(value="/form.maintenance.gui",method = RequestMethod.GET,produces = "application/json")
	public String show(Model model) {
		Object[] status = maintenanceDao.getMaintenanceStatus();
		model.addAttribute("active", status[0]);
		model.addAttribute("emails", status[2]);
		model.addAttribute("message", status[1]);
		return "maintenance";
	}
	
	
	@RequestMapping(value="/save.maintenance.gui", method = RequestMethod.POST)
	public String save(@RequestParam(defaultValue="false") Boolean active, @RequestParam String message, @RequestParam String[] emails, HttpServletRequest request) {
		//save
		StringBuilder stringToDB = new StringBuilder();
		StringBuilder stringToAudit = new StringBuilder();
		for (String email: emails) {
			if (email == null || email.trim().equals(""))
				continue;
			
			if (stringToDB.length() == 0)
				stringToDB.append(email);
			else
				stringToDB.append(";").append(email);
			
			if (stringToAudit.length() == 0)
				stringToAudit.append(email);
			else
				stringToAudit.append(", ").append(email);
		}
		maintenanceDao.updateMaintenanceStatus(active, message, stringToDB.toString());
		AuditHelper.write(request, (active ? "Включение" : "Выключение") + " режима поддержки" , 
					active ? "Текст сообщения: " + message +". Список разрешенных пользователей: " + stringToAudit.toString() : "", AuditHelper.SERVICE);
		return "redirect:form.maintenance.gui";
	}
	
}
