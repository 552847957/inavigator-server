package ru.sberbank.syncserver2.service.sql;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.FieldType;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.util.XMLHelper;

import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/maintenance-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class MaintenanceServiceTest extends StatusFromHostMock {

    public static final String ID_1_DOCUMENT_ID_DOCID_OBJECT_ID_ONE_TWO_AT_ST = "{id: 1, documentId: \"DOCID\", objectId: [\"ONE\",\"TWO\",...,\"AT ST\"]} ";
    @Autowired
    @Qualifier("maintenanceServiceTestBean")
    private MaintenanceService service;

    @Autowired
    @Qualifier("sqlRequestLogService")
    private SQLRequestLogService sqlRequestLogService;

    @Autowired
    @Qualifier("sqlRequestAndCertificateEmailVerifier")
    private SQLRequestAndCertificateEmailVerifier sqlRequestAndCertificateEmailVerifier;

    @Autowired
    @Qualifier("sqlPublicServiceSigmaTestBean")
    private SQLPublicService sqlPublicServiceSigma;

    @Autowired
    @Qualifier("dataPowerServiceTestBean")
    private DataPowerService dataPowerService;

    @Autowired
    @Qualifier("sqlDispatcherServiceAlphaTestBean")
    private SQLDispatcherService sqlDispatcherServiceAlpha;

    @Autowired
    @Qualifier("qlikSenseServiceTestBean")
    private QlikSenseService qlikSenseservice;

    @Before
    public void before() throws Exception {
        super.before();

        qlikSenseservice.doStart();
        sqlDispatcherServiceAlpha.setSubService(qlikSenseservice);

        sqlPublicServiceAlpha.setSqlDispatcherService(sqlDispatcherServiceAlpha);
        sqlPublicServiceAlpha.doStart();

        sqlPublicServiceSigma.setDataPowerService(dataPowerService);
        sqlRequestAndCertificateEmailVerifier.doStart();
        sqlRequestLogService.doInit();
        sqlRequestLogService.doRun();
        service.doInit();
        service.doRun();
    }

    @After
    public void after() throws ComponentException, IOException {
        qlikSenseservice.doStop();
        sqlDispatcherServiceAlpha.doStop();
        sqlPublicServiceAlpha.doStop();
        sqlRequestAndCertificateEmailVerifier.doStop();
        sqlRequestLogService.doStop();
        super.after();
    }

    @Test
    public void maintenanceServiceTest() throws IOException, InterruptedException {

        String serviceNameOne = "QLIK_SENSE_SERVICE_ONE";
        OnlineRequest qlikSenseRequestOne = new OnlineRequest();
        qlikSenseRequestOne.setProvider("DATAPOWER");
        qlikSenseRequestOne.setService(serviceNameOne);
        qlikSenseRequestOne.setStoredProcedure("QLIK_SENSE_SERVICE_ONE_REQUEST_ONE");
        qlikSenseRequestOne.setUserEmail("mail1@sberbank.ru");
        OnlineRequest.Arguments qlikSenseArgsOne = new OnlineRequest.Arguments();
        qlikSenseArgsOne.getArgument().add(new OnlineRequest.Arguments.Argument(1, FieldType.STRING, Base64.encodeBase64String(ID_1_DOCUMENT_ID_DOCID_OBJECT_ID_ONE_TWO_AT_ST.getBytes("UTF8"))));
        qlikSenseRequestOne.setArguments(qlikSenseArgsOne);
        System.out.println(qlikSenseRequestOne);
        String requestXmlString = XMLHelper.writeXMLToString(qlikSenseRequestOne, false, OnlineRequest.class);
        System.out.println(requestXmlString);
        System.out.println(ID_1_DOCUMENT_ID_DOCID_OBJECT_ID_ONE_TWO_AT_ST);
        DataResponse qlikSenseResponseOne = service.request(qlikSenseRequestOne);
        Assert.assertEquals(DataResponse.Result.OK, qlikSenseResponseOne.getResult());
        String resultStrExpected = OnlineRequestQlikSenseExecutorAnswer.returnJSON.replace("${serviceName}", serviceNameOne);
        String resultStr = new String(Base64.decodeBase64(qlikSenseResponseOne.getDataset().getRows().get(0).getValues().get(0)), "UTF8");
        Assert.assertEquals(resultStrExpected, resultStr);
        System.out.println(qlikSenseResponseOne);
        String json = qlikSenseResponseOne.getDataset().getRows().get(0).getValues().get(0);
        json = new String(Base64.decodeBase64(json.getBytes("UTF8")), "UTF8");
        System.out.println(json);
        String responseXmlString = XMLHelper.writeXMLToString(qlikSenseResponseOne, false, DataResponse.class);
        System.out.println(responseXmlString);
    }

/*
    @Test
    public void jsonTest() throws IOException {
        Object[] results = new Object[4];
        results[0] = true;
        results[1] = "some text";
        results[2] = "mail1@sberbank.ru;mail2@sbberbank.ru;";
        results[3] = 1234567890L;
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, results);
        baos.flush();
        baos.close();
        System.out.println(baos.toString("UTF8"));
    }

    @Test
    public void xmlRequestTest() throws IOException {

        String serviceNameOne = "QLIK_SENSE_SERVICE_ONE";
        OnlineRequest qlikSenseRequestOne = new OnlineRequest();
        qlikSenseRequestOne.setProvider("DATAPOWER");
        qlikSenseRequestOne.setService(serviceNameOne);
        qlikSenseRequestOne.setStoredProcedure("QLIK_SENSE_SERVICE_ONE_REQUEST_ONE");
        qlikSenseRequestOne.setUserEmail("mail1@sberbank.ru");
        OnlineRequest.Arguments qlikSenseArgsOne = new OnlineRequest.Arguments();
        qlikSenseArgsOne.getArgument().add(new OnlineRequest.Arguments.Argument(1, FieldType.STRING, "{id: 1, documentId: \"DOCID\", objectId: [\"ONE\",\"TWO\",...,\"AT ST\"]} "));
        qlikSenseRequestOne.setArguments(qlikSenseArgsOne);
        String requestString = XMLHelper.writeXMLToString(qlikSenseRequestOne, false, OnlineRequest.class);
        System.out.println(requestString);

        DataResponse qlikSenseResponseOne = service.request(qlikSenseRequestOne);
        Assert.assertEquals(DataResponse.Result.OK, qlikSenseResponseOne.getResult());
        String responseString = XMLHelper.writeXMLToString(qlikSenseResponseOne, false, DataResponse.class);
        System.out.println(responseString);
        String resultStrExpected = returnJSON.replace("${serviceName}", serviceNameOne);
        String resultStr = new String(Base64.decodeBase64(qlikSenseResponseOne.getDataset().getRows().get(0).getValues().get(0)), "UTF8");
        Assert.assertEquals(resultStrExpected, resultStr);

    }
*/

}
