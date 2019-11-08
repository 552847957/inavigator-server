package ru.sberbank.syncserver2.service.monitor.check;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import ru.sberbank.syncserver2.service.log.LogEventType;

import javax.net.ssl.SSLContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;


/**
 * Чекер GET запроса к ресурсу по URL.
 * <p>
 * В файле passwordFile должены быть свойства:
 * password=пароль для хранилища сертификата (хранилище должно быть в формате pkcs12)
 * basicAuthLogin=логин бэйсик авторизации
 * basicAuthPass=пароль бэйсик авторизации
 */
public class AppHttpRequestChecker extends AbstractCheckAction {

    private static final String CHECK_RESULT_CODE_REQUEST_FAULT = "APP_RESPONSE_CHECKER_REQUEST_FAULT";
    private static final String CHECK_RESULT_CODE_REQUEST_SUCCESS = "APP_RESPONSE_CHECKER_REQUEST_SUCCESS";
    private static final String CHECK_RESULT_CODE_REQUEST_FAULT_SOCKET_FACTORY_NOT_INIT = "APP_RESPONSE_CHECKER_REQUEST_SOCKET_FACTORY_NOT_INIT";
    private static final String CHECK_RESULT_CODE_REQUEST_FAULT_RESPONCE_NOT_MATCH = "APP_RESPONSE_CHECKER_RESPONCE_NOT_MATCH";
    private static final String CHECK_RESULT_TEXT_FAULT_SOCKET_FACTORY_NOT_INIT = "Не инициализирована фабрика соединений с приложением %s, проверка пропущена!";
    private static final String CHECK_WORCK_APP_S = "Проверка работоспособности приложения %s";
    private static final String RESPONSE_APP_S_NOT_MATCH_PATTERN_S = "Ответ приложения %s не соответствует шаблону %s";
    private static final String REQUEST_EXECUTED_SUCCESSFULL = "Запрос выполнен успешно!";
    private static final String CHECKER_FOR_CHECK_RESPONSE_APP_S = "Чекер для проверки ответа приложения %s.";
    private static final String NOT_SUCCESS_EXECUTE_REQUEST_S_FOR_APP_S_ERROR_S = "Не удалось выполнить запрос к %s для приложения %s, ошибка: %s";
    private static final String CERTIFICATE_S_SUCCESSFULLY_LOADED = "Сертификат %s успешно загружен.";
    private static final String NOT_LOAD_CERTIFICATE_S_WITH_KEY_IN_S_ERROR_S = "Не удалось загрузить сертификат %s с ключем в %s ошибка: %s";
    private static final String IN_FILE_PARAMS_S_MUST_BE_PROP_PASSWORD_WITH_PASSWORD_FOR_KEY_STORE_S = "В файле параметров %s должно быть свойство password с паролем для KeyStore %s";

    private ConnectionSocketFactory socketFactory;
    private SSLContext sslContext;
    private String certificateFile;
    private String passwordFile;
    private String onlineUrl;
    private String SSLProtocol = "TLSv1.2";
    private String appName;
    private String matchPattern;
    private CloseableHttpClient httpClient;

    {
        setDefaultDoNotNotifyCount(10);
    }

    @Override
    protected void doStart() {
        logServiceMessage(LogEventType.SERV_START, "starting service");
        try {
            if (passwordFile != null) {
                Properties prop = readPropsFromFile(passwordFile);
                KeyStore keyStore = KeyStore.getInstance("pkcs12");
                String keyStorePassword = prop.getProperty("password");
                if (keyStorePassword == null) {
                    throw new Exception(String.format(IN_FILE_PARAMS_S_MUST_BE_PROP_PASSWORD_WITH_PASSWORD_FOR_KEY_STORE_S, passwordFile, certificateFile));
                }
                sslContext = createSSLContext(keyStore, keyStorePassword.toCharArray());
                socketFactory = createSSLConnectionSocketFactory(sslContext);
                String basicAuthLogin = prop.getProperty("basicAuthLogin");
                String basicAuthPass = prop.getProperty("basicAuthPass");
                if (basicAuthLogin != null && basicAuthPass != null) {
                    httpClient = createHttpClient(basicAuthLogin, basicAuthPass, (SSLConnectionSocketFactory) socketFactory);
                } else {
                    httpClient = createHttpClient((SSLConnectionSocketFactory) socketFactory);
                }
            } else {
                httpClient = createHttpClient();
            }
            String msg = String.format(CERTIFICATE_S_SUCCESSFULLY_LOADED, certificateFile);
            tagLogger.log(msg);
        } catch (Exception e) {
            sslContext = null;
            httpClient = null;
            socketFactory = null;
            String errMsg = String.format(NOT_LOAD_CERTIFICATE_S_WITH_KEY_IN_S_ERROR_S, certificateFile, passwordFile, e.getMessage());
            logger.error(errMsg, e);
            tagLogger.log(errMsg);
        }
        clearAllCheckResults();
        logServiceMessage(LogEventType.SERV_START, "started service");
    }

    @Override
    protected void doStop() {
        super.doStop();
        clearAllCheckResults();
    }

