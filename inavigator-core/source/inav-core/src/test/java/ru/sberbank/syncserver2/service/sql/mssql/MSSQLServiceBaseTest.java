package ru.sberbank.syncserver2.service.sql.mssql;

import org.apache.commons.dbcp.BasicDataSource;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.syncserver2.service.sql.MSSQLService;
import ru.sberbank.syncserver2.service.sql.SQLTemplateLoader;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.FieldType;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequestDBExecutor;

import java.io.IOException;

@Ignore
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/mssql-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class MSSQLServiceBaseTest {

    @Autowired
    @Qualifier("msSQLServiceServiceTestBean")
    protected MSSQLService service;
    @Autowired
    @Qualifier("jdbcTamplateMSSQLServiceMockFactoryBean")
    protected JdbcTemplate jdbcTemplate;
    @Autowired
    @Qualifier("basicDataSourceMockFactoryBean")
    protected BasicDataSource dataSource;
    @Autowired
    @Qualifier("sqlTemplateLoaderBean")
    protected SQLTemplateLoader sqlTemplateLoader;
    protected String msSqlStoredProcedureGenius = "msSqlStoredProcedureGenius";

    @Before
    public void before() throws IOException, InterruptedException {
        service.doStart();
        jdbcTemplate.setDataSource(dataSource);
        service.setDataSource(dataSource);
        service.setJdbcTemplate(jdbcTemplate);
        service.setProcessor(new OnlineRequestDBExecutor(jdbcTemplate));
    }

    @After
    public void after() {
        //service.doStop();
    }

    protected void someCommonCode(String expected){
        OnlineRequest olr = new OnlineRequest();
        olr.setStoredProcedure(msSqlStoredProcedureGenius);
        olr.setProvider("Provader MS SQL");
        olr.setSqlTemplate("select ?");
        olr.setAlphaDbHost("host with ms sql");
        OnlineRequest.Arguments args = new OnlineRequest.Arguments();
        args.getArgument().add(new OnlineRequest.Arguments.Argument(0, FieldType.STRING, "Argument for ms sql!"));
        olr.setArguments(args);
        DataResponse dataResponse = service.request(olr);
        Assert.assertEquals(expected, dataResponse.toString());
    }

}
