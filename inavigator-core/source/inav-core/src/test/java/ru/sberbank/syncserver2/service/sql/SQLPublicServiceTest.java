package ru.sberbank.syncserver2.service.sql;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.FieldType;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequestDBExecutor;
import ru.sberbank.syncserver2.service.sql.sqlite.SQLiteDataSourceHolder;
import ru.sberbank.syncserver2.util.FileHelper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static ru.sberbank.syncserver2.service.sql.sqlite.SQLiteServiceTest.createPhoneBookSectors;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/sql-public-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class SQLPublicServiceTest extends DataPowerMockObject {

    @Autowired
    @Qualifier("sqlPublicServiceAlphaTestBean")
    private SQLPublicService service;

    @Autowired
    @Qualifier("dataPowerServiceTestBean")
    private DataPowerService dataPowerservice;

    @Autowired
    @Qualifier("msSQLServiceServiceTestBean")
    private MSSQLService msSqlservice;
    @Autowired
    @Qualifier("jdbcTamplateMSSQLServiceMockFactoryBean")
    private JdbcTemplate msSqlJdbcTemplate;
    @Autowired
    @Qualifier("basicDataSourceMockFactoryBean")
    private BasicDataSource msSqldataSource;

    @Autowired
    @Qualifier("sqLiteServiceTestBean")
    private SQLiteService sqLiteService;
    private SQLiteDataSourceHolder sqLiteDataSourceHolder;

    @Autowired
    @Qualifier("sqliteDataSourceHolderTestName")
    public void setSqLiteDataSourceHolder(SQLiteDataSourceHolder sqLiteDataSourceHolder) {
        this.sqLiteDataSourceHolder = sqLiteDataSourceHolder;
    }

    @Value("${test.sqLiteService.localIncomingPath}")
    private String localIncomingPath;


    @Autowired
    @Qualifier("sqlDispatcherServiceSigmaTestBean")
    private SQLDispatcherService sqlDispatcherservice;

    @Autowired
    @Qualifier("msSQLServiceServiceTestBean")
    private MSSQLService dispatchService1;

    @Autowired
    @Qualifier("msSQLServiceServiceTestBean1")
    private MSSQLService dispatchService2;

    @Autowired
    @Qualifier("qlikSenseServiceTestBean")
    private QlikSenseService qlikSenseservice;

    @Before
    @Override
    public void before() throws Exception {

        super.before();

        dataPowerservice.doStart();
        service.setDataPowerService(dataPowerservice);

        msSqlJdbcTemplate.setDataSource(msSqldataSource);
        msSqlservice.setDataSource(msSqldataSource);
        msSqlservice.setJdbcTemplate(msSqlJdbcTemplate);
        msSqlservice.setProcessor(new OnlineRequestDBExecutor(msSqlJdbcTemplate));
        service.setMssqlService(msSqlservice);
        msSqlservice.doStart();

        FileHelper.createMissingFolders(localIncomingPath);
        createPhoneBookSectors(sqLiteDataSourceHolder);
        sqLiteService.doInit();
        sqLiteService.doRun();
        service.setSqliteService(sqLiteService);

        msSqlJdbcTemplate.setDataSource(msSqldataSource);
        dispatchService1.setDataSource(msSqldataSource);
        dispatchService1.setJdbcTemplate(msSqlJdbcTemplate);
        dispatchService1.setProcessor(new OnlineRequestDBExecutor(msSqlJdbcTemplate));
        dispatchService2.setDataSource(msSqldataSource);
        dispatchService2.setJdbcTemplate(msSqlJdbcTemplate);
        dispatchService2.setProcessor(new OnlineRequestDBExecutor(msSqlJdbcTemplate));
        sqlDispatcherservice.setSubService(dispatchService1);
        sqlDispatcherservice.setSubService(dispatchService2);
        //sqlDispatcherservice.setSubService(msSqlservice);
        sqlDispatcherservice.doStart();
        service.setSqlDispatcherService(sqlDispatcherservice);

        qlikSenseservice.doStart();
        sqlDispatcherservice.setSubService(qlikSenseservice);

        service.doStart();

    }

    @After
    @Override
    public void after() throws ComponentException, IOException {
        super.after();
        dataPowerservice.doStop();
        msSqlservice.doStop();
        service.doStop();
        sqLiteService.doStop();
    }

    @Test
    public void sqlPublicServiceTest() throws UnsupportedEncodingException {

        Assert.assertNotNull(service);

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

        OnlineRequest msSqlOnlineRequest = new OnlineRequest();
        msSqlOnlineRequest.setService("MSSQLTestService");
        msSqlOnlineRequest.setProvider("MSSQL");
        msSqlOnlineRequest.setStoredProcedure("msSqlStoredProcedureGenius");
        msSqlOnlineRequest.setSqlTemplate("select ?");
        msSqlOnlineRequest.setAlphaDbHost("host with ms sql");
        OnlineRequest.Arguments msSqlArgs = new OnlineRequest.Arguments();
        msSqlArgs.getArgument().add(new OnlineRequest.Arguments.Argument(0, FieldType.STRING, "Argument for ms sql!"));
        msSqlOnlineRequest.setArguments(msSqlArgs);
        DataResponse msSqlOnLineResponse = service.request(msSqlOnlineRequest);
        Assert.assertEquals("DataResponse {result=OK, metadata=null, dataset=Dataset [rows=[DatasetRow [values=[Все просто здорово!]], DatasetRow [values=[И даже больше чем здорово!]]]], error=null}", msSqlOnLineResponse.toString());

        OnlineRequest sqLiteOnlineRequest = new OnlineRequest();
        sqLiteOnlineRequest.setStoredProcedure("SELECT * FROM 'phonebook.sectors'");
        sqLiteOnlineRequest.setSqlTemplate("SELECT * FROM 'phonebook.sectors'");
        sqLiteOnlineRequest.setProvider("SQLITE");
        sqLiteOnlineRequest.setService("finik1");
        sqLiteOnlineRequest.setAlphaDbHost("finik1");
        DataResponse sqLite = service.request(sqLiteOnlineRequest);
        Assert.assertEquals("DataResponse {result=OK, metadata=DatasetMetaData [rowCount=1, fields=[DatasetFieldMetaData [name=id, type=NUMBER], DatasetFieldMetaData [name=NAME, type=STRING], DatasetFieldMetaData [name=phone, type=STRING]]], dataset=Dataset [rows=[DatasetRow [values=[1, Петя, +7(903)222-33-44]]]], error=null}", sqLite.toString());

        OnlineRequest sqlDispatcherOnlineRequest1 = new OnlineRequest();
        sqlDispatcherOnlineRequest1.setProvider("DISPATCHER");
        sqlDispatcherOnlineRequest1.setService("MSSQLTestService");
        sqlDispatcherOnlineRequest1.setStoredProcedure("msSqlStoredProcedureGenius");
        sqlDispatcherOnlineRequest1.setSqlTemplate("msSqlStoredProcedureGenius");
        sqlDispatcherOnlineRequest1.setAlphaDbHost("MSSQLTestService");
        OnlineRequest.Arguments dispatcherArgs1 = new OnlineRequest.Arguments();
        dispatcherArgs1.getArgument().add(new OnlineRequest.Arguments.Argument(0, FieldType.STRING, "Argument for ms sql!"));
        sqlDispatcherOnlineRequest1.setArguments(dispatcherArgs1);
        DataResponse sqlDispatcherDataResponse1 = service.request(sqlDispatcherOnlineRequest1);
        Assert.assertEquals("DataResponse {result=OK, metadata=null, dataset=Dataset [rows=[DatasetRow [values=[Все просто здорово!]], DatasetRow [values=[И даже больше чем здорово!]]]], error=null}", sqlDispatcherDataResponse1.toString());

        OnlineRequest sqlDispatcherOnlineRequest2 = new OnlineRequest();
        sqlDispatcherOnlineRequest2.setProvider("DISPATCHER");
        sqlDispatcherOnlineRequest2.setService("MSSQLTestService1");
        sqlDispatcherOnlineRequest2.setStoredProcedure("msSqlStoredProcedureGenius1");
        sqlDispatcherOnlineRequest2.setSqlTemplate("msSqlStoredProcedureGenius1");
        sqlDispatcherOnlineRequest2.setAlphaDbHost("MSSQLTestService1");
        OnlineRequest.Arguments dispatcherArgs2 = new OnlineRequest.Arguments();
        dispatcherArgs2.getArgument().add(new OnlineRequest.Arguments.Argument(0, FieldType.STRING, "Argument for ms sql!"));
        sqlDispatcherOnlineRequest2.setArguments(dispatcherArgs2);
        DataResponse sqlDispatcherDataResponse2 = service.request(sqlDispatcherOnlineRequest2);
        Assert.assertEquals("DataResponse {result=OK, metadata=null, dataset=Dataset [rows=[DatasetRow [values=[That's one!]], DatasetRow [values=[And that's two!]]]], error=null}", sqlDispatcherDataResponse2.toString());

        String serviceName = "QLIK_SENSE_SERVICE_ONE";
        OnlineRequest qlikSenseRequest = new OnlineRequest();
        qlikSenseRequest.setProvider("DISPATCHER");
        qlikSenseRequest.setService(serviceName);
        qlikSenseRequest.setAlphaDbHost(serviceName);
        qlikSenseRequest.setStoredProcedure("QLIK_SENSE_SERVICE_ONE_REQUEST_ONE");
        qlikSenseRequest.setSqlTemplate("QLIK_SENSE_SERVICE_ONE_REQUEST_ONE");
        OnlineRequest.Arguments qlikSenseArgs = new OnlineRequest.Arguments();
        qlikSenseArgs.getArgument().add(new OnlineRequest.Arguments.Argument(1, FieldType.STRING, "{id: 1, documentId: \"DOCID\", objectId: [\"ONE\",\"TWO\",...,\"AT ST\"]} "));
        qlikSenseRequest.setArguments(qlikSenseArgs);
        DataResponse qlikSenseResponse = service.request(qlikSenseRequest);
        Assert.assertEquals(DataResponse.Result.OK, qlikSenseResponse.getResult());
        String resultStrExpected = OnlineRequestQlikSenseExecutorAnswer.returnJSON.replace("${serviceName}", serviceName);
        String resultStr = new String(Base64.decodeBase64(qlikSenseResponse.getDataset().getRows().get(0).getValues().get(0)), "UTF8");
        Assert.assertEquals(resultStrExpected, resultStr);

    }


}
