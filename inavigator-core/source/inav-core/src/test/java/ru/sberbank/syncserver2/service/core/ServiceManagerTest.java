package ru.sberbank.syncserver2.service.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.qlik.sense.QlikSenseClient;
import ru.sberbank.qlik.services.GetDataRequest;
import ru.sberbank.qlik.services.GetDataResponse;
import ru.sberbank.qlik.services.ObjectData;
import ru.sberbank.syncserver2.service.sql.MSSQLService;
import ru.sberbank.syncserver2.service.sql.OnlineRequestQlikSenseExecutor;
import ru.sberbank.syncserver2.service.sql.QlikSenseService;
import ru.sberbank.syncserver2.service.sql.SQLService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.FieldType;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/service-manager-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class ServiceManagerTest {

    public static final String DOCUMENT_ID_HELLO_OBJECT_DATAS_ID_DATA_LEFT_NULL_TOP_NULL_MEASURES_VALUES_ERROR_FALSE_TYPE_NULL = "{\"documentId\":\"hello!\",\"objectDatas\":[{\"id\":\"data!\",\"measures\":[],\"values\":[],\"error\":false}]}";
    @Autowired
    @Qualifier("serviceManagerTest")
    private ServiceManager manager;

    @Before
    public void before() throws ComponentException {
        manager.startAll();
    }

    @After
    public void after() throws ComponentException {
        manager.stopAll();
    }

    @Test
    public void serviceManagerTest() throws IOException {
        AbstractService mssqlService = manager.findFirstServiceByClassCode(MSSQLService.class);
        Assert.assertEquals("proxyMSSQLService1", mssqlService.getServiceBeanCode());
        System.out.println(mssqlService);
        ServiceContainer container = manager.findServiceByBeanCode("proxyQlikSenseService1");
        Assert.assertEquals("QLIK_SENSE", ((QlikSenseService) container.getService()).getServiceName());

        QlikSenseService qlikSenseServiceSpy = (QlikSenseService) container.getService();
        OnlineRequestQlikSenseExecutor executor = qlikSenseServiceSpy.getProcessor();
        OnlineRequestQlikSenseExecutor spyExecutor = Mockito.spy(executor);
        Future<GetDataResponse> spyResponse = new Future<GetDataResponse>() {
            @Override
            public boolean cancel(boolean b) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return false;
            }

            @Override
            public GetDataResponse get() throws InterruptedException, ExecutionException {
                GetDataResponse dr = new GetDataResponse();
                dr.setDocumentId("hello!");
                List<ObjectData> od = new ArrayList<ObjectData>();
                ObjectData odo = new ObjectData();
                odo.setId("data!");
                od.add(odo);
                dr.setObjectDatas(od);
                return dr;
            }

            @Override
            public GetDataResponse get(long l, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
                return get();
            }
        };
        Mockito.doReturn(spyResponse).when(spyExecutor).getData(Mockito.any(GetDataRequest.class), Mockito.any(QlikSenseClient.class));
        qlikSenseServiceSpy.setProcessor(spyExecutor);


        System.out.println((QlikSenseService) container.getService());
        List<String> serviceCodes = manager.getAllServiceCodes();
        Collections.sort(serviceCodes);
        Assert.assertTrue("Должно быть!", Collections.binarySearch(serviceCodes,"adminPingService") >= 0);
        Assert.assertTrue("Должно быть!", Collections.binarySearch(serviceCodes,"adminFileLogService") >= 0);
        Assert.assertTrue("Должно быть!", Collections.binarySearch(serviceCodes,"proxyMSSQLService1") >= 0);
        Assert.assertTrue("Должно быть!", Collections.binarySearch(serviceCodes,"proxySQLPublicService") >= 0);
        Assert.assertTrue("Должно быть!", Collections.binarySearch(serviceCodes,"proxyDispatcherService") >= 0);
        Assert.assertTrue("Должно быть!", Collections.binarySearch(serviceCodes,"proxyQlikSenseService1") >= 0);
        //Assert.assertEquals("[adminPingService, adminFileLogService, proxyMSSQLService1, proxySQLPublicService, proxyDispatcherService, proxyQlikSenseService1]", serviceCodes.toString());
        System.out.println(serviceCodes);

        SQLService sqlService = (SQLService) manager.getPublicService("proxy", "online.do");

        GetDataRequest gdr = new GetDataRequest();
        gdr.setDocumentId("7E129B08-CC35-4AD9-9DC9-B57E73BD455A");
        List<String> objectIds = new ArrayList<String>();
        objectIds.add("eparPnX");
        objectIds.add("PspkZm");
        objectIds.add("URaWq");
        gdr.setObjectIds(objectIds);
        String requestStr;
        ObjectMapper mapper = new ObjectMapper();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            mapper.writeValue(baos, gdr);
            requestStr = baos.toString("UTF8");
        } finally {
            baos.flush();
            baos.close();
        }
        requestStr = Base64.encodeBase64String(requestStr.getBytes("UTF8"));

        String serviceName = "QLIK_SENSE";
        OnlineRequest qlikSenseRequest = new OnlineRequest();
        qlikSenseRequest.setProvider("DISPATCHER");
        qlikSenseRequest.setService(serviceName);
        qlikSenseRequest.setAlphaDbHost(serviceName);
        qlikSenseRequest.setStoredProcedure("QLIK_SENSE_SERVICE_ONE_REQUEST_ONE");
        qlikSenseRequest.setSqlTemplate("QLIK_SENSE_SERVICE_ONE_REQUEST_ONE");
        OnlineRequest.Arguments qlikSenseArgs = new OnlineRequest.Arguments();
        qlikSenseArgs.getArgument().add(new OnlineRequest.Arguments.Argument(1, FieldType.STRING, requestStr));
        qlikSenseRequest.setArguments(qlikSenseArgs);

        DataResponse qlikSenseResponse = sqlService.request(qlikSenseRequest);

        Assert.assertEquals(DataResponse.Result.OK, qlikSenseResponse.getResult());
        String resultStrExpected = DOCUMENT_ID_HELLO_OBJECT_DATAS_ID_DATA_LEFT_NULL_TOP_NULL_MEASURES_VALUES_ERROR_FALSE_TYPE_NULL;
        String resultStr = new String(Base64.decodeBase64(qlikSenseResponse.getDataset().getRows().get(0).getValues().get(0)), "UTF8");
        Assert.assertEquals(resultStrExpected, resultStr);


    }

}
