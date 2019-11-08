package ru.sberbank.syncserver2.service.core;

import org.mockito.Mockito;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.servlet.ServletContext;

public class MockServletContextFactory implements FactoryBean<ServletContext>, ApplicationContextAware {

    private ApplicationContext appContext;

    @Override
    public ServletContext getObject() throws Exception {
        ServletContextAnswer sca = new ServletContextAnswer();
        ServletContext result = (ServletContext) Mockito.mock(ServletContext.class, sca);
        sca.setApplicationContext(appContext);
        sca.setServletContext(result);
        sca.init();
        return result;
    }

    @Override
    public Class<?> getObjectType() {
        return ServletContext.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        appContext = applicationContext;
    }

}
