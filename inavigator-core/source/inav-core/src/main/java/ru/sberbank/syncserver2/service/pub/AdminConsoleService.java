package ru.sberbank.syncserver2.service.pub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import ru.sberbank.syncserver2.gui.data.AuthContext;
import ru.sberbank.syncserver2.gui.data.Employee;
import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.gui.db.dao.AdminConsoleDao;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.XmlPublicService;
import ru.sberbank.syncserver2.service.log.LogAction;
import ru.sberbank.syncserver2.service.log.TagBuffers;
import ru.sberbank.syncserver2.service.pub.xml.Authentication;
import ru.sberbank.syncserver2.service.pub.xml.EditEmployeeRequest;
import ru.sberbank.syncserver2.service.pub.xml.GetEmployeesRequest;
import ru.sberbank.syncserver2.service.pub.xml.LogMessages;
import ru.sberbank.syncserver2.service.pub.xml.LogsTags;
import ru.sberbank.syncserver2.service.pub.xml.Message;
import ru.sberbank.syncserver2.service.pub.xml.Message.Status;
import ru.sberbank.syncserver2.service.pub.xml.Services;
import ru.sberbank.syncserver2.service.pub.xml.Services.Service;
import ru.sberbank.syncserver2.service.pub.xml.Services.ServiceCommand;
import ru.sberbank.syncserver2.service.pub.xml.Settings;
import ru.sberbank.syncserver2.service.pub.xml.Settings.Setting;
import ru.sberbank.syncserver2.service.pub.xml.SyncEmployeesRequest;

public class AdminConsoleService extends XmlPublicService {	
	
	private AdminConsoleDao adminConsoleDao;
	
	@Autowired
	public void setPropertyDao(AdminConsoleDao propertyDao) {
		this.adminConsoleDao = propertyDao;
	}

	private Integer logsOnPage = 20;
	
	private static Class[] XMLClasses = {
		LogMessages.class,
		LogsTags.class,
		Services.class,
		Settings.class,
		Authentication.class,
		GetEmployeesRequest.class,
		EditEmployeeRequest.class,
		SyncEmployeesRequest.class
	}; 	
	
	@Override
	protected Class[] getSupportedXmlClasses() {		
		return XMLClasses;
	}

	public Object xmlRequest(HttpServletRequest request,
			HttpServletResponse response, Object xmlRequest) {
		if (xmlRequest instanceof Authentication)
			return authenticate((Authentication)xmlRequest, request, response);
		else if (xmlRequest instanceof LogMessages) {
			LogMessages logMessages = (LogMessages) xmlRequest;
			if (!checkAccessAndWriteCode(request,logMessages, true))
				return logMessages;
			return getLogMessages(logMessages);				
		} else if (xmlRequest instanceof LogsTags) {
			LogsTags logsTags = (LogsTags) xmlRequest;
			if (!checkAccessAndWriteCode(request,logsTags, true))
				return logsTags;
			return getLogsTags(logsTags);
		} else if (xmlRequest instanceof Services) {
			return getServices(request,response,(Services)xmlRequest);
		} else if (xmlRequest instanceof Settings) {
			return getSettings(request,response,(Settings)xmlRequest);
		} else if (xmlRequest instanceof GetEmployeesRequest) {	
			GetEmployeesRequest getEmployeesRequest = (GetEmployeesRequest) xmlRequest;
			if (!checkAccessAndWriteCode(request,getEmployeesRequest, true))
				return xmlRequest;	
			return getEmployeeRequest(request,response,getEmployeesRequest);
		} else if (xmlRequest instanceof EditEmployeeRequest) {	
			EditEmployeeRequest employeesRequest = (EditEmployeeRequest) xmlRequest;
			if (!checkAccessAndWriteCode(request,employeesRequest, false))
				return xmlRequest;	
			return editEmployeeRequest(request,response,employeesRequest);
		} else if (xmlRequest instanceof SyncEmployeesRequest) {	
			SyncEmployeesRequest employeesRequest = (SyncEmployeesRequest) xmlRequest;
			if (!checkAccessAndWriteCode(request,employeesRequest, false))
				return xmlRequest;	
			return syncEmployeesRequest(request,response,employeesRequest);
		} else		
			try {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			} catch (IOException e) {
			}
		return null;
	}	

	private SyncEmployeesRequest syncEmployeesRequest(HttpServletRequest request, HttpServletResponse response,
			SyncEmployeesRequest employeesRequest) {
		if (employeesRequest.getCommand().equals(SyncEmployeesRequest.GET)) {
			employeesRequest.setEmployees(adminConsoleDao.listEmployeesWithPass());
		} else if (employeesRequest.getCommand().equals(SyncEmployeesRequest.SET)&&employeesRequest.getEmployees()!=null&&!employeesRequest.getEmployees().isEmpty()) {
			adminConsoleDao.execute("DELETE FROM EMPLOYEES WHERE EMPLOYEE_ROLE_ID=1", new Object[0]);
			if (!adminConsoleDao.setEmployess(employeesRequest.getEmployees())) {				
				employeesRequest.setCode(Status.FAILED);
				employeesRequest.setNoteMessage("Not all employees were inserted");
			} else {
				String info = "Текущие администраторы:";
				for (Employee e: employeesRequest.getEmployees()) {
					info += "\n"+e;
				}
				AuditHelper.write(request, "Синхронизация пользователей", info, AuditHelper.EMPLOYEE);					
			}
		} else {
			employeesRequest.setCode(Status.BAD_REQUEST);
		}
		return employeesRequest;		
	}

