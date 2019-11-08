package ru.sberbank.syncserver2.service.config;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import ru.sberbank.syncserver2.util.constants.INavConstants;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ConfigAnswer implements Answer , Serializable {

    private static final long serialVersionUID = 1234567890L;

    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        String sql = INavConstants.RU_SBERBANK_SYNCSERVER2_SERVICE_CONFIG_CONFIGSERVICE_CHECK_APP_VERSION;
        String sql1 = INavConstants.RU_SBERBANK_SYNCSERVER2_SERVICE_CONFIG_CONFIGSERVICE_GET_PROPERTIES;
        Method m = invocation.getMethod();
        if ("query".equalsIgnoreCase(m.getName())) {
            if (sql.equals(invocation.getArguments()[0])) {
                List<String> rv = new ArrayList<String>();
                rv.add("03.003.20");
                return rv;
            }
            if (sql1.equals(invocation.getArguments()[0])) {
                List<ConfigProperty> rv = new ArrayList<ConfigProperty>();
                rv.add(new ConfigProperty("name1", "value1"));
                return rv;
            }
        }
        return null;
    }
}
