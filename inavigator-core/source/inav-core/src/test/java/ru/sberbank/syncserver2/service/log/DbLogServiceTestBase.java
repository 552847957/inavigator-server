package ru.sberbank.syncserver2.service.log;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.sberbank.syncserver2.service.core.ComponentException;
import ru.sberbank.syncserver2.util.FormatHelper;

import java.sql.SQLException;

public class DbLogServiceTestBase {

    @Autowired
    @Qualifier("dbLogService")
    protected DbLogService dbLogService;

    protected void before() throws ComponentException, InterruptedException, SQLException {

        Mockito.doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }

        }).when(dbLogService).doInit();
        Mockito.doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                return null;
            }

        }).when(dbLogService).doRun();
        Mockito.doAnswer(new Answer() {

            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                for (Object arg : args) {
                    System.out.println(FormatHelper.stringConcatenator("\t thread ", Thread.currentThread(), " arg ", arg));
                }
                return null;
            }

        }).when(dbLogService).log(Mockito.<LogMsg[]>any());
    }
}
