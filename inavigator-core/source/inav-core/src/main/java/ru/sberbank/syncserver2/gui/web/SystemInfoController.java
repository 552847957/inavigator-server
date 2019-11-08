package ru.sberbank.syncserver2.gui.web;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import ru.sberbank.syncserver2.gui.data.CompleteAuditRecord;
import ru.sberbank.syncserver2.gui.db.AuditHelper;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;
import ru.sberbank.syncserver2.service.core.config.MSSQLConfigLoader;
import ru.sberbank.syncserver2.service.core.config.SQLiteConfigLoader;
import ru.sberbank.syncserver2.service.generator.single.SingleGeneratorService;
import ru.sberbank.syncserver2.service.monitor.check.AbstractCheckAction;
import ru.sberbank.syncserver2.util.FileCopyHelper;
import ru.sberbank.syncserver2.util.HttpRequestUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.File;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Created by sbt-kozhinsky-lb on 28.03.14.
 */
public class SystemInfoController extends ShowHtmlController {
    public SystemInfoController() {
        super(SystemInfoController.class);
    }

    @Override
    protected ModelAndView showForm(HttpServletRequest request, HttpServletResponse response, BindException errors) throws Exception {
        String servletPath = HttpRequestUtils.getFulRequestPath(request);
        String webAppRootKey = getServletContext().getInitParameter("webAppRootKey");
        if(servletPath.contains("show.systeminfo.gui") ){
            return showSystemInfo();
        } else if(servletPath.contains("filelist.systeminfo.gui") ){
            String folder = request.getParameter("folder");
            return showFileList(folder);
        } else if(servletPath.contains("mkdir.systeminfo.gui") ){
            String folder = request.getParameter("folder");
            File file = new File(folder);            
            file.mkdirs();
            AuditHelper.write(request, "Создание папки", "Создание папки "+folder, AuditHelper.SYSTEM_INFO);
            
            return new ModelAndView(UrlBasedViewResolver.REDIRECT_URL_PREFIX+"show.systeminfo.gui");
        } else if(servletPath.contains("copy.systeminfo.gui") ){
            String src = request.getParameter("src");
            String dst = request.getParameter("dst");
            FileCopyHelper.reliableCopy(new File(src), new File(dst));
            AuditHelper.write(request, "Копирование файла", "Копирование файла "+src+" в "+dst, AuditHelper.SYSTEM_INFO);
            
            return new ModelAndView(UrlBasedViewResolver.REDIRECT_URL_PREFIX+"show.systeminfo.gui");
        } else if(servletPath.contains("delete.systeminfo.gui") ){
            String src = request.getParameter("src");
            FileCopyHelper.reliableDelete(new File(src));
            AuditHelper.write(request, "Удаление файла", "Удаление файла "+src, AuditHelper.SYSTEM_INFO);
            
            return new ModelAndView(UrlBasedViewResolver.REDIRECT_URL_PREFIX+"show.systeminfo.gui");
        } else if(servletPath.contains("rmdir.systeminfo.gui") ){
            String folder = request.getParameter("folder");
            FileCopyHelper.reliableDeleteFolderAndSubFolders(new File(folder));
            AuditHelper.write(request, "Удаление папки", "Удаление папки "+folder, AuditHelper.SYSTEM_INFO);
            
            return new ModelAndView(UrlBasedViewResolver.REDIRECT_URL_PREFIX+"show.systeminfo.gui");
        } else if(servletPath.contains("generator.systeminfo.gui") && isGenerator()){
            SingleGeneratorService singleGeneratorService = (SingleGeneratorService) ServiceManager.getInstance().findFirstServiceByClassCode(SingleGeneratorService.class);
            if(singleGeneratorService!=null){
                singleGeneratorService.redeployConfigFiles(request);
                AuditHelper.write(request, "Копирование файлов настроек", "Копирование ETL файлов", AuditHelper.SYSTEM_INFO);
            }
            return new ModelAndView(UrlBasedViewResolver.REDIRECT_URL_PREFIX+"show.systeminfo.gui");
        } else {
        	return new ModelAndView(UrlBasedViewResolver.REDIRECT_URL_PREFIX+"show.systeminfo.gui");
        }
    }

