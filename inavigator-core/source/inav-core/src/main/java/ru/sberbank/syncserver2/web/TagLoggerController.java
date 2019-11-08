package ru.sberbank.syncserver2.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import ru.sberbank.syncserver2.service.log.LogAction;
import ru.sberbank.syncserver2.service.log.TagBuffers;
import ru.sberbank.syncserver2.service.core.ServiceManager;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 27.02.2014
 * Time: 2:39:08
 * To change this template use File | Settings | File Templates.
 */
public class TagLoggerController extends BasicHtmlController{

    @RequestMapping(value = "/admin/list.tags")
    public void listTags(HttpServletRequest request, HttpServletResponse response){
        //1. Get list of all tags
        ServiceManager serviceManager = ServiceManager.getInstance();
        List<String> tags = TagBuffers.listTags();
        List services = serviceManager.getAllServiceCodes();
        ServiceFirstComparator comparator = new ServiceFirstComparator(services);
        Collections.sort(tags, comparator);

        //2. Display tag list
        StringBuilder sb = new StringBuilder(2000);
        for (int i = 0; i < tags.size(); i++) {
            String t =  tags.get(i);
            sb.append("<tr>")
                    .append("<td>").append(t).append("</td>")
                    .append("<td>").append("<a href='actions.tags?tag=").append(t).append("'>View Actions</a></td>")
              .append("</tr>");
        }

        //3. Show result
        System.out.println("TAG LIST : "+sb.toString());
        showResponseInTable(response, sb.toString());
    }

    @RequestMapping(value = "/admin/actions.tags")
    public void listTagActions(HttpServletRequest request, HttpServletResponse response){
        //1. Get list of all actions
        String tag = request.getParameter("tag");
        List<LogAction> actions = TagBuffers.listActions(tag);

        //2. Display tag list
        StringBuilder sb = new StringBuilder(2000);
        for (int i = 0; i < actions.size(); i++) {
            String a =  actions.get(i).toString();
            sb.append("<tr><td>").append(a).append("</td></tr>");
        }

        //3. Show response
        showResponseInTable(response, sb.toString());
    }

    private static class ServiceFirstComparator implements Comparator {
        private Set services;

        private ServiceFirstComparator(List<String> services) {
            this.services = new HashSet(services);
        }

        @Override
        public int compare(Object o1, Object o2) {
            return  compare((String)o1, (String)o2);
        }

        public int compare(String s1, String s2) {
            s1 = services.contains(s1) ? "1.services."+s1:"2.files"+s1;
            s2 = services.contains(s2) ? "1.services."+s1:"2.files"+s2;
            return s1.compareTo(s2);
        }
    }

}
