package ru.sberbank.inav.test.util.factory.bean;

import org.mockito.Mockito;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;

public class MockJdbcTemplateFactory implements FactoryBean<JdbcTemplate> {

    private Answer answer;

    public MockJdbcTemplateFactory() {
        answer = null;
    }

    public MockJdbcTemplateFactory(Answer theAnswer) {
        answer = theAnswer;
    }

    @Override
    public JdbcTemplate getObject() throws Exception {
        if (answer != null) {
            return Mockito.mock(JdbcTemplate.class, answer);
        } else {
            return Mockito.mock(JdbcTemplate.class);
        }
    }

    @Override
    public Class<?> getObjectType() {
        return JdbcTemplate.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }
}
