package ru.sberbank.syncserver2.service.core;

import org.mockito.Mockito;
import org.mockito.internal.invocation.InvocationImpl;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContext;
import java.lang.reflect.Method;

public class ServletContextAnswer implements Answer, ApplicationContextAware {

    private ApplicationContext appContext;
    private ServletContext servletContext;
    private WebApplicationContext webApplicationContext;


    @Override
    public Object answer(InvocationOnMock invocation) throws Throwable {
        Method m = ((InvocationImpl) invocation).getMethod();
        if ("getAttribute".equals(m.getName())) {
            Object[] args = invocation.getArguments();
            String arg0 = (String) args[0];
            if ("org.springframework.web.context.WebApplicationContext.ROOT".equals(arg0)) {
                return webApplicationContext;
            }
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appContext = applicationContext;
    }

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void init() {
        webApplicationContext = Mockito.mock(WebApplicationContext.class, new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Method m = ((InvocationImpl) invocation).getMethod();
                if ("getAutowireCapableBeanFactory".equals(m.getName())) {
                    return appContext.getAutowireCapableBeanFactory();
                }
                if ("getServletContext".equals(m.getName())){
                    return servletContext;
                }
                return null;
            }
        });
    }
}
