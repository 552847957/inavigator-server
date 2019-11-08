package ru.sberbank.syncserver2.service.monitor.check;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.log.DbLogService;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Properties;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/inav3-front-checker-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class Inav3FrontBackCheckerTest {

    @Autowired
    @Qualifier("spyInav3FrontCheckerBean")
    private AppHttpRequestChecker checkerInav3Front;

    @Autowired
    @Qualifier("spyInav3BackCheckerBean")
    private AppHttpRequestChecker checkerInav3Back;

    @Autowired
    @Qualifier("dbLogService")
    private DbLogService dbLogger;

    @Autowired
    @Qualifier("serviceContainerMockBean")
    private ServiceContainer serviceContainer;

    @Before
    public void before() throws IOException, UnrecoverableKeyException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

        Properties prop = new Properties();
        prop.put("password", "QQ");

        Mockito.doReturn(prop).when(checkerInav3Front).readPropsFromFile(Mockito.any(String.class));
        Mockito.doReturn(prop).when(checkerInav3Back).readPropsFromFile(Mockito.any(String.class));

        SSLContext sslContext = Mockito.mock(SSLContext.class);
        Mockito.doReturn(sslContext).when(checkerInav3Front).createSSLContext(Mockito.any(KeyStore.class), Mockito.any(char[].class));
        Mockito.doReturn(sslContext).when(checkerInav3Back).createSSLContext(Mockito.any(KeyStore.class), Mockito.any(char[].class));

        SSLConnectionSocketFactory socketFactory = Mockito.mock(SSLConnectionSocketFactory.class);
        Mockito.doReturn(socketFactory).when(checkerInav3Front).createSSLConnectionSocketFactory(Mockito.any(SSLContext.class));
        Mockito.doReturn(socketFactory).when(checkerInav3Back).createSSLConnectionSocketFactory(Mockito.any(SSLContext.class));

        Header header = new BasicHeader("", "");
        HttpEntity responseEntity1 = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(header).when(responseEntity1).getContentType();
        String responseString1 = "<!DOCTYPE html><html lang=\"en\"><head><meta charset=\"utf-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1,shrink-to-fit=no\"><meta name=\"theme-color\" content=\"#000000\"><link rel=\"manifest\" href=\"/manifest.json\"><link rel=\"shortcut icon\" href=\"/favicon.ico\"><script type=\"text/javascript\" src=\"/config.js\"></script><title>Навигатор 3.0</title><link href=\"/static/css/main.b9a2d154.css\" rel=\"stylesheet\"></head><body><noscript>You need to enable JavaScript to run this app.</noscript><div id=\"root\"></div><script type=\"text/javascript\" src=\"/static/js/main.7c51fa68.js\"></script></body></html>";
        InputStream is1 = new ByteArrayInputStream(responseString1.getBytes("utf8"));
        Mockito.doReturn(new Long(responseString1.length())).when(responseEntity1).getContentLength();
        Mockito.doReturn(is1).when(responseEntity1).getContent();
        CloseableHttpResponse httpResponse1 = Mockito.mock(CloseableHttpResponse.class);
        Mockito.doReturn(responseEntity1).when(httpResponse1).getEntity();
        Mockito.doReturn(httpResponse1).when(checkerInav3Front).httpExecute(Mockito.any(CloseableHttpClient.class), Mockito.any(HttpGet.class));

        HttpEntity responseEntity2 = Mockito.mock(HttpEntity.class);
        Mockito.doReturn(header).when(responseEntity2).getContentType();
        String responseString2 = "{\"nOrganizationPositionID\":644,\"email\":\"Karpov.V.Vladim@sberbank.ru\",\"position\":\"Президент Банка\",\"menu\":[{\"nID\":8,\"sName_RU\":\"Экосистема\",\"sName_EN\":\"Ecosystem\",\"nObjectID\":7039,\"nObjectTypeID\":2},{\"nID\":2,\"sName_RU\":\"Расходы (old)\",\"sName_EN\":\"Expenses\",\"nObjectID\":7028,\"nObjectTypeID\":2},{\"nID\":9,\"sName_RU\":\"Расходы\",\"sName_EN\":\"Expenses\",\"nObjectID\":7040,\"nObjectTypeID\":2},{\"nID\":1,\"sName_RU\":\"Дэшборды\",\"sName_EN\":\"Dashboards\"},{\"nID\":12,\"sName_RU\":\"NPV/EVA\",\"sName_EN\":\"NPV/EVA\",\"nObjectID\":7043,\"nObjectTypeID\":2},{\"nID\":6,\"sName_RU\":\"Поиск\",\"sName_EN\":\"Search\"},{\"nID\":7,\"sName_RU\":\"Профиль\",\"sName_EN\":\"Profile\"},{\"nID\":4,\"sName_RU\":\"Упр. отчеты\",\"sName_EN\":\"Administrative reports\"},{\"nID\":3,\"sName_RU\":\"Аналит. отчеты\",\"sName_EN\":\"Analytic reports\"}]}";
        InputStream is2 = new StringBufferInputStream(responseString2);
        Mockito.doReturn(new Long(responseString2.length())).when(responseEntity2).getContentLength();
        Mockito.doReturn(is2).when(responseEntity2).getContent();
        CloseableHttpResponse httpResponse2 = Mockito.mock(CloseableHttpResponse.class);
        Mockito.doReturn(responseEntity2).when(httpResponse2).getEntity();
        Mockito.doReturn(httpResponse2).when(checkerInav3Back).httpExecute(Mockito.any(CloseableHttpClient.class), Mockito.any(HttpGet.class));

        CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
        Mockito.doReturn(httpClient).when(checkerInav3Front).createHttpClient(Mockito.any(SSLConnectionSocketFactory.class));
        Mockito.doReturn(httpClient).when(checkerInav3Back).createHttpClient(Mockito.any(SSLConnectionSocketFactory.class));

        checkerInav3Front.setDbLogger(dbLogger);
        checkerInav3Front.setServiceContainer(serviceContainer);
        checkerInav3Front.doStart();
        checkerInav3Back.setDbLogger(dbLogger);
        checkerInav3Back.setServiceContainer(serviceContainer);
        checkerInav3Back.doStart();
    }

    @After
    public void after() {
        checkerInav3Front.doStop();
        checkerInav3Back.doStop();
    }

    @Test
    public void inav3FrontCheckerTest() {
        checkerInav3Front.doCheck();
    }

    @Test
    public void inav3BackCheckerTest() {
        checkerInav3Back.doCheck();
    }

}
