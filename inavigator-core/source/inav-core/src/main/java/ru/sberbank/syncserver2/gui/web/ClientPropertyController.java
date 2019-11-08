package ru.sberbank.syncserver2.gui.web;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import ru.sberbank.syncserver2.gui.data.ClientConfig;
import ru.sberbank.syncserver2.gui.data.CompleteAuditRecord;
import ru.sberbank.syncserver2.gui.data.SyncConfig;
import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;


@Controller
public class ClientPropertyController extends DatabaseController {
    public ClientPropertyController() {
        super(ClientPropertyController.class);
    }

    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
        String servletPath = HttpRequestUtils.getFulRequestPath(request);
        if(servletPath.contains("apps.clientproperties.gui") ){
            return showApplications(request,response,errors);
        } else if(servletPath.contains("versions.clientproperties.gui") ){
            return showVersions(request, response, errors);
        } else if(servletPath.contains("properties.clientproperties.gui") ){
            return showProperties(request, response, errors);
        } else if(servletPath.contains("copy.clientproperties.gui") ){
            return copyProperties(request, response, errors);
        } else {
            return new ModelAndView("index");
        }
    }

    protected ModelAndView showApplications(HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
        List apps = database.listClientApplications();
        ModelAndView mv = new ModelAndView("confserverAppList");
        mv.addObject("apps", apps);
        return mv;
    }

    protected ModelAndView showVersions(HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
        String appBundle = request.getParameter("appBundle");
        List versions = database.listClientAppVersions(appBundle);
        ModelAndView mv = new ModelAndView("confserverVersionList");
        mv.addObject("appBundle", appBundle);
        mv.addObject("versions" , versions);
        return mv;
    }

    protected ModelAndView showProperties(HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
        String appBundle = request.getParameter("appBundle");
        String appVersion = request.getParameter("appVersion");
        List properties = database.listClientProperties(appBundle, appVersion);
        ModelAndView mv = new ModelAndView("confserverPropList");
        mv.addObject("appBundle"  , appBundle);
        mv.addObject("appVersion" , appVersion);
        mv.addObject("properties" , properties);
        return mv;
    }

    protected ModelAndView copyProperties(HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
    	String webAppRootKey = getServletContext().getInitParameter("webAppRootKey");
        String appBundle = request.getParameter("appBundle");
        String fromAppVersion = request.getParameter("fromAppVersion");
        String toAppVersion = request.getParameter("toAppVersion");
        // Вытягиваем данные версии из которой копируем
        List<ClientConfig> properties = database.listClientProperties(appBundle, fromAppVersion);
        // Создаем новую версию, куда копируем
        database.insertClientAppVersion(appBundle, toAppVersion);
        // переносим все данные настроек
        for(int i=0;i<properties.size();i++) {
        	ClientConfig config =  properties.get(i);
	        String property = config.getPropertyKey();
	        String value = config.getPropertyValue();
	        database.updateClientProperty(appBundle, toAppVersion, property, value);
        }
        AuditHelper.write(request, "Создание настроек клиента", "Копирование настроек для "+appBundle+" из версии "+fromAppVersion+" в версию "+toAppVersion, AuditHelper.CLIENT_PROP);
        
        // возвращаемся в список версий
        return showVersions(request, response, errors);
    }
    
    protected ModelAndView onSubmit(
        HttpServletRequest request,
        HttpServletResponse response,
        Object command,
        BindException errors)
        throws Exception {
        String webAppRootKey = getServletContext().getInitParameter("webAppRootKey");
        String servletPath = HttpRequestUtils.getFulRequestPath(request);
        if(servletPath.contains("update.clientproperties.gui") ){
            String appBundle  = request.getParameter("appBundle");
            String appVersion = request.getParameter("appVersion");
            List<ClientConfig> properies = database.listClientProperties(appBundle,appVersion);
            for (int i = 0; i < properies.size(); i++) {
                ClientConfig config =  properies.get(i);
                String property = config.getPropertyKey();
                String value = request.getParameter(property);
                database.updateClientProperty(appBundle, appVersion, property, value.trim());
            }
            AuditHelper.write(request, "Изменение настроек клиента", "Изменение настроек для "+appBundle+" версии "+appVersion, AuditHelper.CLIENT_PROP);
            
            return showVersions(request, response, errors);
        } else if(servletPath.contains("add.clientproperties.gui") ){
            String appBundle  = request.getParameter("appBundle");
            String appVersion = request.getParameter("appVersion");
            database.insertClientAppVersion(appBundle, appVersion.trim());
            AuditHelper.write(request, "Добавление настроек клиента", "Добавление настроек для "+appBundle+" версии "+appVersion, AuditHelper.CLIENT_PROP);
            
            return showProperties(request, response, errors);
        } else if(servletPath.contains("delete.clientproperties.gui") ){
            String appBundle  = request.getParameter("appBundle");
            String appVersion = request.getParameter("appVersion");
            database.deleteClientAppVersion(appBundle, appVersion);
            AuditHelper.write(request, "Удаление настроек клиента", "Удаление настроек для "+appBundle+" версии "+appVersion, AuditHelper.CLIENT_PROP);
            
            return showVersions(request, response, errors);
        }
        return new ModelAndView("index");
    }

}