	private EditEmployeeRequest editEmployeeRequest(HttpServletRequest request, HttpServletResponse response,
			EditEmployeeRequest employeeRequest) {
		Employee employee = employeeRequest.getEmployee();
		switch (employeeRequest.getCommand()) {
		case EditEmployeeRequest.ADD:
			if (employee!=null) {
				if (adminConsoleDao.existEmployeeWithEmail(employee.getEmployeeEmail())) {
					employeeRequest.setCode(Status.FAILED);
					employeeRequest.setNoteMessage("User with email "+employee.getEmployeeEmail()+" is alrady exist");
					return employeeRequest;
				}
				employee.setEmployeeId(-1);
				adminConsoleDao.saveEmployee(employee);
				AuditHelper.write(request, "Создание пользователя", "Создание пользователя "+employee.getEmployeeEmail()+". \nСоздан пользователь \n"+employee, AuditHelper.EMPLOYEE);
			}
			return employeeRequest;
		case EditEmployeeRequest.CHANGE_PASSWORD:
			if (employee!=null) {				
				adminConsoleDao.changePassword(employee.getEmployeeEmail(), employee.getEmployeePassword());
				AuditHelper.write(request, "Изменение пароля", "Изменение пароля для пользователя "+employee.getEmployeeEmail(), AuditHelper.EMPLOYEE);
			}
			return employeeRequest;
		case EditEmployeeRequest.EDIT:
			if (employee!=null && employeeRequest.getEmail()!=null) {
				Employee oldEmployee = adminConsoleDao.getEmployeeByEmail(employeeRequest.getEmail());
				if (oldEmployee!=null) {					
					int id = oldEmployee.getEmployeeId();
					employee.setEmployeeId(id);
					adminConsoleDao.saveEmployee(employee);
					AuditHelper.write(request, "Редактирование пользователя", "Редактирование пользователя "+employee.getEmployeeEmail()+
								". \nПользователь\n"+oldEmployee+"\n изменен на \n"+employee, AuditHelper.EMPLOYEE);
				}
			}
			return employeeRequest;
		case EditEmployeeRequest.DELETE:
			if (employeeRequest.getEmail()==null) {
				break;
			}
			employee = adminConsoleDao.getEmployeeByEmail(employeeRequest.getEmail());
			if (employee!=null) {
				adminConsoleDao.deleteEmployee(employee.getEmployeeId());
				AuditHelper.write(request, "Удаление пользователя", "Удаление пользователя \n"+employee, AuditHelper.EMPLOYEE);
			}
			return employeeRequest;
		}
		employeeRequest.setCode(Status.BAD_REQUEST);
		return employeeRequest;
	}

	private GetEmployeesRequest getEmployeeRequest(HttpServletRequest request, HttpServletResponse response,
			GetEmployeesRequest xmlRequest) {
		xmlRequest.setEmployeesList(adminConsoleDao.listEmployees());
		return xmlRequest;			
	}

	private boolean checkAccess(HttpServletRequest request, boolean requireRead) {
		AuthContext ctx = (AuthContext)request.getSession().getAttribute("user");
		if (ctx==null || ctx.getEmployee()==null) 
			return false;
		Employee employee = (Employee) ctx.getEmployee();
		return employee.isRemote() && (requireRead || !employee.isReadOnly());
	}
	
	private boolean checkAccessAndWriteCode(HttpServletRequest request, Message xmlRequest, boolean requireRead) {
		if (!checkAccess(request, requireRead)) {
			xmlRequest.setCode(Status.FORBIDDEN);
			return false;
		} else {
			xmlRequest.setCode(Status.OK);
			return true;			
		}
	}

	private Authentication authenticate(Authentication xmlRequest, HttpServletRequest request,HttpServletResponse response) {
		
		AuthContext authContext = adminConsoleDao.authenticate(xmlRequest.getLogin(), xmlRequest.getPassword());
		
		if (authContext.getStatus()==AuthContext.VALID_USER) {	
			Employee employee = (Employee) authContext.getEmployee();
			if (employee.isRemote()) {
				request.getSession().setAttribute("user", authContext);
				xmlRequest.setPassword("");
				xmlRequest.setCode(Status.OK);
				
				AuditHelper.write(request, "Вход в систему", "", AuditHelper.LOGIN);
				
				return xmlRequest;
			}	
			xmlRequest.setNoteMessage("user is not remote");
		} else
			xmlRequest.setNoteMessage("access denied");
		xmlRequest.setPassword("");
		xmlRequest.setCode(Status.UNAUTHORIZED);
		return xmlRequest;
	}

