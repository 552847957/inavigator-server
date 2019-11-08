package ru.sberbank.syncserver2.service.config;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.Serializable;
import java.lang.reflect.Method;

public class BeanAnswer implements Answer, Serializable {
    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        Method m = invocation.getMethod();
        if (m.getName().equalsIgnoreCase("getCode")) {
            return "confTag";
        }


        return null;
    }
}
