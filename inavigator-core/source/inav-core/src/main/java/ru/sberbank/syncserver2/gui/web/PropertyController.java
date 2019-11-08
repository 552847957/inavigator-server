package ru.sberbank.syncserver2.gui.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import ru.sberbank.syncserver2.gui.data.SyncConfig;
import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;


@Controller
public class PropertyController extends DatabaseController {
    public PropertyController() {
        super(PropertyController.class);
    }

    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
        String servletPath = HttpRequestUtils.getFulRequestPath(request);
        if(servletPath.contains("list.properties.gui") ){
            return showListForm(request,response,errors);
        } else {
            return new ModelAndView("index");
        }
    }

    protected ModelAndView showListForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
        List<SyncConfig> properties = database.listProperties();
        Map<String, List<SyncConfig>> map = new TreeMap<String, List<SyncConfig>>();
        for (SyncConfig config: properties) {
        	if (!map.containsKey(config.getPropertyGroup())) {
        		map.put(config.getPropertyGroup(), new ArrayList<SyncConfig>());
        	}
        	map.get(config.getPropertyGroup()).add(config);
        }        
        ModelAndView mv = new ModelAndView("propertyList");
        mv.addObject("properties", map);
        return mv;
    }

    protected ModelAndView onSubmit(
        HttpServletRequest request,
        HttpServletResponse response,
        Object command,
        BindException errors)
        throws Exception {    	
        SyncConfig prop = (SyncConfig) command;
        String propertyKey = prop.getPropertyKey();
        String propertyValue = prop.getPropertyValue();
        String sql = "SELECT PROPERTY_VALUE FROM SYNC_CONFIG WHERE PROPERTY_KEY='"+propertyKey+"'";
        String oldValue = database.getStringValue(sql);
        database.updateProperty(propertyValue,propertyKey); 
        AuditHelper.write(request, "Изменение настроек", AuditHelper.composeEditMsg(propertyKey, oldValue, propertyValue), AuditHelper.CONFIGURATION);
        if ("true".equals(request.getParameter("json"))) {
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
    	} else
    		return showListForm(request,response,errors);
    }

}
