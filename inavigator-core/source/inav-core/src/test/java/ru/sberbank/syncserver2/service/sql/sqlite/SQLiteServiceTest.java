package ru.sberbank.syncserver2.service.sql.sqlite;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.syncserver2.service.sql.SQLiteService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.util.FileHelper;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/sqlite-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class SQLiteServiceTest {

    @Autowired
    @Qualifier("sqLiteServiceTestBean")
    private SQLiteService service;
    private SQLiteDataSourceHolder sqLiteDataSourceHolder;

    @Autowired
    @Qualifier("sqliteDataSourceHolderTestName")
    public void setSqLiteDataSourceHolder(SQLiteDataSourceHolder sqLiteDataSourceHolder) {
        this.sqLiteDataSourceHolder = sqLiteDataSourceHolder;
    }

    @Value("${test.sqLiteService.localIncomingPath}")
    private String localIncomingPath;

    @Before
    public void before() throws IOException, InterruptedException, SQLException {
        FileHelper.createMissingFolders(localIncomingPath);
        createPhoneBookSectors(sqLiteDataSourceHolder);

        service.doStop();
        service.doInit();
        service.doRun();

    }

    public static void createPhoneBookSectors(SQLiteDataSourceHolder theSQLiteDataSourceHolder) throws SQLException {
        Connection connection = theSQLiteDataSourceHolder.getIncomeDataSource().getConnection();
        try {
            PreparedStatement stmt = connection.prepareStatement("CREATE TABLE if NOT EXISTS 'phonebook.sectors' (\n" +
                    "  id NUMBER,\n" +
                    "  NAME VARCHAR(100),\n" +
                    "  phone VARCHAR(100)\n" +
                    ");\n");
            stmt.execute();
            stmt = connection.prepareStatement("INSERT INTO 'phonebook.sectors' (id, name, phone) VALUES (1, 'Петя', '+7(903)222-33-44');");
            stmt.execute();
        } finally {
            connection.commit();
            connection.close();
            theSQLiteDataSourceHolder.getIncomeDataSource().close();
        }

    }

    @After
    public void after() throws SQLException {
        service.doStop();
    }

    @Test
    public void sqLiteServiceTest() throws IOException, SQLException {

        OnlineRequest or = new OnlineRequest();
        or.setStoredProcedure("SELECT * FROM 'phonebook.sectors'");
        or.setProvider("SQLITE");
        or.setService("finik1");

        System.out.println("REQUEST: " + or);
        DataResponse dr = service.request(or);
        System.out.println("RESULT: " + dr);

        Assert.assertEquals("DataResponse {result=OK, metadata=DatasetMetaData [rowCount=1, fields=[DatasetFieldMetaData [name=id, type=NUMBER], DatasetFieldMetaData [name=NAME, type=STRING], DatasetFieldMetaData [name=phone, type=STRING]]], dataset=Dataset [rows=[DatasetRow [values=[1, Петя, +7(903)222-33-44]]]], error=null}", dr.toString());

    }

}
