package ru.sberbank.syncserver2.web;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.jndi.JndiObjectFactoryBean;
import org.springframework.web.context.support.WebApplicationContextUtils;
import ru.sberbank.syncserver2.service.config.ConfigService;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.service.core.PublicService;
import ru.sberbank.syncserver2.service.core.ResponseError;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.AbstractConfigLoader;
import ru.sberbank.syncserver2.service.core.config.MSSQLConfigLoader;
import ru.sberbank.syncserver2.service.core.config.SQLiteConfigDataSource;
import ru.sberbank.syncserver2.service.core.config.SQLiteConfigLoader;
import ru.sberbank.syncserver2.service.file.FileService;
import ru.sberbank.syncserver2.service.log.LogMsgComposer;
import ru.sberbank.syncserver2.service.log.TagLogger;
import ru.sberbank.syncserver2.service.security.LdapUserCheckerServiceGroup;
import ru.sberbank.syncserver2.service.sql.SQLService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.util.ExecutionTimeProfiler;
import ru.sberbank.syncserver2.util.FormatHelper;
import ru.sberbank.syncserver2.util.HttpRequestUtils;
import ru.sberbank.syncserver2.util.XMLHelper;

import javax.naming.NamingException;
import javax.security.auth.x500.X500Principal;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by sbt-kozhinsky-lb on 09.06.14.
 */
