package ru.sberbank.syncserver2.service.file;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.PublicService;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfo;
import ru.sberbank.syncserver2.service.file.cache.data.FileInfoList;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Admin on 20.05.14.
 */
public class FileServiceRenamer extends AbstractService implements PublicService {
    private ServiceManager serviceManager;
    private Map renames = new HashMap();

    @Autowired
    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public void setServiceManager(ServiceManager serviceManager) {
        this.serviceManager = serviceManager;
    }

    @Override
    public void request(HttpServletRequest request, HttpServletResponse response) {
        //1. Parsing params
        String command = request.getParameter("command");
        String src     = request.getParameter("src");
        String dst     = request.getParameter("dst");

        //2. Processing list
        ServletOutputStream output = null;
        if("list".equalsIgnoreCase(command)){
            StringBuilder sb = new StringBuilder();
            sb.append("<table><tr><th>Source Identifier</th><th>Dest identifier</th></tr>");
            for (Iterator iterator = renames.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry entry = (Map.Entry) iterator.next();
                sb.append("<tr>").append("<td>").append(entry.getKey()).append("</td>").append(entry.getValue()).append("</td></tr>");
            }
            sb.append("</table>");
            try {
                output = response.getOutputStream();
                output.print(sb.toString());
                output.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.closeQuietly(output);
            }
        } else if("add".equalsIgnoreCase(command)){
            renames.put(src,dst);
            try {
                response.sendRedirect("rename.do?command=list");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void doStop() {
        renames.clear();
    }

    @Override
    protected void waitUntilStopped() {
    }

    public FileInfoList renameList(FileInfoList src){
        FileInfoList dst = (FileInfoList) src.clone();
        List<FileInfo> statuses = dst.getReportStatuses();
        for (int i = 0; i < statuses.size(); i++) {
            FileInfo fileInfo =  statuses.get(i);
            String id = fileInfo.getId();
            String newId = (String) renames.get(id);
            if(StringUtils.isNotBlank(newId)){
                fileInfo.setId(newId);
            }
        }
        return  dst;
    }

    public FileServiceRequest renameRequest(FileServiceRequest fsRequest){
        if(FileServiceRequest.COMMANDS.DATA==fsRequest.getCommand()){
            String id = fsRequest.getId();
            String newId = (String) renames.get(id);
            if(StringUtils.isNotBlank(newId)){
                fsRequest.setId(newId);
            }
            return fsRequest;
        } else { //i.e. COMMANDS.LIST or COMMANDS.UNKNOWN
            return fsRequest;
        }
    }

    public String renameTitle(String newId, String title){
        //1. Finding string between id= and following space
        int start = title.indexOf(" id=");
        int end = title.indexOf(" ",start+4);
        String newTitle = title.substring(0, start)+newId+title.substring(end);
        System.out.println("Renamed title from '"+title+"' to '"+newTitle+"'");
        return newTitle;
    }
}


