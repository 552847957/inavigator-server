package ru.sberbank.inav.test.util.factory.bean;

import org.apache.commons.dbcp.BasicDataSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;

public class MockBasicDataSourceFactory implements FactoryBean<BasicDataSource> {
    @Override
    public BasicDataSource getObject() throws Exception {
        return Mockito.mock(BasicDataSource.class);
    }

    @Override
    public Class<?> getObjectType() {
        return BasicDataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
