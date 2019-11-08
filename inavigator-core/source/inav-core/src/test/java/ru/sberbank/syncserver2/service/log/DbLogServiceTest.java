package ru.sberbank.syncserver2.service.log;

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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.Bean;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/dblogger-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class DbLogServiceTest {

    @Autowired
    @Qualifier("dbLogService")
    private DbLogService dbLogService;

    private ServiceContainer sc = new ServiceContainer(Mockito.mock(ServiceManager.class, new Answer() {
        @Override
        public Object answer(InvocationOnMock invocation) throws Throwable {
            Method m = invocation.getMethod();
            if ("getConfigLoader".equals(m.getName())) {
                return Mockito.mock(ConfigLoader.class, new Answer() {
                    @Override
                    public Object answer(InvocationOnMock invocation) throws Throwable {
                        Method m = invocation.getMethod();
                        Object[] args = invocation.getArguments();
                        if ("getSyncConfigProperty".equals(m.getName())) {
                            if ("IS_DB_LOGGING_ENABLED".equals(args[0])) {
                                return "True";
                            }
                        }
                        return null;
                    }
                });
            }
            if ("getConfigSource".equals(m.getName())) {
                return Mockito.mock(DataSource.class);
            }
            return null;
        }
    }),
            "qlikview",
            Mockito.spy(new Bean(1L, "qlikView", "ru.sberbank.syncserver2.service.log.DbLogService", null,
                    null, 1, null, "DbLogService")));

    private volatile List<LogMsg> msgs = new ArrayList<LogMsg>();


    @Before
    public void before() throws ComponentException, InterruptedException {

        JdbcTemplate jdbcTemplate = Mockito.mock(JdbcTemplate.class, new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = invocation.getMethod();
                Object[] args = invocation.getArguments();
                if ("batchUpdate".equals(m.getName())) {
                    msgs.addAll((List<LogMsg>) args[1]);
                }
                return null;
            }
        });

        Mockito.doReturn(jdbcTemplate).when(dbLogService).getJdbcTemplate(Mockito.any(DataSource.class));

        dbLogService.setServiceContainer(sc);
        sc.setService(dbLogService);
        sc.startService();

        Thread.sleep(5000);

    }

    @After
    public void after() {
        //
    }

    @Test
    public void clusterManagerServiceTest() throws InterruptedException {
        LogMsg lm = new LogMsg("eventId", Calendar.getInstance().getTime(), "email@mail.ru", "clientEventId", "device", LogEventType.DEBUG, "startEventId", "eventDesc", "clientIpAddress", "webHostName",
                "webAppName", "distribServer", "eventInfo", "errorStackTrace");
        dbLogService.log(lm);
        int cnt = 0;
        while (msgs.size() < 3) {
            Thread.sleep(1000);
            cnt++;
            if (cnt > 300) {
                break;
            }
        }
        Assert.assertTrue(msgs.size() == 3);
    }

}
