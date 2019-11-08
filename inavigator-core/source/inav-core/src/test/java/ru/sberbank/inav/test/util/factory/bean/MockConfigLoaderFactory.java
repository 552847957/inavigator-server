package ru.sberbank.inav.test.util.factory.bean;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.sberbank.syncserver2.service.core.config.AbstractConfigLoader;


public class MockConfigLoaderFactory implements FactoryBean<AbstractConfigLoader> {

    private Answer answer;

    public MockConfigLoaderFactory() {
        answer = null;
    }

    public MockConfigLoaderFactory(Answer theAnswer) {
        answer = theAnswer;
    }


    @Override
    public AbstractConfigLoader getObject() throws Exception {
        if (answer != null) {
            return Mockito.mock(AbstractConfigLoader.class, answer);
        } else {
            return Mockito.mock(AbstractConfigLoader.class);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
