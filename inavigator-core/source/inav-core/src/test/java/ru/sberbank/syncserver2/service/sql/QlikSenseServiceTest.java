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
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.FieldType;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;

import java.io.UnsupportedEncodingException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/qlik-sense-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class QlikSenseServiceTest {

    public static final String ID_1_DOCUMENT_ID_DOCID_OBJECT_ID_ONE_TWO_AT_ST = "{id: 1, documentId: \"DOCID\", objectId: [\"ONE\",\"TWO\",...,\"AT ST\"]} ";
    @Autowired
    @Qualifier("qlikSenseServiceTestBean")
    private QlikSenseService service;

    @Before
    public void before() {
        service.doStart();
    }

    @After
    public void after() {
        service.doStop();
    }

    @Test
    public void qlikSenseServiceTest() throws UnsupportedEncodingException {
        String serviceName = "QLIK_SENSE_SERVICE_ONE";
        OnlineRequest qlikSenseRequest = new OnlineRequest();
        qlikSenseRequest.setProvider("QLIK_SENSE");
        qlikSenseRequest.setService(serviceName);
        qlikSenseRequest.setStoredProcedure("QLIK_SENSE_SERVICE_ONE_REQUEST_ONE");
        OnlineRequest.Arguments qlikSenseArgs = new OnlineRequest.Arguments();
        qlikSenseArgs.getArgument().add(new OnlineRequest.Arguments.Argument(1, FieldType.STRING, Base64.encodeBase64String(ID_1_DOCUMENT_ID_DOCID_OBJECT_ID_ONE_TWO_AT_ST.getBytes("UTF8"))));
        qlikSenseRequest.setArguments(qlikSenseArgs);
        DataResponse qlikSenseResponse = service.request(qlikSenseRequest);
        Assert.assertEquals(DataResponse.Result.OK, qlikSenseResponse.getResult());
        String resultStrExpected = OnlineRequestQlikSenseExecutorAnswer.returnJSON.replace("${serviceName}", serviceName);
        String resultStr = new String(Base64.decodeBase64(qlikSenseResponse.getDataset().getRows().get(0).getValues().get(0)), "UTF8");
        Assert.assertEquals(resultStrExpected, resultStr);
    }

}
