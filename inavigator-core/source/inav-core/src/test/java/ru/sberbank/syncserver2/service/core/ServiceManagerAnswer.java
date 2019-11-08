package ru.sberbank.syncserver2.service.core;

import org.mockito.invocation.InvocationOnMock;

public class ServiceManagerAnswer implements org.mockito.stubbing.Answer {
    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        return null;
    }
}
