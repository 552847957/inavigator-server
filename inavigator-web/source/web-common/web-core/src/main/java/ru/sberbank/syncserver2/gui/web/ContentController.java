package ru.sberbank.syncserver2.gui.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import ru.sberbank.syncserver2.gui.data.CompleteAuditRecord;
import ru.sberbank.syncserver2.gui.data.Employee;
import ru.sberbank.syncserver2.gui.data.MobileLogMode;
import ru.sberbank.syncserver2.gui.data.SyncConfig;
import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.gui.db.dao.GeneratorDao;
import ru.sberbank.syncserver2.service.pub.xml.LogEvent;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

public class ContentController extends ShowTableController{

	@Autowired
	private GeneratorDao generatorDao;
	
	public ContentController() {
		super(ContentController.class);
		numberOfColumns = 2;
	}

	@Override
	protected SearchResult search(HttpServletRequest request, int orderCol,
			int direction, List<String> searchValues, int startIndex,
			int numberOfMessages) {
		List<String[]> list = generatorDao.getUsersFromDraftGroup();
		final int dir = direction;
		Collections.sort(list, new Comparator<String[]>() {
			@Override
			public int compare(String[] o1, String[] o2) {
				return dir*o1[0].compareToIgnoreCase(o2[0]);
			}							
		});
		int size = list.size();
		int endIndex = Math.min(startIndex + numberOfMessages,size);
		
		if (endIndex>startIndex) 
			list = list.subList(startIndex, endIndex);
		else
			list = Collections.emptyList();
		SearchResult sr = new SearchResult(list, size,size);
		return sr;
	}

	@Override
	protected void generateFile(HttpServletRequest request,
			HttpServletResponse response, SearchResult searchResult) {
		
	}
	
	@Override
	protected ModelAndView showForm(HttpServletRequest request,
			HttpServletResponse response, BindException errors)
			throws Exception {
		String servletPath = HttpRequestUtils.getFulRequestPath(request);
        if(servletPath.contains("show.contentcontrol.gui") ){
            return showControllers(request, response, errors);
        } else if (servletPath.contains("delete.contentcontrol.gui")) {
        	return delete(request, response, errors);
        }
        return new ModelAndView("index");
	}
	
	protected ModelAndView showControllers(HttpServletRequest request,
			HttpServletResponse response, BindException errors) {
		if ("true".equals(request.getParameter("table"))) {
			transmit(request, response);
	        return null;
		}
		String sql = "SELECT DISTINCT APP_CODE FROM SYNC_CACHE_STATIC_FILES";
		List<String> apps = getDatabase().getStringList(sql);
		ModelAndView mv = new ModelAndView("listContentControllers");
		mv.addObject("apps",apps);
		return mv;
	}
	
	protected ModelAndView onSubmit(
	        HttpServletRequest request,
	        HttpServletResponse response,
	        Object command,
	        BindException errors)
	        throws Exception { 
		String app = request.getParameter("app");
		String email = request.getParameter("userEmail");
		generatorDao.addUserToDraftGroup(app, email);  
		AuditHelper.write(request, "Добавление контроллера контента", "Добавлен пользователь "+email+" для приложения "+app, AuditHelper.CONTENT_DRAFT_GROUP);
		response.setStatus(response.SC_OK);
		response.getWriter().write("Saved");
		return null;
	    }
	
    protected ModelAndView delete(
            HttpServletRequest request,
            HttpServletResponse response,
            BindException errors)
            throws Exception {
    	
		String app = request.getParameter("app");
		String email = request.getParameter("userEmail");
        if (email==null || app==null) {
        	logger.debug((email==null)?"Missing user Email - severe logic problem":"Missing user device - severe logic problem");
        	return new ModelAndView("index");
        }
        
        generatorDao.removeUserFromDraftGroup(app, email);        
        AuditHelper.write(request, "Удаление контроллера контента", "Удален пользователь "+email+" для приложения "+app, AuditHelper.CONTENT_DRAFT_GROUP);
		response.setStatus(response.SC_OK);
		response.getWriter().write("Saved");
        return null;
        }


}
