package ru.sberbank.syncserver2.service.monitor;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetupTest;
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

import javax.mail.Message;
import javax.mail.MessagingException;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/email-sender-service-test-spring-context.xml"})
@ActiveProfiles("unit-test")
public class EmailSenderTest {

    @Autowired
    @Qualifier("spyEmailSenderServiceTestBean")
    private EmailSender service;

    public GreenMail greenMail;

    @Before
    public void before() {
        greenMail = new GreenMail(ServerSetupTest.ALL);
        greenMail.start();
    }

    @After
    public void after() {
        greenMail.stop();
    }

    @Test
    public void clusterManagerServiceTest() throws InterruptedException, MessagingException, IOException {

        String subject = "some subject";
        String body = "some body";

        String[] mailTo = {"to@localhost.ru"};
        service.sendMail(subject + "1", mailTo, body + "1");
        service.sendMail(subject + "2", mailTo, body + "2");

        //wait for max 5s for 1 email to arrive
        //waitForIncomingEmail() is useful if you're sending stuff asynchronously in a separate thread
        Assert.assertTrue(greenMail.waitForIncomingEmail(5000, 2));

        //Retrieve using GreenMail API
        Message[] messages = greenMail.getReceivedMessages();
        Assert.assertEquals(2, messages.length);

        // Simple message
        Assert.assertEquals(subject + "1", messages[0].getSubject());
        Assert.assertEquals(body + "1", GreenMailUtil.getBody(messages[0]).trim());

        //if you send content as a 2 part multipart...
        //Assert.assertTrue(messages[1].getContent() instanceof MimeMultipart);
        //MimeMultipart mp = (MimeMultipart) messages[1].getContent();
        //Assert.assertEquals(2, mp.getCount());
        //Assert.assertEquals("body1", GreenMailUtil.getBody(mp.getBodyPart(0)).trim());
        //Assert.assertEquals("body2", GreenMailUtil.getBody(mp.getBodyPart(1)).trim());

    }

}