    @Override
    protected List<CheckResult> doCheck() {
        List<CheckResult> result = new ArrayList<CheckResult>();
        if (httpClient != null) {
            try {
                HttpGet httpGet = new HttpGet(onlineUrl);
                HttpEntity responseEntity = null;
                try {
                    HttpResponse httpResponse = httpExecute(httpClient, httpGet);
                    responseEntity = httpResponse.getEntity();
                    logger.info(String.format(CHECK_WORCK_APP_S, appName));
                    String responseStr = EntityUtils.toString(responseEntity, "utf8");
                    logger.info(responseStr);
                    if (responseStr.matches(matchPattern)) {
                        result.add(new CheckResult(CHECK_RESULT_CODE_REQUEST_SUCCESS, true, REQUEST_EXECUTED_SUCCESSFULL));
                    } else {
                        String errMsg = String.format(RESPONSE_APP_S_NOT_MATCH_PATTERN_S, appName, matchPattern);
                        logger.error(errMsg);
                        tagLogger.log(errMsg);
                        result.add(new CheckResult(CHECK_RESULT_CODE_REQUEST_FAULT_RESPONCE_NOT_MATCH, false, errMsg));
                    }
                } finally {
                    EntityUtils.consume(responseEntity);
                }
            } catch (IOException e) {
                String errMsg = String.format(NOT_SUCCESS_EXECUTE_REQUEST_S_FOR_APP_S_ERROR_S, onlineUrl, appName, e.getMessage());
                logger.error(errMsg, e);
                tagLogger.log(errMsg);
                result.add(new CheckResult(CHECK_RESULT_CODE_REQUEST_FAULT, false, e.getMessage()));
            }
        } else {
            String errMsg = String.format(CHECK_RESULT_TEXT_FAULT_SOCKET_FACTORY_NOT_INIT, appName, matchPattern);
            logger.error(errMsg);
            tagLogger.log(errMsg);
            result.add(new CheckResult(CHECK_RESULT_CODE_REQUEST_FAULT_SOCKET_FACTORY_NOT_INIT, false, errMsg));
        }
        return result;
    }

    SSLConnectionSocketFactory createSSLConnectionSocketFactory(SSLContext theSslContext) {
        return new SSLConnectionSocketFactory(
                theSslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    }

    CloseableHttpClient createHttpClient() {
        HttpClientBuilder httpBuilder = HttpClientBuilder.create();
        return httpBuilder.build();
    }

    CloseableHttpClient createHttpClient(SSLConnectionSocketFactory theSocketFactory) {
        HttpClientBuilder httpBuilder = HttpClientBuilder.create();
        httpBuilder.setSSLSocketFactory(theSocketFactory);
        return httpBuilder.build();
    }

    CloseableHttpClient createHttpClient(String basicAuthLogin, String basicAuthPass, SSLConnectionSocketFactory theSocketFactory) {
        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials
                = new UsernamePasswordCredentials(basicAuthLogin, basicAuthPass);
        provider.setCredentials(AuthScope.ANY, credentials);
        HttpClientBuilder httpBuilder = HttpClientBuilder.create();
        httpBuilder.setDefaultCredentialsProvider(provider);
        httpBuilder.setSSLSocketFactory(theSocketFactory);
        return httpBuilder.build();
    }

    SSLContext createSSLContext(KeyStore theKeyStore, char[] thePasswords) throws IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, KeyManagementException {
        InputStream keyStoreInput = new FileInputStream(certificateFile);
        try {
            theKeyStore.load(keyStoreInput, thePasswords);
        } finally {
            keyStoreInput.close();
        }
        SSLContext sslContext = SSLContexts.custom()
                .loadKeyMaterial(theKeyStore, thePasswords)
                .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                .useTLS()
                .build();
        return sslContext;
    }

    Properties readPropsFromFile(String thePathAndFileName) throws IOException {
        Properties prop = new Properties();
        FileInputStream input = new FileInputStream(thePathAndFileName);
        try {
            prop.load(input);
        } finally {
            input.close();
        }
        return prop;
    }

    CloseableHttpResponse httpExecute(CloseableHttpClient httpClient, HttpGet httpGet) throws IOException {
        return httpClient.execute(httpGet);
    }

    @Override
    public String getDescription() {
        return String.format(CHECKER_FOR_CHECK_RESPONSE_APP_S, appName);
    }

    public String getCertificateFile() {
        return certificateFile;
    }

    public void setCertificateFile(String certificateFile) {
        this.certificateFile = certificateFile;
    }

    public String getPasswordFile() {
        return passwordFile;
    }

    public void setPasswordFile(String passwordFile) {
        this.passwordFile = passwordFile;
    }

    public String getOnlineUrl() {
        return onlineUrl;
    }

    public void setOnlineUrl(String onlineUrl) {
        this.onlineUrl = onlineUrl;
    }

    public String getSSLProtocol() {
        return SSLProtocol;
    }

    public void setSSLProtocol(String SSLProtocol) {
        this.SSLProtocol = SSLProtocol;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getMatchPattern() {
        return matchPattern;
    }

    public void setMatchPattern(String matchPattern) {
        this.matchPattern = matchPattern;
    }

}
