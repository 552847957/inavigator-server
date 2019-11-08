package ru.sberbank.syncserver2.gui.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import ru.sberbank.syncserver2.gui.data.CompleteAuditRecord;
import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.DatabaseBackgroundProvider;
import ru.sberbank.syncserver2.service.core.MonitoredPublicService;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.ServiceState;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.BeanProperty;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;
import ru.sberbank.syncserver2.service.core.config.Folder;
import ru.sberbank.syncserver2.service.log.SynchronousDbLogService;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

/**
 * Created by sbt-kozhinsky-lb on 28.03.14.
 */
@Controller
public class ServiceAdminController extends ShowHtmlController {
    public ServiceAdminController() {
        super(ServiceAdminController.class);
    }

    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors)
            throws Exception {
        String servletPath = HttpRequestUtils.getFulRequestPath(request);
        if(servletPath.contains("folders.services.gui") ){
            return showFolders(request, response, errors);
        } else if(servletPath.contains("list.services.gui") ){
            return showServices(request, response, errors);
        } else if(servletPath.contains("command.services.gui") ){
            return command(request, response, errors);
        } else {
            return new ModelAndView("index");
        }
    }

    private ModelAndView showFolders(HttpServletRequest request, HttpServletResponse response, BindException errors) {
        //1. Listing folders
        String userName = HttpRequestUtils.getUsernameFromRequest(request);
        String ipAddress = HttpRequestUtils.getClientIpAddr(request);
        ServiceManager serviceManager = ServiceManager.getInstance();
        ConfigLoader configLoader = serviceManager.getConfigLoader();
        List<Folder> folders = configLoader.getFolders();
        Folder admin = new Folder("admin",-1,"Folder for built-in administrative services");
        folders.add(0, admin);

        //2. Display data
        ModelAndView mv = new ModelAndView("serviceFolders");
        mv.addObject("userName" , userName);
        mv.addObject("ipAddress", ipAddress);
        mv.addObject("folders"  , folders);
        return mv;
    }

    private ModelAndView showServices(HttpServletRequest request, HttpServletResponse response, BindException errors) {
        //1. Listing services
        String userName = HttpRequestUtils.getUsernameFromRequest(request);
        String ipAddress = HttpRequestUtils.getClientIpAddr(request);
        ServiceManager serviceManager = ServiceManager.getInstance();
        String folder = request.getParameter("folder");
        List<ServiceContainer> list = serviceManager.getServiceContainerList(folder);

        //2. Compose data
        //2. Display services, properties and start/stop commands
        StringBuilder sb = new StringBuilder();
        sb.append("<script src= \"js/service.admin.js\"> </script> ");
        for (int i = 0; i < list.size(); i++) {
            ServiceContainer container =  list.get(i);
            Bean config = container.getConfig();
            AbstractService service = container.getService();
            String stateString = composeState(container);
            sb.append("<tr id=").append(config.getCode()).append(">");
            sb.append("<td><a href='actions.logs.gui?tag=").append(config.getCode()).append("'>").append(config.getCode()).append("</a></td>");
            sb.append("<td>").append(config.getDescription()).append("</td>");
            sb.append("<td><span>").append(stateString).append("</span></td>");
            String props = composeBeanProperties(config.getBeanProperties());
            sb.append("<td><span>").append(props).append("</span></td>");

            if(service instanceof SingleThreadBackgroundService){
                sb.append("<td>").append("<a class='json' href='command.services.gui?folder="+folder+"&service="+config.getCode()+"&command=start'>Start</a>").append("</td>");
                sb.append("<td>").append("<a class='json' href='command.services.gui?folder="+folder+"&service="+config.getCode()+"&command=stop'>Stop</a>").append("</td>");
            } else {
                sb.append("<td colspan='2'>").append("<a class='json' href='command.services.gui?folder="+folder+"&service="+config.getCode()+"&command=start'>Reload properties</a>").append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("<span id=\"growl\"></span>");

        //3. Display response
        return showTable(sb.toString());
    }
    
    private static String composeState(ServiceContainer container) {
        ServiceState state = container.getState();
        String stateString = state.toString()+"<br/>";
        AbstractService service = container.getService();
        if (service instanceof DatabaseBackgroundProvider) {
        	DatabaseBackgroundProvider databaseBackgroundProvider = (DatabaseBackgroundProvider)service;
        	stateString = stateString + "<span " + ((!databaseBackgroundProvider.getLastConnectionStatus().get())?"class=\"warningMessage\"":"") + ">lastConnectionStatus: " + databaseBackgroundProvider.getLastConnectionStatus() + "</span><br/>"; 
        	stateString = stateString + "<span>lastDoRunFinish: " + databaseBackgroundProvider.getLastDoRunFinish() + "</span><br/>"; 
        }
        if (service instanceof MonitoredPublicService) {
        	MonitoredPublicService monitoredPublicService = (MonitoredPublicService)service;
        	stateString = stateString + "<span " + ((!monitoredPublicService.isStateValid())?"class=\"warningMessage\"":"") + ">lastRequestTime: " + new Date(monitoredPublicService.getLastRequestTime().get())  + "</span><br/>"; 
        }
        if(service instanceof SingleThreadBackgroundService && !(service instanceof SynchronousDbLogService)){
            SingleThreadBackgroundService backgroundService = (SingleThreadBackgroundService) service;
            stateString = stateString + "Runs every "+backgroundService.getWaitSeconds()+" seconds<br/>";
            stateString = stateString + "Last start time is "+new Date(backgroundService.getLastDoRunStart())+"<br/>";
            if(backgroundService.isInternalTaskRunning()){
                stateString = stateString + "Running now";
                String comment = service.getLastActionComment();
                if(comment!=null && comment.trim().length()>0){
                    stateString = stateString + ": "+comment;
                }
            } else {
                stateString = stateString + "Idle now";
            }
        } else {
            stateString = "Runs on request from other services";
        }    	
    	return stateString;
    }

    private ModelAndView command(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        //1. Listing services
        ServiceManager serviceManager = ServiceManager.getInstance();
        String folder = request.getParameter("folder");
        String service = request.getParameter("service");
        List<ServiceContainer> list = serviceManager.getServiceContainerList(folder);
        String command = request.getParameter("command");
        String hint="";
        String webAppRootKey = getServletContext().getInitParameter("webAppRootKey");
        
        //2. Get container and do action
        ServiceContainer container = serviceManager.getServiceContainerExt(folder, service);
        if(container==null){
            String html = "Service not found";
            return showText(html);
        } else if("stop".equalsIgnoreCase(command)){
            serviceManager.stopService(folder, service);
            hint=" остановлен";
            AuditHelper.write(request, "Остановка сервиса", folder+"/"+service+hint, AuditHelper.SERVICE);
        } else if("start".equalsIgnoreCase(command)){
            if (serviceManager.startService(folder, service)) {
            	hint=" запущен";
            	AuditHelper.write(request, "Запуск сервиса", folder+"/"+service+hint, AuditHelper.SERVICE);
            } else {
            	hint=" ошибка запуска";
            }            
        } else {
            return showText("Unexpected command : "+command+" only start or stop are supported");
            //showResponse(response, );
        }
        
        //3. Display service list
        if ("true".equals(request.getParameter("json"))) {
        	String s = composeBeanProperties(container.getConfig().getBeanProperties());
        	PrintWriter out=null;
	        try {					        	
				out = response.getWriter();
				if (out!=null) {					
					JsonGenerator g = new JsonFactory().createJsonGenerator(out);
					g.writeStartArray();
					g.writeString(service+hint);
					g.writeString(composeState(container));
					g.writeString(s);
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
        	return showServices(request, response,errors);
    }

    private static String composeBeanProperties(List<BeanProperty> properties){
        if(properties==null || properties.size()==0){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<b> Значения: </b><br/><table>");
        for (int i = 0; i < properties.size(); i++) {
            BeanProperty beanProperty =  properties.get(i);
            String code = beanProperty.getCode();
            String value = beanProperty.getValue();
            if(code.toLowerCase().contains("password")){
                value = "***";
            }
            if ((value != null) && (value.length()>100)) {
            	value=value.substring(0, 98)+"..";
            }
            sb.append("<tr><td><b>").append(code).append("</b>=").append(value!=null?value:"");
        }
        sb.append("</table><br/><br/>");

        sb.append("<b> Справка: </b><br/><table>");
        for (int i = 0; i < properties.size(); i++) {
            BeanProperty beanProperty =  properties.get(i);
            sb.append("<tr><td><b>").append(beanProperty.getCode()).append("</b> - ");
            sb.append(beanProperty.getDescription()).append("</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    /*
    @RequestMapping(value = "/admin/list.services")
    public void listServices(HttpServletRequest request, HttpServletResponse response){
        //1. Get list services
        String folder = request.getParameter("folder");
        List<ServiceContainer> list = serviceManager.getServiceContainerList(folder);

        showResponseInTable(response, sb.toString());
    }


    @RequestMapping(value = "/admin/command.services")
    public void startStopServices(HttpServletRequest request, HttpServletResponse response){
    }

     */
}