    private ModelAndView showFileList(String folder) {
        //1. Declaring and listing files
        StringBuilder sb = new StringBuilder();
        File[] files = new File(folder).listFiles();

        //2, Composing
        if(files!=null){
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                String name = file.getAbsolutePath();
                String encoded = URLEncoder.encode(name);
                sb.append("<tr><td>");
                if(file.isDirectory()){
                    sb.append("<a href ='filelist.systeminfo.gui?folder=").append(encoded)
                    .append("'>").append(name).append("</a>");
                } else {
                    sb.append(name);
                }
                sb.append("</td><td>").append(file.length()).append("</td>");
                sb.append("</td><td>").append(new Date(file.lastModified())).append("</td></tr>");
            }
        }
        String text = sb.toString();
        return showTable(text);
    }

    private ModelAndView showSystemInfo() {
        String systemInfo = composeSystemInfo();
        return showText(systemInfo);
    }

    public String composeSystemInfo(){
        //1. Prepare information
        Runtime r = Runtime.getRuntime();
        long totalMemory = r.totalMemory();
        long maxMemory   = r.maxMemory();
        long freeMemory  = r.freeMemory();
        TreeMap sizes = new TreeMap();
        File[] roots = File.listRoots();
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            DiskSpaceInfo info = new DiskSpaceInfo(root);
            sizes.put(root.getAbsolutePath(), info);
        }

        //2. Getting database name
        String databaseName = "UNDEFINED";
        ServiceManager sm = ServiceManager.getInstance();
        ConfigLoader configLoader = sm==null ? null:sm.getConfigLoader();
        if(configLoader instanceof MSSQLConfigLoader){
            databaseName = configLoader.getValue("SELECT db_name()+'@'+@@servername", new ResultSetExtractor<String>() {
                @Override
                public String extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                    if(resultSet.next()){
                        return resultSet.getString(1);
                    } else {
                        return "";
                    }
                }
            });
        } else if(configLoader instanceof SQLiteConfigLoader){
            databaseName = configLoader.getDatabaseName();
        }

        //3. Compose answer
        StringBuilder sb = new StringBuilder();
        sb.append("<table border=1>");
        sb.append("<tr><td>Host Name").append("</td><td>").append(AbstractCheckAction.LOCAL_HOST_NAME).append("</td></tr>");
        sb.append("<tr><td>Database Name").append("</td><td>").append(databaseName).append("</td></tr>");
        sb.append("<tr><td>Max Memory").append("</td><td>").append(maxMemory / 1024 / 1024).append("Mb</td></tr>");
        sb.append("<tr><td>Total Memory").append("</td><td>").append(totalMemory/1024/1024).append("Mb</td></tr>");
        sb.append("<tr><td>Free Memory").append("</td><td>").append(freeMemory/1024/1024).append("Mb</td></tr>");
        for (Iterator iterator = sizes.values().iterator(); iterator.hasNext(); ) {
            DiskSpaceInfo info = (DiskSpaceInfo) iterator.next();
            String link = "<a href='filelist.systeminfo.gui?folder="+ URLEncoder.encode(info.getRoot())+"'>"+info.getRoot()+"</a>";
            sb.append("<tr><td>").append(link).append("</td><td>")
                    .append("Free Space: ").append(info.getFreeSpace()/1024/1024).append("Mb<br>")
                    .append("Total Space: ").append(info.getTotalSpace()/1024/1024).append("Mb</td></tr>");
        }
        sb.append("</table>");

        //3. Adding forms for file operations
        sb.append("<table border=0><tr><td align='center'><h5>File Copy Form</h5></td>");
        sb.append("<td align='center'><h5>File Delete Form</h5></td>");
        sb.append("<td align='center'><h5>Create Folder Form</h5></td>");
        sb.append("<td align='center'><h5>Delete Folder Form</h5></td></tr>");

        //3. Adding form for file copy
        sb.append("<tr><td valign='top'>");
        sb.append("<form action='copy.systeminfo.gui' method='GET'>");
        sb.append("<table border=0 cellspacing=10>");
        sb.append("<tr><td>Source File</td><td><input type='text' name='src' value=''/></tr>");
        sb.append("<tr><td>Destination File</td><td><input type='text' name='dst' value=''/></td></tr>");
        sb.append("<tr><td align='center' colspan=2><input type='submit' value='Copy'></td></tr>");
        sb.append("</table>");
        sb.append("</form>");

        sb.append("</td><td valign='top'>");
        sb.append("<form action='delete.systeminfo.gui' method='GET'>");
        sb.append("<table border=0 cellspacing=10>");
        sb.append("<tr><td>File</td><td><input type='text' name='src' value=''/></tr>");
        sb.append("<tr><td align='center' colspan=2><input type='submit' value='Delete'></td></tr>");
        sb.append("</table>");
        sb.append("</form>");

        sb.append("</td><td valign='top'>");
        sb.append("<form action='mkdir.systeminfo.gui' method='GET'>");
        sb.append("<table border=0 cellspacing=10>");
        sb.append("<tr><td>Folder</td><td><input type='text' name='folder' value=''/></td></tr>");
        sb.append("<tr><td align='center' colspan=2><input type='submit' value='Create Folder'></td></tr>");
        sb.append("</table>");
        sb.append("</form>");

        sb.append("</td><td valign='top'>");
        sb.append("<form action='rmdir.systeminfo.gui' method='GET'>");
        sb.append("<table border=0 cellspacing=10>");
        sb.append("<tr><td>Folder</td><td><input type='text' name='folder' value=''/></td></tr>");
        sb.append("<tr><td align='center' colspan=2><input type='submit' value='Delete Folder'></td></tr>");
        sb.append("</table>");
        sb.append("</form>");
        sb.append("</td></tr></table>");

        if(isGenerator()){
            sb.append("<br><br><table><tr><td>ETL Redeploy Form</td></tr>");
            sb.append("<tr><td><input type='button' onclick=\"javascript:window.location='generator.systeminfo.gui'\" value='Redeploy ETL'/></td></tr>");
            sb.append("</table>");
        }
        return sb.toString();
    }

    private static class DiskSpaceInfo {
        private String root;
        private long totalSpace;
        private long freeSpace;

        private DiskSpaceInfo(File root) {
            this.root = root.getAbsolutePath();
            this.totalSpace = root.getTotalSpace();
            this.freeSpace = root.getFreeSpace();
        }

        private DiskSpaceInfo(String root) {
            this(new File(root));
        }

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }

        public long getTotalSpace() {
            return totalSpace;
        }

        public void setTotalSpace(long totalSpace) {
            this.totalSpace = totalSpace;
        }

        public long getFreeSpace() {
            return freeSpace;
        }

        public void setFreeSpace(long freeSpace) {
            this.freeSpace = freeSpace;
        }
    }

    private boolean isGenerator(){
        ServletContext context = getServletContext();
        String contextName = context.getInitParameter("webAppRootKey");
        return "generator".equalsIgnoreCase(contextName);
    }

    public static void main(String[] args) {
        //String s = composeSystemInfo();
        //System.out.println(s);
    }
}
