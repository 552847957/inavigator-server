package ru.sberbank.inav.test.util.factory.bean;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.sberbank.syncserver2.service.generator.ClusterManager;
import ru.sberbank.syncserver2.util.constants.INavConstants;

import java.io.Serializable;
import java.lang.reflect.Method;

public class ConfigLoaderTestBeanAnswer implements Answer, Serializable {
    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        Method m = invocation.getMethod();
        if ("applyMacrosToString".equalsIgnoreCase(m.getName())) {
            return invocation.getArguments()[0];
        }
        if ("getValue".equalsIgnoreCase(m.getName())) {
            String sql = (String) invocation.getArguments()[0];
            String localName = (String) invocation.getArguments()[2];
            if (INavConstants.RU_SBERBANK_SYNCSERVER2_SERVICE_GENERATOR_CLUSTERMANAGER_DORUN.equals(sql)) {
                ClusterManager.ActiveInfo localActiveInfo = new ClusterManager.ActiveInfo(true, localName);
                return localActiveInfo;
            }
        }
        return null;
    }
}
