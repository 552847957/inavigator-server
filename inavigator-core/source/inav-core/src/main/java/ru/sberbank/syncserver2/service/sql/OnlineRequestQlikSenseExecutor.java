package ru.sberbank.syncserver2.service.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.log4j.Logger;
import ru.sberbank.qlik.sense.QlikSenseClient;
import ru.sberbank.qlik.services.GetDataRequest;
import ru.sberbank.qlik.services.GetDataResponse;
import ru.sberbank.qlik.services.QlikApi;
import ru.sberbank.qlik.services.QlikApiUtils;
import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;
import ru.sberbank.syncserver2.service.sql.query.Dataset;
import ru.sberbank.syncserver2.service.sql.query.DatasetRow;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.util.FormatHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;


public class OnlineRequestQlikSenseExecutor implements PooledObjectFactory<QlikSenseClient>, QlikSenseServiceConfig {

    private static final Logger log = Logger.getLogger(OnlineRequestQlikSenseExecutor.class);

    private String serviceBeanCode;
    private String serviceName;

    private String serverHost;
    private Integer serverPort;
    private String serverContext = "/app";
    private String certificateDir = "./cerificates";
    private String rootCertificateFileName = "root.pem";
    private String clientCertificateFileName = "client.pem";
    private String clientKeyPathFileName = "client_key_8.pem";
    private File rootCertificate = null;
    private File clientCertificate = null;
    private File clientKeyPath = null;
    private String clientKeyPassword;
    private String user;
    private String domain;
    private String viewUserLogin;
    private String viewUserPassword;

    private Integer maxTotal = 8;
    private Integer maxIdle = 8;
    private Integer minIdle = 0;
    private GenericObjectPoolConfig poolConfig;
    private ObjectPool<QlikSenseClient> pool;

    private AbstractService ownerExecuter;

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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
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

    @Override
    public String getViewUserLogin() {
        return viewUserLogin;
    }

    @Override
    public void setViewUserLogin(String login) {
        this.viewUserLogin = login;
    }

    @Override
    public String getViewUserPassword() {
        return viewUserPassword;
    }

    @Override
    public void setViewUserPassword(String password) {
        this.viewUserPassword = password;
    }

    public ObjectPool<QlikSenseClient> getPool() {
        return pool;
    }

    public void setPool(ObjectPool<QlikSenseClient> pool) {
        this.pool = pool;
    }

    public GenericObjectPoolConfig getPoolConfig() {
        return poolConfig;
    }

    public void setPoolConfig(GenericObjectPoolConfig poolConfig) {
        this.poolConfig = poolConfig;
    }

    @Override
    public PooledObject<QlikSenseClient> makeObject() throws Exception {
        QlikSenseClient qlikClient = new QlikSenseClient(serverHost, serverPort, serverContext,
                rootCertificate,
                clientCertificate,
                clientKeyPath,
                clientKeyPassword,
                user,
                domain);
        PooledObject<QlikSenseClient> defaultPooledObject = new DefaultPooledObject<QlikSenseClient>(qlikClient);
        return defaultPooledObject;
    }

    @Override
    public void destroyObject(PooledObject<QlikSenseClient> pooledObject) throws Exception {
        QlikSenseClient qlikClient = pooledObject.getObject();
        qlikClient.disconnect();
    }

    @Override
    public boolean validateObject(PooledObject<QlikSenseClient> pooledObject) {
        QlikSenseClient qlikClient = pooledObject.getObject();
        return qlikClient.isConnected();
    }

    @Override
    public void activateObject(PooledObject<QlikSenseClient> pooledObject) throws Exception {
        QlikSenseClient qlikClient = pooledObject.getObject();
        if (!qlikClient.isConnected()) {
            qlikClient.connect();
        }
    }

    @Override
    public void passivateObject(PooledObject<QlikSenseClient> pooledObject) throws Exception {
    }

    public OnlineRequestQlikSenseExecutor() {
    }

