package ru.sberbank.syncserver2.service.sql;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/sql-template-loader-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class SQLTemplateLoaderTest {

    @Autowired
    @Qualifier("sqlTamplateServiceTestBean")
    private SQLTemplateLoader service;

    @Before
    public void before() {
        service.doInit();
        service.doRun();
    }

    @After
    public void after() {
    }

    @Test
    public void dataPowerServiceTest() {
        String tmplA = service.getTemplateSQL("tamplateA");
        Assert.assertEquals("sql A", tmplA);
    }

}