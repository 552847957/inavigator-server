package ru.sberbank.syncserver2.service.config;

import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;

import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/config-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class ConfigServiceTest {

    @Autowired
    @Qualifier("configServiceTestBean")
    private ConfigService configService;
    @Autowired
    @Qualifier("dataSourceMockFactoryBean")
    private DataSource configDataSource;
    @Autowired
    @Qualifier("configServiceJdbcTemplateMockFactoryBean")
    private JdbcTemplate jdbcTemplate;


    @Before
    public void before() {
        when(configService.getServiceContainer().getServiceManager().getConfigSource()).thenReturn(configDataSource);
        configService.setJdbcTemplate(jdbcTemplate);
        configService.doStart();
    }

    @After
    public void after() {
        configService.doStop();
    }

    @Test
    public void configServiceTest() throws UnsupportedEncodingException {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest();
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse();
        mockHttpServletResponse.setCharacterEncoding("UTF8");
        mockHttpServletRequest.setParameter("appBundle", "03.003.20");
        mockHttpServletRequest.setParameter("appVersion", "03.003.20");
        mockHttpServletRequest.setAttribute("SYNC_USER_NAME", "UnitTestEngin");
        configService.request(mockHttpServletRequest, mockHttpServletResponse);
        String strResponse = mockHttpServletResponse.getContentAsString();
        Assert.assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<config>\n" +
                "    <property>\n" +
                "        <name>name1</name>\n" +
                "        <value>value1</value>\n" +
                "    </property>\n" +
                "    <property>\n" +
                "        <name>requestCounter</name>\n" +
                "        <value>1</value>\n" +
                "    </property>\n" +
                "</config>\n", strResponse);
    }


}
