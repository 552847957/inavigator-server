package ru.sberbank.syncserver2.service.sql;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.sberbank.syncserver2.service.core.ServiceManagerHelper;
import ru.sberbank.syncserver2.service.log.LogAction;
import ru.sberbank.syncserver2.service.log.TagBuffers;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SQLRequestLogServiceTest {
    private SQLRequestLogService service = new SQLRequestLogService();
    private OnlineRequest request = new OnlineRequest();
    private DataResponse OKResponse = new DataResponse();
    private DataResponse ErrorResponse = new DataResponse();
    private String TEST_OK = "TEST_OK";


    @Before
    public void initAll() {
        ServiceManagerHelper.setTagLoggerForUnitTest(service);
        service.setOriginalService(new SQLService() {
            @Override
            public DataResponse request(OnlineRequest request) {
                if (TEST_OK.equals(request.getStoredProcedure()))
                    return OKResponse;
                else
                    return ErrorResponse;
            }
        });
        request.setUserEmail("inavdev.sbt@sberbank.ru");

        OKResponse.setResult(DataResponse.Result.OK);
        ErrorResponse.setResult(DataResponse.Result.FAIL);
        ErrorResponse.setError("error");
    }

    @Test
    public void logOnWithoutErrorResponse() throws Exception {
        service.doInit();
        request.setStoredProcedure(TEST_OK);
        List<LogAction> logActions = TagBuffers.listActions(service.getTag());
        int size = logActions.size();
        DataResponse response = service.request(this.request);
        service.doRun();
        assertEquals(response.getResult(), DataResponse.Result.OK);
        logActions = TagBuffers.listActions(service.getTag());
        assertEquals(size, logActions.size()); // не было записи
    }

    @Test
    public void logOnWithErrorResponse() throws Exception {
        service.doInit();
        request.setStoredProcedure("Error");
        List<LogAction> logActions = TagBuffers.listActions(service.getTag());
        int size = logActions.size();
        DataResponse response = service.request(this.request);
        service.doRun();
        assertEquals(response.getResult(), DataResponse.Result.FAIL);
        logActions = TagBuffers.listActions(service.getTag());
        assertEquals(size+1, logActions.size()); // добавилась запись
    }

    @Test
    public void logOffWithErrorResponse() throws Exception {
        service.doInit();
        service.doStop();
        request.setStoredProcedure("Error");
        List<LogAction> logActions = TagBuffers.listActions(service.getTag());
        int size = logActions.size();
        DataResponse response = service.request(this.request);
        service.doRun();
        assertEquals(response.getResult(), DataResponse.Result.FAIL);
        logActions = TagBuffers.listActions(service.getTag());
        assertEquals(size, logActions.size()); // новых записей не появилось
    }

    @After
    public void stopService() {
        service.doStop();
    }
}
