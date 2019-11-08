package ru.sberbank.syncserver2.gui.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.gui.db.dao.PushNotificationDao;
import ru.sberbank.syncserver2.service.pushnotifications.model.DictionaryItem;
import ru.sberbank.syncserver2.service.pushnotifications.model.PushNotificationClient;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PushNotificationController extends ShowTableController {
	
	public PushNotificationController() {
		super(ShowHtmlController.class);
		numberOfColumns = 2;
	}

	private PushNotificationDao pushNotificationDao; 
	
	@Autowired
	public void setPushNotificationDao(PushNotificationDao pushNotificationDao) {
		this.pushNotificationDao = pushNotificationDao;
	}
	
	/**
	 * Алгоритм отправки push уведомления 
	 */
    @Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {

    		ModelAndView mv = new ModelAndView("pushForm");
	    	if (request.getParameter("data") != null)  { 
		    	String os = request.getParameter("os");
		    	String app = request.getParameter("app");
		    	String vers = request.getParameter("vers");
		    	String emails = request.getParameter("emails");
		    	String devices[] = request.getParameterValues("devices[]");
		    	String message = request.getParameter("message");
		    	String[] emailArray = (emails != null && !emails.equals(""))?new String[] {emails}:new String[]{};
		    	String temp = "Операционная система: "+os+" \n"
		    				+ "Приложение: "+app+" \n"
		    				+ "Версия приложения: "+vers+" \n"
		    				+ "Emails: "+emails+" \n"
		    				+ "Устройства: "+PushNotificationDao.decorateStringListForWhereInClause(devices)+" \n"
		    				+ "Сообщение: "+message;
		    	AuditHelper.write(request, "Добавлено push уведомление", temp, AuditHelper.PUSH_NOTIFICATION);
		    	int clientsCount = pushNotificationDao.addPushNotificationToQueue(message, os, app, vers, emailArray, devices);
		    	mv.addObject("message","Уведомление добавлено в очередь." + clientsCount + " пользователей системы получат сообщение в ближайшее время.");	    	}
	    	return mv;
	}

	@Override
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
        String servletPath = HttpRequestUtils.getFulRequestPath(request);
        // генерируем форму отправки сообщения
        if(servletPath.contains("sendform.push.gui") ){
        	return new ModelAndView("pushForm");
        	
        } 
        // Далее генерируем ответы в случае запроса справочников 
        else if (servletPath.contains("/push/list/os")) {
        	writeDictionaryResponse(response, pushNotificationDao.getOsCodes());
        	
        } else if (servletPath.contains("/push/list/emails")) {
        	writeDictionaryResponse(response, pushNotificationDao.getEmails());
        	
        } else if (servletPath.contains("/push/list/devices")) {
        	String email = request.getParameter("email")!= null?request.getParameter("email"):"";
        	writeDictionaryResponse(response, pushNotificationDao.getDevices(email));
        	
        } else if (servletPath.contains("/push/list/applications")) {
        	String os = request.getParameter("os")!= null?request.getParameter("os"):"";
        	writeDictionaryResponse(response, pushNotificationDao.getApplications(os));
        	
        } else if (servletPath.contains("/push/list/versions")) {
        	String os = request.getParameter("os")!= null?request.getParameter("os"):"";
        	String app = request.getParameter("app")!= null?request.getParameter("app"):"";
        	writeDictionaryResponse(response, pushNotificationDao.getApplicationVersions(os, app));
        	
        } else if(servletPath.contains("messages.push.gui") ){
        	if ("true".equals(request.getParameter("table")))
    			transmit(request, response); else 
    				return new ModelAndView("listPushMsg");   
        }
        return null;
    }
    
    /**
     * Записать в ответ данные справочников
     * @param response
     * @param data
     */
    public void writeDictionaryResponse(HttpServletResponse response,List<DictionaryItem> data) {
    	Map<String,Object> results = new HashMap<String, Object>();
    	results.put("results", data);
    	ObjectMapper mapper = new ObjectMapper();
    	try {
    	mapper.writeValue(response.getOutputStream(), results);
    	} catch (Exception ex) {
    		ex.printStackTrace();
    	}
    }

	@Override
	protected SearchResult search(HttpServletRequest request, int orderCol,
			int direction, List<String> searchValues, int startIndex,
			int numberOfMessages) {
		String notificationId = request.getParameter("NID");
		if (notificationId!=null && !notificationId.equals("")) {
			try {
				//считаем что notificationId задан, поэтому возвращаем список получателей
				long id = Long.valueOf(notificationId);
				List<PushNotificationClient> clients = pushNotificationDao.getClientsByNotification(id,startIndex,numberOfMessages);
				List<String[]> result = new ArrayList<String[]>();
				for (PushNotificationClient client: clients) {
					result.add(new String[]{client.getEmail(),client.getDeviceName(),client.getStatus()});
				}
				int count = pushNotificationDao.getClientsByNotificationCount(id);
				return new SearchResult(result, count,count);		
			} catch (NumberFormatException e) {}				
		}
		//считаем что notificationId не задан, возвращаем список сообщений
		List<String[]> list = pushNotificationDao.getNotificationsFromArch(startIndex, numberOfMessages);	
		int count = pushNotificationDao.getNotificationsCount();
		return new SearchResult(list, count, count);
	}

	@Override
	protected void generateFile(HttpServletRequest request,
			HttpServletResponse response, SearchResult searchResult) {		
	}
	
}	