	private LogMessages getLogMessages(LogMessages msgs) {
		String tag = msgs.getTag();		
		List<LogAction> actions = TagBuffers.listActions(tag);
		List<LogAction> filtrated = new ArrayList<LogAction>();
		for (LogAction log: actions) {
			if (msgs.getDateFrom()!=null && msgs.getDateFrom().after(log.getDate())) {
				continue;
			}
			if (msgs.getDateTo()!=null && msgs.getDateTo().before(log.getDate())) {
				continue;
			}
			if (msgs.getSearchWithMsg()!="" && !StringUtils.containsIgnoreCase(log.getMsg(), msgs.getSearchWithMsg())) {
				continue;
			}
			filtrated.add(log);
		}
		int pages = filtrated.size()/logsOnPage+(filtrated.size()%logsOnPage>0?1:0);
		msgs.setMaxPage(pages);
		
		Integer page = msgs.getPage();
		if (page==null || page<0) 
			page = 1;
		int lastIndex = Math.min(filtrated.size(), logsOnPage*page);
		if (logsOnPage*(page-1)<filtrated.size()) {
			Collections.reverse(filtrated);			
			msgs.setLogs(filtrated.subList(logsOnPage*(page-1), lastIndex));
		} else
			msgs.setLogs(new ArrayList<LogAction>());				
		return msgs;
	}
	
	private LogsTags getLogsTags(LogsTags tags) {
		tags.setTags(TagBuffers.listTags());		
		return tags;
	}
	
	private Services getServices(HttpServletRequest request, HttpServletResponse response, Services services) {
		ServiceCommand command = services.getCommand();
		ServiceManager serviceManager = ServiceManager.getInstance();
		
		if (command == ServiceCommand.GET) {
			if (!checkAccessAndWriteCode(request, services, true))
				return services;
			ArrayList<Service> list = new ArrayList<Service>();
			List<String> folders = serviceManager.getServiceFolders();
			for (String folder: folders) {
				List<ServiceContainer> containers = serviceManager.getServiceContainerList(folder);
				for (ServiceContainer container: containers) {
					list.add(new Service(folder, container.getConfig().getCode(), container.getState()));				
				}
			}
			services.setServices(list);
			return services;	
		}		
		if (!checkAccessAndWriteCode(request, services, false))
			return services;
		if (services.getServices()==null || services.getServices().isEmpty()) {
			services.setCode(Status.FAILED);
			return services;
		}
		Service service = services.getServices().get(0);		
		try {
			switch (command) {
			case START: 
				if (serviceManager.startService(service.getFolder(), service.getCode())) {
					AuditHelper.write(request, "Запуск сервиса", service.getFolder()+"/"+service.getCode()+" запущен", AuditHelper.SERVICE);
				} else {
					services.setCode(Status.FAILED);
					return services;
				}
				break;
			case STOP: 
				if (serviceManager.stopService(service.getFolder(), service.getCode())) {
					AuditHelper.write(request, "Остановка сервиса", service.getFolder()+"/"+service.getCode()+" остановлен", AuditHelper.SERVICE);
				} else {
					services.setCode(Status.FAILED);
					return services;
				}
			}			
		} catch (ComponentException e) {
			tagLogger.log("Can't start/stop service "+service.getFolder()+"/"+service.getCode());
		}
		service.setState(serviceManager.getServiceState(service.getFolder(), service.getCode()));			
		return services;	
	}
	
	
	private Settings getSettings(HttpServletRequest request, HttpServletResponse response, Settings settings) {
		Settings.SettingCommand command = settings.getCommand();
		if (command == null) {
			settings.setCode(Status.BAD_REQUEST);
			return settings;
		}
		Map<String, Setting> props = adminConsoleDao.getProperties();
		
		if (settings.getCommand().equals(Settings.SettingCommand.GET)) {
			if (!checkAccessAndWriteCode(request, settings, true))
				return settings;
			
			settings.setSettings(props.values());
			return settings;
		} else {
			if (!checkAccessAndWriteCode(request, settings, false))
				return settings;
			
			Collection<Setting> propsToSet = settings.getSettings();
			String info = "";
			if (props!=null)
				for (Setting set: propsToSet) {
					String oldValue = props.get(set.getName()).getValue();
					if (!oldValue.equals(set.getValue())) {
						adminConsoleDao.updateProperty(set.getValue(), set.getName());
						if (set.getName().toLowerCase().contains("password")) {
							info+=set.getName()+" изменено";
						} else {
							info+=set.getName()+" изменено с "+oldValue+" на "+set.getValue()+'\n';
						}
					}
					
				}
			if (info.length()>0)
				AuditHelper.write(request, "Изменение настроек", info, AuditHelper.CONFIGURATION);
			settings.setSettings(Collections.<Setting> emptyList());		
			return settings;
		}
	}
	
}
