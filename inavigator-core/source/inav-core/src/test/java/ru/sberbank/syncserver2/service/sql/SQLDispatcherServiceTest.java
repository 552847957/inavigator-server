package ru.sberbank.syncserver2.service.sql;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.FieldType;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequestDBExecutor;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/sql-dispatcher-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class SQLDispatcherServiceTest {

    @Autowired
    @Qualifier("sqlDispatcherServiceSigmaTestBean")
    private SQLDispatcherService service;

    @Autowired
    @Qualifier("msSQLServiceServiceTestBean")
    private MSSQLService dispatchService1;

    @Autowired
    @Qualifier("msSQLServiceServiceTestBean1")
    private MSSQLService dispatchService2;

    @Autowired
    @Qualifier("jdbcTamplateMSSQLServiceMockFactoryBean")
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    @Qualifier("basicDataSourceMockFactoryBean")
    protected BasicDataSource dataSource;
    @Autowired
    @Qualifier("sqlTemplateLoaderBean")
    protected SQLTemplateLoader sqlTemplateLoader;

    @Before
    public void before() {

        jdbcTemplate.setDataSource(dataSource);
        dispatchService1.setDataSource(dataSource);
        dispatchService1.setJdbcTemplate(jdbcTemplate);
        dispatchService1.setProcessor(new OnlineRequestDBExecutor(jdbcTemplate));

        dispatchService2.setDataSource(dataSource);
        dispatchService2.setJdbcTemplate(jdbcTemplate);
        dispatchService2.setProcessor(new OnlineRequestDBExecutor(jdbcTemplate));

        service.setSubService(dispatchService1);
        service.setSubService(dispatchService2);
        service.doStart();
    }

    @After
    public void after() {
        service.doStop();
    }

    @Test
    public void sqlDispatcherTest() {
        OnlineRequest olr = new OnlineRequest();
        olr.setService("MSSQLTestService");
        olr.setStoredProcedure("msSqlStoredProcedureGenius");
        olr.setProvider("Provader MS SQL");
        olr.setSqlTemplate("select ?");
        olr.setAlphaDbHost("host with ms sql");
        OnlineRequest.Arguments args = new OnlineRequest.Arguments();
        args.getArgument().add(new OnlineRequest.Arguments.Argument(0, FieldType.STRING, "Argument for ms sql!"));
        olr.setArguments(args);
        DataResponse dataResponse = service.request(olr);
        Assert.assertEquals("DataResponse {result=OK, metadata=null, dataset=Dataset [rows=[DatasetRow [values=[Все просто здорово!]], DatasetRow [values=[И даже больше чем здорово!]]]], error=null}", dataResponse.toString());

        OnlineRequest olr1 = new OnlineRequest();
        olr1.setService("MSSQLTestService1");
        olr1.setStoredProcedure("msSqlStoredProcedureGenius1");
        olr1.setProvider("Provader MS SQL");
        olr1.setSqlTemplate("select ?");
        olr1.setAlphaDbHost("host with ms sql");
        OnlineRequest.Arguments args1 = new OnlineRequest.Arguments();
        args1.getArgument().add(new OnlineRequest.Arguments.Argument(0, FieldType.STRING, "Argument for ms sql!"));
        olr1.setArguments(args1);
        DataResponse dataResponse1 = service.request(olr1);
        Assert.assertEquals("DataResponse {result=OK, metadata=null, dataset=Dataset [rows=[DatasetRow [values=[That's one!]], DatasetRow [values=[And that's two!]]]], error=null}", dataResponse1.toString());
    }

}
