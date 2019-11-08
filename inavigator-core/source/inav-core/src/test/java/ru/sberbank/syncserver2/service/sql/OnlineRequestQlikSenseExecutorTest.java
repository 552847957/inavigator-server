package ru.sberbank.syncserver2.service.sql;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.qlik.sense.QlikSenseClient;
import ru.sberbank.qlik.sense.methods.*;
import ru.sberbank.qlik.sense.objects.*;
import ru.sberbank.qlik.services.GetDataRequest;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.FieldType;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.doAnswer;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/qlik-sense-executer-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class OnlineRequestQlikSenseExecutorTest {

    @Autowired
    @Qualifier("onlineRequestQlikSenseExecutorTestBean")
    private OnlineRequestQlikSenseExecutor onlineRequestQlikSenseExecutor;
    private OnlineRequestQlikSenseExecutor onlineRequestQlikSenseMocExecutor;

    @Before
    public void before() throws Exception {
        onlineRequestQlikSenseExecutor.init();
        onlineRequestQlikSenseMocExecutor = Mockito.spy(onlineRequestQlikSenseExecutor);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return makeObject();
            }
        }).when(onlineRequestQlikSenseMocExecutor).makeObject();
        onlineRequestQlikSenseMocExecutor.init();
    }

    public PooledObject<QlikSenseClient> makeObject() throws Exception {
        QlikSenseClient qlikClient = Mockito.mock(QlikSenseClient.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = invocation.getMethod();
                if ("isConnected".equals(m.getName())) {
                    return false;
                }
                if ("connect".equals(m.getName())) {
                    return null;
                }
                if ("call".equals(m.getName())) {

/*
                GetDataResponse getDataResponse = new GetDataResponse();
                getDataResponse.setDocumentId("7E129B08-CC35-4AD9-9DC9-B57E73BD455A");
                ObjectData objData1 = new ObjectData();
                objData1.setId("eparPnX");
                ObjectData objData2 = new ObjectData();
                objData1.setId("PspkZm");
                List<ObjectData> lst = new ArrayList<ObjectData>();
                lst.add(objData1);
                lst.add(objData2);
                getDataResponse.setObjectDatas(lst);
*/
                    Object argument = invocation.getArguments()[0];
                    if (argument instanceof OpenDocRequest) {
                        OpenDocRequest argumentOpenDoc = (OpenDocRequest) argument;
                        OpenDocResponse br = new OpenDocResponse();
                        OpenDocResponse.Result rslt = new OpenDocResponse.Result();
                        OpenDocResponse.QReturn qrt = new OpenDocResponse.QReturn();
                        qrt.setqHandle(1);
                        rslt.setqReturn(qrt);
                        br.setResult(rslt);
                        argumentOpenDoc.setResponse(br);
                        return argumentOpenDoc;
                    }
                    if (argument instanceof GetObjectRequest) {
                        GetObjectRequest argumentGetObjectRequest = (GetObjectRequest) argument;
                        GetObjectResponse gor = new GetObjectResponse();
                        GetObjectResponse.Result rslt = new GetObjectResponse.Result();
                        GetObjectResponse.QReturn qrt = new GetObjectResponse.QReturn();
                        qrt.setqHandle(2);
                        rslt.setqReturn(qrt);
                        gor.setResult(rslt);
                        argumentGetObjectRequest.setResponse(gor);
                        return argumentGetObjectRequest;
                    }
                    if (argument instanceof ApplyPatchesRequest) {
                        ApplyPatchesRequest argumentApplyPatchesRequest = (ApplyPatchesRequest) argument;
                        return argumentApplyPatchesRequest;
                    }
                    if (argument instanceof GetEffectivePropertiesRequest) {
                        GetEffectivePropertiesRequest argumentGetEffectivePropertiesRequest = (GetEffectivePropertiesRequest) argument;
                        //.getResponse().result.qProp.qHyperCubeDef;
                        GetEffectivePropertiesResponse getEffectivePropertiesResponse = new GetEffectivePropertiesResponse();
                        GetEffectivePropertiesResponse.Result rslt = new GetEffectivePropertiesResponse.Result();
                        GetEffectivePropertiesResponse.QProp qprop = new GetEffectivePropertiesResponse.QProp();
                        QHyperCubeDef qHyperCubeDef = new QHyperCubeDef();
                        qHyperCubeDef.qPseudoDimPos = 10;
                        qHyperCubeDef.qNoOfLeftDims = 20;
                        qHyperCubeDef.qAlwaysFullyExpanded = true;
                        String[] s1 = {"1", "2"};
                        String[] s2 = {"3", "4"};
                        String[] s3 = {"5", "6"};
                        String[] s4 = {"7", "8"};
                        qHyperCubeDef.qDimensions = new NxDimension[]{new NxDimension(new NxInlineDimensionDef(s1, s2, 1)), new NxDimension(new NxInlineDimensionDef(s3, s4, 2))};
                        qHyperCubeDef.qInitialDataFetch = new QInitialDataFetch[]{new QInitialDataFetch(100, 100, 300, 200), new QInitialDataFetch(110, 110, 310, 210)};
                        qHyperCubeDef.qMeasures = new NxMeasure[]{new NxMeasure(new NxInlineMeasureDef("qLabel1")), new NxMeasure(new NxInlineMeasureDef("qLabel2"))};
                        qHyperCubeDef.qMode = QMode.DATA_MODE_STRAIGHT;
                        qHyperCubeDef.qShowTotalsAbove = true;
                        qprop.setqHyperCubeDef(qHyperCubeDef);
                        rslt.setqProp(qprop);
                        getEffectivePropertiesResponse.setResult(rslt);
                        argumentGetEffectivePropertiesRequest.setResponse(getEffectivePropertiesResponse);
                        return argumentGetEffectivePropertiesRequest;
                    }
                }
                return null;
            }
        });
        PooledObject<QlikSenseClient> defaultPooledObject = new DefaultPooledObject<QlikSenseClient>(qlikClient);
        return defaultPooledObject;
    }

    @After
    public void after() {
    }

    @Test
    public void onlineRequestQlikSenseExecutorTest() throws IOException {
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
        String serviceName = "QLIK_SENSE_SERVICE_ONE";
        OnlineRequest qlikSenseRequest = new OnlineRequest();
        qlikSenseRequest.setProvider("QLIK_SENSE");
        qlikSenseRequest.setService(serviceName);
        qlikSenseRequest.setStoredProcedure("QLIK_SENSE_SERVICE_ONE_REQUEST_ONE");
        OnlineRequest.Arguments qlikSenseArgs = new OnlineRequest.Arguments();
        qlikSenseArgs.getArgument().add(new OnlineRequest.Arguments.Argument(1, FieldType.STRING, requestStr));
        qlikSenseRequest.setArguments(qlikSenseArgs);
        DataResponse dr = onlineRequestQlikSenseExecutor.query(qlikSenseRequest);
        System.out.println(dr);
    }

    @Test
    public void onlineRequestQlikSenseMockExecutorTest() throws Exception {
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
        String serviceName = "QLIK_SENSE_SERVICE_ONE";
        OnlineRequest qlikSenseRequest = new OnlineRequest();
        qlikSenseRequest.setProvider("QLIK_SENSE");
        qlikSenseRequest.setService(serviceName);
        qlikSenseRequest.setStoredProcedure("QLIK_SENSE_SERVICE_ONE_REQUEST_ONE");
        OnlineRequest.Arguments qlikSenseArgs = new OnlineRequest.Arguments();
        qlikSenseArgs.getArgument().add(new OnlineRequest.Arguments.Argument(1, FieldType.STRING, requestStr));
        qlikSenseRequest.setArguments(qlikSenseArgs);
        DataResponse dr = onlineRequestQlikSenseMocExecutor.query(qlikSenseRequest);
        System.out.println(dr);
        //String resultStrExpected = OnlineRequestQlikSenseExecutorAnswer.returnJSON.replace("${serviceName}", serviceName);
        //String resultStrExpected = "{\"documentId\":\"7E129B08-CC35-4AD9-9DC9-B57E73BD455A\",\"objectDatas\":[{\"id\":\"eparPnX\",\"left\":null,\"top\":null,\"measures\":[],\"values\":[],\"error\":true,\"type\":null},{\"id\":\"PspkZm\",\"left\":null,\"top\":null,\"measures\":[],\"values\":[],\"error\":true,\"type\":null},{\"id\":\"URaWq\",\"left\":null,\"top\":null,\"measures\":[],\"values\":[],\"error\":true,\"type\":null}]}";
        String resultStrExpected = "{\"documentId\":\"7E129B08-CC35-4AD9-9DC9-B57E73BD455A\",\"objectDatas\":[{\"id\":\"eparPnX\",\"measures\":[],\"values\":[],\"error\":true},{\"id\":\"PspkZm\",\"measures\":[],\"values\":[],\"error\":true},{\"id\":\"URaWq\",\"measures\":[],\"values\":[],\"error\":true}]}";
        String resultStr = new String(Base64.decodeBase64(dr.getDataset().getRows().get(0).getValues().get(0)), "UTF8");
        Assert.assertEquals(resultStrExpected, resultStr);

    }


}
