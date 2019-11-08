package ru.sberbank.syncserver2.gui.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import ru.sberbank.syncserver2.gui.data.CompleteAuditRecord;
import ru.sberbank.syncserver2.gui.data.MobileLogMode;
import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.gui.db.dao.MobileLogDao;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

public class MobileLogModeController extends ShowTableController{
	
	@Autowired
	private MobileLogDao mobileLogDao;
	
	public MobileLogModeController() {
		super(MobileLogModeController.class);
		numberOfColumns = 7;
	}
	
	@Override
	protected SearchResult search(HttpServletRequest request, int orderCol,
			int direction, List<String> searchValues, int startIndex,
			int numberOfMessages) {
		String email = request.getParameter("email");		
		String where = "WHERE USER_EMAIL = '"+email+"'"; 
		String order = "ORDER BY EVENT_TIME "+(direction>0?"ASC":"DESC");		
		String value = searchValues.get(2).trim();
		if (!value.equals("")) {
			where += " AND APP_VERSION LIKE '%"+value+"%'";
		}
		value = request.getParameter("model");
		if (!value.trim().equals("")) {
			where += " AND DEVICE_MODEL = '"+value+"'";
		}
		List<Object> args = new ArrayList<Object>(2);
		try {
			value = searchValues.get(0).trim();
			if (!value.equals("")) {
				Date date = DateFormat.getInstance().parse(value);
				args.add(date);
				where +=" AND EVENT_TIME >= ?";
			}
			value = searchValues.get(1).trim();
			if (!value.equals("")) {
				Date date = DateFormat.getInstance().parse(value);
				args.add(date);
				where +=" AND EVENT_TIME < ?";
			}
		} catch (ParseException e) {
		}
		
		
		List<Object[]> list = mobileLogDao.listLogEvents(where, order,args.toArray(), startIndex, numberOfMessages);
		int count = mobileLogDao.getLogsCount(where, args.toArray());
		SearchResult sr = new SearchResult(list, count,count);
		return sr;
	}

	@Override
	protected void generateFile(HttpServletRequest request,
			HttpServletResponse response, SearchResult searchResult) {
		//NtoDo
	}
	
	@Override
	protected ModelAndView showForm(HttpServletRequest request,
			HttpServletResponse response, BindException errors)
			throws Exception {
		String servletPath = HttpRequestUtils.getFulRequestPath(request);
        if(servletPath.contains("mode.mobile.gui") ){
            return showModes(request, response, errors);
        } else if(servletPath.contains("logs.mobile.gui") ){
            return showLogEvents(request, response, errors);
        } else if (servletPath.contains("delete.mobile.gui")) {
        	return delete(request, response, errors);
        } else if (servletPath.contains("add.mobile.gui")) {
        	return add(request, response, errors);
        } else if (servletPath.contains("clear.mobile.gui"))
        	return clearLogs(request, response, errors);
        return new ModelAndView("index");
	}

	private ModelAndView clearLogs(HttpServletRequest request,
			HttpServletResponse response, BindException errors) {
		String email = request.getParameter("email");
		mobileLogDao.clearLogs(email);
		if ("true".equals(request.getParameter("ajax")))
			return null;
		return new ModelAndView(UrlBasedViewResolver.REDIRECT_URL_PREFIX+"logs.mobile.gui?email="+email);
	}

	protected ModelAndView showModes(HttpServletRequest request,
			HttpServletResponse response, BindException errors) {
		
		List<MobileLogMode> modes = mobileLogDao.listLogModes();
		ModelAndView mv = new ModelAndView("MobileLogModes");
		mv.addObject("modes", modes);
		return mv;
	}
	
	protected ModelAndView showLogEvents(HttpServletRequest request,
			HttpServletResponse response, BindException errors) {
		if ("true".equals(request.getParameter("table"))) {
			transmit(request, response);
	        return null;
		}
		ModelAndView mv = new ModelAndView("listMobileLogs");
		List<String> models = mobileLogDao.getAllDeviceModels();
		List<String> emails = mobileLogDao.getAllEmails();
		mv.addObject("models", models);
		mv.addObject("emails",emails);
		return mv;
	}
	
	protected ModelAndView onSubmit(
	        HttpServletRequest request,
	        HttpServletResponse response,
	        Object command,
	        BindException errors)
	        throws Exception {    	
	        MobileLogMode mode = (MobileLogMode) command;
	        String sql = "exec SP_SYNC_MOBILE_SET_MODE ?,?,?";
			getDatabase().executePatternUnicode(sql, new Object[] {mode.getUserEmail(), mode.getDevice(), mode.getMode()}, null);	 
			String webAppRootKey = getServletContext().getInitParameter("webAppRootKey");
	        if ("true".equals(request.getParameter("json"))) {
	        	AuditHelper.write(request,"Изменение настроек логирования", 
	        			(mode.getMode()==0?"Выключен":"Включен")+" режим сохранения логов для пользователя "+mode.getUserEmail()+" с устройства "+mode.getDevice(), AuditHelper.MOBILE_MODES);
	        	PrintWriter out=null;
		        try {					        	
					out = response.getWriter();
					if (out!=null) {					
						JsonGenerator g = new JsonFactory().createJsonGenerator(out);					
						g.writeString("Сохранено");
						g.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
		            if (out != null) {
		                out.flush();
		                out.close();
		            }
		        }	
				return null;    		
	    	} else {	    		
	            AuditHelper.write(request, "Изменение настроек логирования", "Создание пользователя "+mode.getUserEmail()+" с устройством "+mode.getDevice(), AuditHelper.MOBILE_MODES);
	    		return new ModelAndView(UrlBasedViewResolver.REDIRECT_URL_PREFIX+"mode.mobile.gui");
	    	}	    			    
	    }
	
    protected ModelAndView delete(
            HttpServletRequest request,
            HttpServletResponse response,
            BindException errors)
            throws Exception {
    	
            String email = "";
            String device = "";
            email = request.getParameter("userEmail");
            device = request.getParameter("device");
            if (email==null || device==null) {            	
            	logger.debug((email==null)?"Missing user Email - severe logic problem":"Missing user device - severe logic problem");
                return new ModelAndView("index");
            }

            mobileLogDao.deleteMobileLogMode(email, device);
            AuditHelper.write(request, "Изменение настроек логирования", "Удаление пользователя "+email+" с устройством "+device, AuditHelper.MOBILE_MODES);
            return new ModelAndView(UrlBasedViewResolver.REDIRECT_URL_PREFIX+"mode.mobile.gui");
        }
    
    protected ModelAndView add(
            HttpServletRequest request,
            HttpServletResponse response,
            BindException errors)
            throws Exception {    	
            ModelAndView mv = new ModelAndView("addMobileMode");            
            return mv;
        }


}
