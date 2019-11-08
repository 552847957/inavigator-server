package ru.sberbank.syncserver2.service.sql;

import org.apache.log4j.Logger;
import ru.sberbank.syncserver2.service.core.BackgroundService;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.util.ExecutionTimeProfiler;
import ru.sberbank.syncserver2.util.FormatHelper;

/**
 * Service for request data from Qlik Sense on the ALPHA side with DISPATCH WAR.
 * Method request() get OnlineRequest object whit one argument consists JSON string with parameters of request.
 * Service name property set concrete Qlik Sense service (server).
 * Response contains one row dataset with base64 coded JSON string of response.
 */
public class QlikSenseService extends BackgroundService implements SQLService, Dispatchable, Startable, QlikSenseServiceConfig {

    private static final Logger log = Logger.getLogger(QlikSenseService.class);

    private int restarted;
    private boolean started;
    private String serviceName;
    private OnlineRequestQlikSenseExecutor processor;

    private String serverHost;
    private Integer serverPort;
    private String serverContext = "/app";
    private String certificateDir = "./cerificates";
    private String rootCertificateFileName = "root.pem";
    private String clientCertificateFileName = "client.pem";
    private String clientKeyPathFileName = "client_key_8.pem";
    private String clientKeyPassword;
    private String user;
    private String domain;
    private String viewUserLogin;
    private String viewUserPassword;

    private Integer maxTotal = 8;
    private Integer maxIdle = 8;
    private Integer minIdle = 0;

    public QlikSenseService() {
        restarted = 0;
        started = false;
    }

    public String getRootCertificateFileName() {
        return rootCertificateFileName;
    }

    public void setRootCertificateFileName(String rootCertificateFileName) {
        this.rootCertificateFileName = rootCertificateFileName;
    }

    public String getClientCertificateFileName() {
        return clientCertificateFileName;
    }

    public void setClientCertificateFileName(String clientCertificateFileName) {
        this.clientCertificateFileName = clientCertificateFileName;
    }

    public String getClientKeyPathFileName() {
        return clientKeyPathFileName;
    }

    public void setClientKeyPathFileName(String clientKeyPathFileName) {
        this.clientKeyPathFileName = clientKeyPathFileName;
    }

    public String getServerHost() {
        return serverHost;
    }

    public void setServerHost(String serverHost) {
        this.serverHost = serverHost;
    }

    public String getServerPort() {
        return serverPort.toString();
    }

    public void setServerPort(String serverPort) {
        this.serverPort = Integer.parseInt(serverPort);
    }

    public String getServerContext() {
        return serverContext;
    }

    public void setServerContext(String serverContext) {
        this.serverContext = serverContext;
    }

    public String getCertificateDir() {
        return certificateDir;
    }

    public void setCertificateDir(String certificateDir) {
        this.certificateDir = certificateDir;
    }

    public String getClientKeyPassword() {
        return clientKeyPassword;
    }

    public void setClientKeyPassword(String clientKeyPassword) {
        this.clientKeyPassword = clientKeyPassword;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getMaxTotal() {
        return maxTotal.toString();
    }

    public void setMaxTotal(String maxTotal) {
        this.maxTotal = Integer.parseInt(maxTotal);
    }

    public String getMaxIdle() {
        return maxIdle.toString();
    }

    public void setMaxIdle(String maxIdle) {
        this.maxIdle = Integer.parseInt(maxIdle);
    }

    public String getMinIdle() {
        return minIdle.toString();
    }

    public void setMinIdle(String minIdle) {
        this.minIdle = Integer.parseInt(minIdle);
    }

    public void setProcessor(OnlineRequestQlikSenseExecutor processor) {
        this.processor = processor;
    }

    public OnlineRequestQlikSenseExecutor getProcessor() {
        return processor;
    }

    @Override
    public String getViewUserLogin() {
        return viewUserLogin;
    }

    @Override
    public void setViewUserLogin(String viewUserLogin) {
        this.viewUserLogin = viewUserLogin;
    }

    public String getViewUserPassword() {
        return viewUserPassword;
    }

    public void setViewUserPassword(String viewUserpassword) {
        this.viewUserPassword = viewUserpassword;
    }

    @Override
    protected void doStart() {
        log(LogEventType.SERV_START, "starting service");
        restarted++;
        if (processor == null) {
            processor = new OnlineRequestQlikSenseExecutor(serviceName, this, this, serviceBeanCode);
            processor.init();
        }
        started = true;
        log(LogEventType.SERV_START, "started service");
    }

    @Override
    protected void doStop() {
        log(LogEventType.SERV_STOP, "stoping service");
        processor = null;
        started = false;
        log(LogEventType.SERV_STOP, "stoped service");
    }

    @Override
    public boolean isRestarted() {
        return restarted != 1;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public DataResponse request(OnlineRequest request) {
        try {
            ExecutionTimeProfiler.start("requestDataFromQlikSense");
            //
            return processor.query(request);
            //
        } finally {
            ExecutionTimeProfiler.finish("requestDataFromQlikSense");
        }
    }

    protected void log(LogEventType eventType, String msg) {
        String[] tags = new String[]{serviceBeanCode};
        logServiceMessageWithTags(eventType, tags, msg);
        logger.info(msg);
    }

    @Override
    public String toString() {
        return FormatHelper.stringConcatenator("QlikSenseService{",
                "restarted=", restarted,
                ", started=", started,
                ", serviceName='", serviceName, '\'',
                ", serviceBeanCode='", serviceBeanCode, '\'',
                '}');
    }

}