public class SyncDispatchServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;


	private ServiceManager serviceManager;
    private TagLogger tagLogger;
    private Logger logger;
    private final Logger traceOnlineSql = Logger.getLogger("trace.syncserver.online.sql");
    private ConcurrentMap<String,PublicService> configServices 	= new ConcurrentHashMap<String, PublicService>();
    private ConcurrentMap<String,PublicService>  fileServices      = new ConcurrentHashMap<String, PublicService>();
    private ConcurrentMap<String,SQLService>  sqlServices        = new ConcurrentHashMap<String, SQLService>();
    private LdapUserCheckerServiceGroup ldapUserCheckerService;
    //private ApplicationContext appContext;

    public SyncDispatchServlet() {
    }

    @Override
    public void init() throws ServletException {
        //appContext =
        //        WebApplicationContextUtils.getRequiredWebApplicationContext(
        //                this.getServletContext());
        //1. Get startup parameters
        super.init();
        ServletContext servletContext = getServletContext();
        String webAppRootKey      = servletContext.getInitParameter("webAppRootKey");
        String configLoaderClass  = servletContext.getInitParameter("configLoaderClass");
        LogMsgComposer.setWebAppName(webAppRootKey);

        //2. Initialising tagLogger
        logger = Logger.getLogger(SyncDispatchServlet.class);
        tagLogger = TagLogger.getTagLogger(SyncDispatchServlet.class, logger);

        //3. Connecting to the database
        DataSource dataSource = null;
        AbstractConfigLoader configLoader = null;
        if(MSSQLConfigLoader.class.getName().equalsIgnoreCase(configLoaderClass)){
            //3.1. Connect to MSSQL
            String configJNDI         = servletContext.getInitParameter("configJNDI");
            logger.info("Start connecting to "+configJNDI);
            for(int w=0; w<4; w++){
                /**
                 * Websphere JNDI does not need java:comp/env/ and Tomcat need not
                 * At first attempt we try to read WebSphere JNDI and ignore exception
                 * At second attempt we try to read Tomcat JNDI
                 * At third attempt we try to read WebSphere JNDI and print exception
                 * At forth attempt we try to read Jboss/WildFly and print exception (prefix java:jboss/datasources/)
                 */
                String prefix;
                String comment;
                switch (w) {
                    case 1: {
                        prefix = "java:comp/env/";
                        comment = "Tomcat";
                        break;
                    }
                    case 3: {
                        prefix = "java:jboss/datasources/";
                        comment = "Jboss/WildFly";
                        break;
                    }
                    default: {
                        prefix = "jdbc/";
                        comment = "Websphere";
                        break;
                    }
                }
                try {
                    JndiObjectFactoryBean factory = new JndiObjectFactoryBean();
                    factory.setJndiName(prefix+configJNDI);
                    factory.setExpectedType(javax.sql.DataSource.class);
                    factory.afterPropertiesSet();
                    dataSource = (DataSource) factory.getObject();
                    logger.info("Finish connecting to "+configJNDI+" as for "+comment + " with success");
                    break;
                } catch (NamingException e) {
                    logger.info("Finish connecting to " + configJNDI+" as for "+comment+ " with error");
                    if(w>0){
                        e.printStackTrace();
                    }
                }
            }

            //3.2. Initialising config loader
            logger.info("Start loading configuration for MSSQL");
            configLoader = new MSSQLConfigLoader();
            configLoader.setDataSource(dataSource);
            configLoader.init();
            logger.info("Finish loading configuration for MSSQL");
        } else if(SQLiteConfigLoader.class.getName().equalsIgnoreCase(configLoaderClass)){
            //3.3. Connecting to SQLite
            SQLiteConfigDataSource sqliteDataSource = new SQLiteConfigDataSource();
            String webInfClasses = super.getServletContext().getRealPath("WEB-INF/classes");
            String configFileName = servletContext.getInitParameter("configFileName");
            File configFile = new File(webInfClasses, configFileName);
            System.out.println("CONFIG FILE NAME IS "+configFileName);
            System.out.println("WEB-INF/classes: "+webInfClasses);
            sqliteDataSource.setDriverClassName("org.sqlite.JDBC");
            sqliteDataSource.setDbFilename(configFile.getAbsolutePath());
            sqliteDataSource.init();
            dataSource = sqliteDataSource;

            //3.4. Initialising config loader
            logger.info("Start loading configuration for SQLite");
            configLoader = new SQLiteConfigLoader();
            configLoader.setDataSource(dataSource);
            configLoader.init();
            logger.info("Finish loading configuration for SQLite");
        } else {
            String err = "Unexpected configLoader: "+configLoaderClass
                       + "It should be either "+MSSQLConfigLoader.class.getName()
                       + " or "+SQLiteConfigLoader.class.getName();
            System.out.println(err);
        }

        //4. Initialising ServiceManager
        serviceManager = new ServiceManager();
        serviceManager.setServletContext(servletContext);
        serviceManager.setConfigLoader(configLoader);
        serviceManager.setConfigSource(dataSource);
        try {
            serviceManager.startAll();
        } catch (ComponentException e) {
            e.printStackTrace();
        }
        ldapUserCheckerService = (LdapUserCheckerServiceGroup) serviceManager.findFirstServiceByClassCode(LdapUserCheckerServiceGroup.class);
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            ExecutionTimeProfiler.start("SyncDispatchServlet.service");

            if (traceOnlineSql.isTraceEnabled())
                traceOnlineSql.trace(FormatHelper.stringConcatenator("SyncDispatchServlet.service: request = ", request));

            //1. Extracting certificate information
            try {
                X509Certificate certs[] = (X509Certificate[])request.getAttribute("javax.servlet.request.X509Certificate");
                if(certs!=null){
                    X509Certificate clientCert = certs[0];
                    X500Principal subjectDN = clientCert.getSubjectX500Principal();
                    String userEmail = extractEmailAddress(subjectDN);
                    HttpRequestUtils.setUsernameToRequest(request, userEmail);
                    if (ldapUserCheckerService!=null && !ldapUserCheckerService.checkCachedUserCertByEmail(userEmail, clientCert)) {
                        logError(response, "User " + userEmail + " certificate is INVALID", null);
                        return;
                    }
                    //System.out.println("EMAIL = " + userEmail);
                }
            } catch (Exception e) {
                e.printStackTrace();
                logError(response, "User certificate is INVALID", e);
                return;
            }


            //2. Parsing folder and sevletCode
            String folder = null;
            String servletCode = null;
            String servletPath = null;
            try {
                servletPath = request.getServletPath();
                int index2 = servletPath.lastIndexOf('/');
                int index1 = servletPath.lastIndexOf('/', index2-1);
                folder = servletPath.substring(index1+1,index2);
                servletCode = servletPath.substring(index2+1);
            } catch (Exception e) {
                logError(response, "Failed to parse servlet code: " + servletPath, e);
                return;
            }

            //3. Dispatching and processing
            if(servletCode.endsWith("file.do")){
                if(ldapUserCheckerService!=null){
                    serviceFileRequest(folder,servletCode,request,response);
                } else {
                    logError(response, "LDAP is not configured",null);
                    return;
                }
            } else if(servletCode.endsWith("file.preview")){
                if(ldapUserCheckerService!=null){
                    serviceFileRequest(folder,"file.do",request,response);
                } else {
                    logError(response, "LDAP is not configured",null);
                    return;
                }
            } else if(servletCode.endsWith("config.do")){
                serviceConfigRequest(folder, servletCode, request, response);
            } else if(servletCode.endsWith("online.sql")){
                serviceSqlRequest(folder, servletCode, request, response);
            } else if(servletCode.endsWith("profiler.do")){
                serviceProfilerRequest(folder, servletCode, request, response);
            } else if(servletCode.endsWith("ping.do")){
                serviceDoRequest(folder, servletCode, request, response);
            } else if(servletCode.endsWith("upload.do")){
            	serviceDoRequest(folder, servletCode, request, response);
            } else {
                serviceDoRequest(folder, servletCode, request, response);
            }
        } finally {
            ExecutionTimeProfiler.finish("SyncDispatchServlet.service");
        }
    }

    private void serviceFileRequest(String folder, String servletCode, HttpServletRequest request, HttpServletResponse response){
        try {
            // 1. Get public service and send request to it
            ExecutionTimeProfiler.start("serviceFileRequest");
            FileService fileService = (FileService) fileServices.get(folder);
            if(fileService==null){
                fileService = (FileService) serviceManager.getPublicService(folder, servletCode);
                if (fileService == null) {
                    String msg = "NO SERVICE FOUND in folder " + folder + " and servletCode " + servletCode;
                    logError(response, msg, null);
                    return;
                } else {
                    fileServices.putIfAbsent(folder, fileService);
                }
            }

            // 2. Processing request
            fileService.request(request, response);
        } catch (Throwable th) {
            logError(response, "Unexpected exception in " + SyncDispatchServlet.class.getSimpleName(), th);
        } finally {
            ExecutionTimeProfiler.finish("serviceFileRequest");
            //manualGCHelper.action();
        }
    }



    private void serviceConfigRequest(String folder, String servletCode, HttpServletRequest request, HttpServletResponse response){
        try {
            // 1. Get public service and send request to it
        	ConfigService configService = (ConfigService) configServices.get(folder);
        	if(configService==null){
        		configService = (ConfigService) serviceManager.getPublicService(folder, servletCode);
                if (configService == null) {
                	String msg = "NO SERVICE FOUND in folder " + folder + " and servletCode " + servletCode;
                	logError(response, msg, null);
                	return;
                } else {
                	configServices.putIfAbsent(folder, configService);
                }
            }

            // 2. Processing request
            configService.request(request, response);
        } catch (Throwable th) {
            logError(response, "Unexpected exception in " + SyncDispatchServlet.class.getSimpleName(), th);
        } finally {
            //manualGCHelper.action();
        }
    }

    private void serviceProfilerRequest(String folder, String servletCode, HttpServletRequest request, HttpServletResponse response){
        try {
            String command = request.getParameter("command");
            if("clear".equalsIgnoreCase(command)){
                ExecutionTimeProfiler.clear();
                response.getOutputStream().print("<html>CLEARED</html>");
            } else {
                String msg = ExecutionTimeProfiler.printAll();
                response.getOutputStream().print("<html>"+msg+"</html>");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void serviceSqlRequest(String folder, String servletCode, HttpServletRequest request, HttpServletResponse response){

        if (traceOnlineSql.isTraceEnabled())
            traceOnlineSql.trace(FormatHelper.stringConcatenator("SyncDispatchServlet.serviceSqlRequest: folder = ", folder, " servletCode =  ", servletCode, " request = ", request));

        try {
            //1. Parsing input request
            ExecutionTimeProfiler.start("serviceSqlRequest");
            OnlineRequest onlineRequest = null;
            try {
                onlineRequest = (OnlineRequest) XMLHelper.readXML(request.getInputStream(), OnlineRequest.class);
                onlineRequest.setUserEmail(HttpRequestUtils.getUsernameFromRequest(request));
                onlineRequest.setUserIpAddress(HttpRequestUtils.getClientIpAddr(request));
            } catch (Exception e) {
                logError(response,"Cannot perform sql request " + e.getMessage(),e);
                return;
            }

            //2. Finding proper service and execute
            SQLService sqlService = sqlServices.get(folder);
            if(sqlService==null){
                sqlService = (SQLService) serviceManager.getPublicService(folder, servletCode);
                if(sqlService==null){
                    //DataResponse dr = new DataResponse();
                    //writeResponse(dr,response);
                    String msg = "Cannot perform sql request - cannot find public service (" + folder + ", " + servletCode + ")";
                    logError(response, msg, null);
                    return;
                } else {
                	sqlServices.putIfAbsent(folder, sqlService);
                }

            }

            if (traceOnlineSql.isTraceEnabled())
                traceOnlineSql.trace(FormatHelper.stringConcatenator("SyncDispatchServlet.serviceSqlRequest: ", onlineRequest, " for ", sqlService));

            //3. Processing request
            DataResponse dr = sqlService.request(onlineRequest);
            writeResponse(dr,response);
            return;
        } catch (Throwable th) {
            logError(response, "Unexpected exception in " + SyncDispatchServlet.class.getSimpleName(), th);
        } finally {
            ExecutionTimeProfiler.finish("serviceSqlRequest");
            //manualGCHelper.action();
        }
    }

    private void serviceDoRequest(String folder, String servletCode, HttpServletRequest request, HttpServletResponse response){
        try {
            // 1. Get public service and send request to it
            ExecutionTimeProfiler.start("serviceDoRequest");
            PublicService service = serviceManager.getPublicService(folder, servletCode);
            if (service == null) {
                String msg = "NO SERVICE FOUND in folder " + folder + " and servletCode " + servletCode;
                logError(response, msg, null);
                return;
            }

            // 2. Processing request
            service.request(request, response);
        } catch (Throwable th) {
            logError(response, "Unexpected exception in " + SyncDispatchServlet.class.getSimpleName(), th);
        } finally {
            ExecutionTimeProfiler.finish("serviceDoRequest");
            //manualGCHelper.action();
        }
    }

    /**
     * @param response
     * @param msg
     */
    private void logError(HttpServletResponse response, String msg, Throwable th) {
        if (th == null) {
            logger.error(msg);
            tagLogger.log("ERROR", msg);
        } else {
            logger.error(msg, th);
            tagLogger.log("ERROR", msg + " " + LogMsgComposer.logThrowableStackTrace(th));
        }
        writeError(msg,response);
    }

    @Override
    public void destroy() {
        System.out.println("START STOPPING SyncDispatchServlet");
        try {
            serviceManager.stopAll();
        } catch (ComponentException e) {
            e.printStackTrace();
        }
        System.out.println("FINISH STOPPING SyncDispatchServlet");
        super.destroy();
    }
    private static void writeResponse(DataResponse dataResponse, HttpServletResponse httpServletResponse){
        try {
            httpServletResponse.setContentType("text/xml");
            httpServletResponse.setHeader("Pragma", "no-cache");
            httpServletResponse.setDateHeader("Expires", 0);
            ServletOutputStream output = httpServletResponse.getOutputStream();
            XMLHelper.writeXMLWithBuffer(output,dataResponse, false,httpServletResponse, DataResponse.class);
            //output.write(result.getBytes("UTF-8"));
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeError(String error, HttpServletResponse httpServletResponse){
        try {
            String result = XMLHelper.writeXMLToString(new ResponseError("408", error), false, ResponseError.class);
            httpServletResponse.setContentType("text/xml");
            httpServletResponse.setHeader("Pragma", "no-cache");
            httpServletResponse.setDateHeader("Expires", 0);
            ServletOutputStream output = httpServletResponse.getOutputStream();
            output.write(result.getBytes("UTF-8"));
            output.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String extractEmailAddress(X500Principal principal){
        String all = principal.toString();
        String[] pairs = all.split(",");
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i];
            pair = pair==null ? "":pair.trim();

            final String EMAILADDRESS = "EMAILADDRESS=";
            if(pair.length()>0 && pair.startsWith(EMAILADDRESS)){
                return pair.substring(EMAILADDRESS.length());
            }
        }
        return null;
    }

}
