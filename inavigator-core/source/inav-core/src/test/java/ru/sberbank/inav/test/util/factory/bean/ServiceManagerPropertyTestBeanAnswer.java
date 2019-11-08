package ru.sberbank.inav.test.util.factory.bean;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;

import javax.sql.DataSource;
import java.io.Serializable;
import java.lang.reflect.Method;

public class ServiceManagerPropertyTestBeanAnswer implements Answer, Serializable {

    private DataSource dataSource;
    private ConfigLoader configLoader;

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        Method m = invocation.getMethod();
        if (m.getName().equalsIgnoreCase("setConfigSource")) {
            dataSource = (DataSource) invocation.getArguments()[0];
            return null;
        }
        if (m.getName().equalsIgnoreCase("getConfigSource")) {
            return dataSource;
        }
        if (m.getName().equalsIgnoreCase("setConfigLoader")) {
            configLoader = (ConfigLoader) invocation.getArguments()[0];
            return null;
        }
        if (m.getName().equalsIgnoreCase("getConfigLoader")) {
            return configLoader;
        }
        return null;
    }
}

