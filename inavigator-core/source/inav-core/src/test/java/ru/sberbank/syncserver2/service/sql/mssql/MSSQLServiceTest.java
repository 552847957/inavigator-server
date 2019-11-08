package ru.sberbank.syncserver2.service.sql.mssql;

import org.junit.Test;

public class MSSQLServiceTest extends MSSQLServiceBaseTest {

    @Test
    public void mssqlServiceTest() {
        someCommonCode("DataResponse {result=OK, metadata=null, dataset=Dataset [rows=[DatasetRow [values=[Все просто здорово!]], DatasetRow [values=[И даже больше чем здорово!]]]], error=null}");
    }

}
