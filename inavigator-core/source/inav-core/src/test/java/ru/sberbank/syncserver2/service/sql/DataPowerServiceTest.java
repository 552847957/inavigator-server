package ru.sberbank.syncserver2.service.sql;

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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/data-power-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class DataPowerServiceTest extends DataPowerMockObject {

    @Autowired
    @Qualifier("dataPowerServiceTestBean")
    private DataPowerService service;

    @Before
    @Override
    public void before() throws Exception {
        super.before();
        service.doStart();
    }

    @After
    @Override
    public void after() throws ComponentException, IOException {
        super.after();
        service.doStop();
    }

    @Test
    public void dataPowerServiceTest() throws UnsupportedEncodingException {
        OnlineRequest dataPowerOnLineRequest = new OnlineRequest();
        dataPowerOnLineRequest.setService("DATAPOWER");
        dataPowerOnLineRequest.setProvider("DATAPOWER");
        dataPowerOnLineRequest.setSqlTemplate("select * from dual");
        dataPowerOnLineRequest.setAlphaDbHost("DATAPOWER");
        OnlineRequest.Arguments dataPowerArgs = new OnlineRequest.Arguments();
        dataPowerArgs.getArgument().add(new OnlineRequest.Arguments.Argument(0, FieldType.STRING, "Argument!"));
        dataPowerOnLineRequest.setArguments(dataPowerArgs);
        DataResponse dataPowerOnLineResponse = service.request(dataPowerOnLineRequest);
        Assert.assertEquals("DataResponse {result=OK, metadata=null, dataset=Dataset [rows=[DatasetRow [values=[Все просто здорово!]]]], error=null}", dataPowerOnLineResponse.toString());
    }

}
