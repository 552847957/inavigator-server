package ru.sberbank.syncserver2.service.common;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.UnsupportedEncodingException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/junit-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class CommonServiceTest {

    @Autowired
    private PingService pingService;
    @Autowired
    private VersionService versionService;

    @Test
    public void pingPongTest() throws UnsupportedEncodingException {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest() {
        };
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse() {
        };
        mockHttpServletResponse.setCharacterEncoding("UTF8");
        pingService.request(mockHttpServletRequest, mockHttpServletResponse);
        mockHttpServletResponse.getOutputStream();
        String strResponse = mockHttpServletResponse.getContentAsString();
        Assert.assertEquals("PONG", strResponse);
    }

    @Test
    public void versionTest() throws UnsupportedEncodingException {
        MockHttpServletRequest mockHttpServletRequest = new MockHttpServletRequest() {
        };
        MockHttpServletResponse mockHttpServletResponse = new MockHttpServletResponse() {
        };
        mockHttpServletResponse.setCharacterEncoding("UTF8");
        versionService.request(mockHttpServletRequest, mockHttpServletResponse);
        mockHttpServletResponse.getOutputStream();
        String strResponse = mockHttpServletResponse.getContentAsString();
        Assert.assertEquals("<html><body>THIS IS A VERSION INFORMATION</body></html>", strResponse);
    }


}
