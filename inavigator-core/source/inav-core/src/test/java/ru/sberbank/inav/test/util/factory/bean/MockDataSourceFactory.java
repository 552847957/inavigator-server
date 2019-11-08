package ru.sberbank.inav.test.util.factory.bean;

import org.mockito.Mockito;
import org.springframework.beans.factory.FactoryBean;

import javax.sql.DataSource;

public class MockDataSourceFactory implements FactoryBean<DataSource> {
    @Override
    public DataSource getObject() throws Exception {
        return Mockito.mock(DataSource.class);
    }

    @Override
    public Class<?> getObjectType() {
        return DataSource.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
