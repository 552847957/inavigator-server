package ru.sberbank.syncserver2.web;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.ServiceState;
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.BeanProperty;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

/**
 * Created by sbt-kozhinsky-lb on 26.02.14.
 */
public class AdminConsoleController extends BasicHtmlController{

    @RequestMapping(value = "/admin/list.folders.gui")
    public void listFolders(HttpServletRequest request, HttpServletResponse response) {
        ServiceManager serviceManager = ServiceManager.getInstance();
    	String userName = HttpRequestUtils.getUsernameFromRequest(request);
    	String ipAddress = HttpRequestUtils.getClientIpAddr(request);
        ServletOutputStream out = null;
        try {
            List<String> folders = serviceManager.getPublicFolders();
            out = response.getOutputStream();
            out.println("<html><body>");
            out.println("<h3> Authentication is done with X509Certificate = " + String.valueOf(userName) + " and ipAddress=" + ipAddress);
            out.println("</h3>");
            out.println("<table>");
            for (int i = 0; i < folders.size(); i++) {
                out.print("<tr><td>");
                String folder =  folders.get(i);
                out.print(folder);
                out.println("<td><a href=list.services.gui?folder="+folder+">Show Services</a></td></tr>");
                out.println("</td></tr>");
            }
            out.println("</table></body></html>");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(out!=null){
                try {
                    out.flush();
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @RequestMapping(value = "/admin/list.services.gui")
    public void listServices(HttpServletRequest request, HttpServletResponse response){
        //1. Get list services
        ServiceManager serviceManager = ServiceManager.getInstance();
        String folder = request.getParameter("folder");
        List<ServiceContainer> list = serviceManager.getServiceContainerList(folder);

        //2. Display services, properties and start/stop commands
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            ServiceContainer container =  list.get(i);
            Bean config = container.getConfig();
            ServiceState state = container.getState();
            sb.append("<tr>");
            sb.append("<td>").append(config.getCode()).append("</td>");
            sb.append("<td>").append(state).append("</td>");
            String props = composeBeanProperties(config.getBeanProperties());
            sb.append("<td>").append(props).append("</td>");
            sb.append("<td>").append("<a href='command.services.gui?folder="+folder+"&service="+config.getCode()+"&command=start'>Start</a>").append("</td>");
            sb.append("<td>").append("<a href='command.services.gui?folder="+folder+"&service="+config.getCode()+"&command=stop'>Stop</a>").append("</td>");
            sb.append("</tr>");
        }

        //3. Display response
        showResponseInTable(response, sb.toString());
    }

    private static String composeBeanProperties(List<BeanProperty> properties){
        if(properties==null || properties.size()==0){
            return "";
        }
        StringBuilder sb = new StringBuilder("<table>");
        for (int i = 0; i < properties.size(); i++) {
            BeanProperty beanProperty =  properties.get(i);
            sb.append("<tr><td>").append(beanProperty.getCode()).append("=").append(beanProperty.getValue()).append("</td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    @RequestMapping(value = "/admin/command.services.gui")
    public void startStopServices(HttpServletRequest request, HttpServletResponse response) throws Exception {
        //1. Parse param
        String folder  = request.getParameter("folder");
        String service = request.getParameter("service");
        String command = request.getParameter("command");

        //2. Get container
        ServiceManager serviceManager = ServiceManager.getInstance();
        ServiceContainer container = serviceManager.getServiceContainerExt(folder, service);
        if(container==null){
            showResponse(response, "Service not found");
            return;
        } else if("stop".equalsIgnoreCase(command)){
            container.stopService();
        } else if("start".equalsIgnoreCase(command)){
            container.startService();
        } else {
            showResponse(response, "Unexpected command : "+command+" only start or stop are supported");
        }

        //3. Display service list
        listServices(request, response);
    }

}
