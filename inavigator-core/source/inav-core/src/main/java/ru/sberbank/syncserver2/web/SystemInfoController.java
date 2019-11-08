package ru.sberbank.syncserver2.web;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;
import ru.sberbank.syncserver2.service.core.config.MSSQLConfigLoader;
import ru.sberbank.syncserver2.service.core.config.SQLiteConfigLoader;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Created by sbt-kozhinsky-lb on 14.03.14.
 */
public class SystemInfoController extends BasicHtmlController{
    @RequestMapping(value = "/admin/system.info")
    public void showSystemInfo(HttpServletRequest request, HttpServletResponse response){
        String systemInfo = composeSystemInfo();
        showResponseInTable(response, systemInfo);
    }

    public static String composeSystemInfo(){
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
                    return resultSet.getString(1);
                }
            });
        } else if(configLoader instanceof SQLiteConfigLoader){
            databaseName = configLoader.getDatabaseName();
        }

        //2. Compose answer
        StringBuilder sb = new StringBuilder();
        sb.append("<table>");
        sb.append("<tr><td>Max Memory").append("</td><td>").append(maxMemory / 1024 / 1024).append("Mb</td></tr>");
        sb.append("<tr><td>Total Memory").append("</td><td>").append(totalMemory/1024/1024).append("Mb</td></tr>");
        sb.append("<tr><td>Free Memory").append("</td><td>").append(freeMemory/1024/1024).append("Mb</td></tr>");
        for (Iterator iterator = sizes.values().iterator(); iterator.hasNext(); ) {
            DiskSpaceInfo info = (DiskSpaceInfo) iterator.next();
            sb.append("<tr><td>").append(info.getRoot()).append("</td><td>")
              .append("Free Space: ").append(info.getFreeSpace()/1024/1024).append("Mb<br>")
              .append("Total Space: ").append(info.getTotalSpace()/1024/1024).append("Mb</td></tr>");
        }
        sb.append("</table>");
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

    public static void main(String[] args) {
        String s = composeSystemInfo();
        System.out.println(s);
    }

}
