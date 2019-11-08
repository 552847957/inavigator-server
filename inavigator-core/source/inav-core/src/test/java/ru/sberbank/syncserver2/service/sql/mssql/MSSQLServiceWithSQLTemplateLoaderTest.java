package ru.sberbank.syncserver2.service.sql.mssql;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class MSSQLServiceWithSQLTemplateLoaderTest extends MSSQLServiceBaseTest {

    @Before
    public void before() throws IOException, InterruptedException {
        super.before();
        msSqlStoredProcedureGenius = "msSqlStoredProcedureGeniusTamplate";
        sqlTemplateLoader.setJdbcTemplate(jdbcTemplate);
        service.setTemplateLoader(sqlTemplateLoader);
    }

    @Test
    public void mssqlServiceWithSQLTemplateLoaderTest() {
        someCommonCode("DataResponse {result=OK, metadata=null, dataset=Dataset [rows=[DatasetRow [values=[row number one]], DatasetRow [values=[row number two]]]], error=null}");
    }

}
