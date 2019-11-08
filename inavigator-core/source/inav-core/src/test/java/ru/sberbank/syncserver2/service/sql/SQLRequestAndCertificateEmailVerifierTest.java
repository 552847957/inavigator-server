package ru.sberbank.syncserver2.service.sql;

import org.junit.Before;
import org.junit.Test;
import ru.sberbank.syncserver2.service.core.ServiceManagerHelper;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.FieldType;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

//@RunWith(SpringJUnit4ClassRunner.class)
public class SQLRequestAndCertificateEmailVerifierTest {
    private SQLRequestAndCertificateEmailVerifier service = new SQLRequestAndCertificateEmailVerifier();
    private OnlineRequest request = new OnlineRequest();
    private DataResponse response = new DataResponse();
    private String arg = "<params><param name=\"nDashboardID\" value=\"1252\" /><param name=\"nDynamicType\" value=\"2\" /><param name=\"dtDate\" value=\"20160331\" /><param name=\"nDepartmentID\" value=\"0\" /><param name=\"nDashBoardType\" value=\"10\" /><param name=\"Email\" value=\"inavdev.SBT@sberbank.ru\" /></params>";

    @Before
    public void initAll() {
        ServiceManagerHelper.setTagLoggerForUnitTest(service);
        request.setSqlTemplate("TEST");
        OnlineRequest.Arguments arguments = new OnlineRequest.Arguments();
        arguments.setArgument(Arrays.asList(new OnlineRequest.Arguments.Argument(1, FieldType.STRING, arg)));
        request.setArguments(arguments);
        request.setUserEmail("inavdev.sbt@sberbank.ru");
        response.setResult(DataResponse.Result.OK);
        service.setOriginalService(new SQLService() {
            @Override
            public DataResponse request(OnlineRequest request) {
                return response;
            }
        });
    }

    public void setUpService(boolean verifyEmail, boolean checkCert, String ipStr) {
        service.setSkipEmailVerification(verifyEmail ? "false" : "true");
        service.setSkipCertificateEmailChecking(checkCert ? "false" : "true");
        service.setSkipEmailVerificationIps(ipStr);
        service.doStart();
    }

    /**
     * Проверка дефолтного поведения сервиса:
     * сверка email в сертификате и запросе - включена,
     * проверка наличия сертификата при запросе - выключена
     * @throws Exception
     */
    @Test
    public void testDefaultNotBlocked() throws Exception {
        setUpService(true, false, null);
        DataResponse response = service.request(this.request);
        assertEquals(response.getResult(), DataResponse.Result.OK);
    }

    /**
     * Проверка дефолтного поведения сервиса:
     * сверка email в сертификате и запросе - включена,
     * проверка наличия сертификата при запросе - выключена
     * Ip входит в список, проверку на mail не делаем.
     * @throws Exception
     */
    @Test
    public void testDefaultNotBlockedWithIp() throws Exception {
        setUpService(true, false, "1.2.3.4");
        request.setUserIpAddress("1.2.3.4");
        DataResponse response = service.request(this.request);
        assertEquals(response.getResult(), DataResponse.Result.OK);
    }

    /**
     * Проверка дефолтного поведения сервиса:
     * сверка email в сертификате и запросе - включена,
     * проверка наличия сертификата при запросе - выключена
     * Ip не входит в список, проверку на mail делаем.
     * @throws Exception
     */
    @Test
    public void testDefaultNotBlockedWithIp1() throws Exception {
        setUpService(true, false, "1.2.3.4");
        request.setUserIpAddress("1.2.3.5");
        DataResponse response = service.request(this.request);
        assertEquals(response.getResult(), DataResponse.Result.OK);
    }

    /**
     * Проверка дефолтного поведения сервиса:
     * сверка email в сертификате и запросе - включена,
     * проверка наличия сертификата при запросе - выключена
     * @throws Exception
     */
    @Test
    public void testDefaultBlocked() throws Exception {
        setUpService(true, false, null);
        request.setUserEmail("qwerty.sbt@sberbank.ru");
        DataResponse response = service.request(this.request);
        assertEquals(response.getResult(), DataResponse.Result.FAIL_ACCESS);
    }

    /**
     * Проверка дефолтного поведения сервиса:
     * сверка email в сертификате и запросе - включена,
     * проверка наличия сертификата при запросе - выключена
     * @throws Exception
     */
    @Test
    public void testDefaultBlockedWithoutCertificate() throws Exception {
        setUpService(true, false, null);
        request.setUserEmail(null); // like user have no certificate
        DataResponse response = service.request(this.request);
        assertEquals(response.getResult(), DataResponse.Result.FAIL_ACCESS);
    }

    /**
     * Проверка неблокирующего сервиса при отключении сверки email:
     * сверка email в сертификате и запросе - выключена,
     * проверка наличия сертификата при запросе - выключена
     * @throws Exception
     */
    @Test
    public void testSkipVerificationNotBlocked() throws Exception {
        setUpService(false, false, null);
        request.setUserEmail(null); // like user have no certificate
        DataResponse response = service.request(this.request);
        assertEquals(response.getResult(), DataResponse.Result.OK);
        request.setUserEmail("qwerty.sbt@sberbank.ru");
        response = service.request(this.request);
        assertEquals(response.getResult(), DataResponse.Result.OK);
    }

}