    public OnlineRequestQlikSenseExecutor(String theServiceName, QlikSenseServiceConfig config, AbstractService owner, String theServiceBeanCode) {
        this.serviceName = theServiceName;
        this.ownerExecuter = owner;
        this.serviceBeanCode = theServiceBeanCode;
        this.setRootCertificateFileName(config.getRootCertificateFileName());
        this.setClientCertificateFileName(config.getClientCertificateFileName());
        this.setClientKeyPathFileName(config.getClientKeyPathFileName());
        this.setServiceName(config.getServiceName());
        this.setServerHost(config.getServerHost());
        this.setServerPort(config.getServerPort());
        this.setServerContext(config.getServerContext());
        this.setCertificateDir(config.getCertificateDir());
        this.setClientKeyPassword(config.getClientKeyPassword());
        this.setUser(config.getUser());
        this.setDomain(config.getDomain());
        this.setMinIdle(config.getMinIdle());
        this.setMaxTotal(config.getMaxTotal());
        this.setMaxIdle(config.getMaxIdle());
    }

    public void init() {
        rootCertificate = new File(certificateDir, rootCertificateFileName);
        clientCertificate = new File(certificateDir, clientCertificateFileName);
        clientKeyPath = new File(certificateDir, clientKeyPathFileName);
        poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        pool = new GenericObjectPool<QlikSenseClient>(this, poolConfig);
    }

    public DataResponse query(final OnlineRequest request) {
        DataResponse response = new DataResponse();

        int numberOfArguments = 0;
        try {
            if (request.getArguments() != null && request.getArguments().getArgument() != null) {
                numberOfArguments = request.getArguments().getArgument().size();
            }
            if (numberOfArguments != 1) throw new ArrayIndexOutOfBoundsException();
            OnlineRequest.Arguments.Argument argument = request.getArguments().getArgument().get(0);
            String requestStr = new String(Base64.decodeBase64(argument.getValue()), "UTF8");
            ObjectMapper mapper = QlikApiUtils.getObjectMapper();
            GetDataRequest getDataRequest = mapper.readValue(requestStr, GetDataRequest.class);
            QlikSenseClient qlikClient = null;
            GetDataResponse getDataResponse = null;
            String resultStr;
            try {
                qlikClient = pool.borrowObject();
                getDataResponse = getData(getDataRequest, qlikClient).get();
            } finally {
                if (qlikClient != null) pool.returnObject(qlikClient);
            }
            resultStr = getJSonString(mapper, getDataResponse);
            Dataset ds = new Dataset();
            DatasetRow dsr = new DatasetRow();
            dsr.addValue(Base64.encodeBase64String(resultStr.getBytes("UTF8")));
            ds.addRow(dsr);
            response.setDataset(ds);
            response.setResult(Result.OK);
        } catch (ArrayIndexOutOfBoundsException e) {
            String errorStr = FormatHelper.stringConcatenator("Wrong number of arguments: ", numberOfArguments);
            if (ownerExecuter != null) {
                ownerExecuter.logError(LogEventType.ERROR, errorStr, e);
                String[] tags = new String[]{serviceBeanCode};
                ownerExecuter.logServiceMessageWithTags(LogEventType.ERROR, tags, errorStr);
            }
            log.error(errorStr, e);
            response.setResult(Result.FAIL_DB);
            response.setError(errorStr);
        } catch (Exception e) {
            String errorStr = "Error request to QlikSense or handle result";
            response.setResult(Result.FAIL_DB);
            response.setError(e.getMessage());
            if (ownerExecuter != null) {
                ownerExecuter.logError(LogEventType.ERROR, errorStr, e);
                String[] tags = new String[]{serviceBeanCode};
                ownerExecuter.logServiceMessageWithTags(LogEventType.ERROR, tags, errorStr);
            }
            log.error(errorStr, e);
            e.printStackTrace();
        }

        return response;
    }

    public Future<GetDataResponse> getData(GetDataRequest getDataRequest, QlikSenseClient qlikClient) {
        switch (getDataRequest.getServerType()) {
            case QlikApi.SERVER_TYPE_QLIK_SENSE:
                return QlikApi.getQlikSenseDataAsync(getDataRequest, qlikClient);
            default:
            case QlikApi.SERVER_TYPE_QLIK_VIEW:
                return QlikApi.getQlikViewDataAsync(getDataRequest, viewUserLogin, viewUserPassword);
        }
    }

    public static String getJSonString(ObjectMapper mapper, GetDataResponse getDataResponse) throws IOException {
        String resultStr;ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            mapper.writeValue(baos, getDataResponse);
            resultStr = baos.toString("UTF8");
        } finally {
            baos.flush();
            baos.close();
        }
        return resultStr;
    }

}
