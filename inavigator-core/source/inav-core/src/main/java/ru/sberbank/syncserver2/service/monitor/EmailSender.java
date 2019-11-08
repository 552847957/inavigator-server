package ru.sberbank.syncserver2.service.monitor;

import org.apache.log4j.Logger;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import ru.sberbank.syncserver2.service.core.AbstractService;

import java.util.Properties;

public class EmailSender extends AbstractService {
    private static final Logger log = Logger.getLogger(MailSender.class);

    private String mailHost;
    private String mailPort;
    private String mailFrom;
    private String mailUser;
    private String mailPassword;

    public String getMailHost() {
        return mailHost;
    }

    public void setMailHost(String mailHost) {
        this.mailHost = mailHost;
    }

    public String getMailPort() {
        return mailPort;
    }

    public void setMailPort(String mailPort) {
        this.mailPort = mailPort;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public void setMailFrom(String mailFrom) {
        this.mailFrom = mailFrom;
    }

    public String getMailUser() {
        return mailUser;
    }

    public void setMailUser(String mailUser) {
        this.mailUser = mailUser;
    }

    public String getMailPassword() {
        return mailPassword;
    }

    public void setMailPassword(String mailPassword) {
        this.mailPassword = mailPassword;
    }

    public MailSender getMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        Properties options = new Properties();
        options.put("mail.smtp.host", mailHost);
        options.put("mail.smtp.port", mailPort);
        options.put("mail.from", mailFrom);
        options.put("mail.user", mailUser);
        options.put("mail.password", mailPassword);
        options.put("mail.smtp.auth", "true");
        options.put("mail.mime.charset", "KOI8-R");
        mailSender.setDefaultEncoding("KOI8-R");
        options.put("mail.debug", "true");
        mailSender.setUsername(mailUser);
        mailSender.setPassword(mailPassword);
        mailSender.setJavaMailProperties(options);
        return mailSender;
    }

    public boolean sendMail(String subject, String[] mailTo, String text) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom(mailFrom);
            mailMessage.setTo(mailTo);
            mailMessage.setSubject(subject);
            mailMessage.setText(text);
            getMailSender().send(mailMessage);
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            log.error(e);
            tagLogger.log("Can't send e-mail: "+e.getMessage());
            return false;
        }
    }

    @Override
    protected void doStop() {
    }

    @Override
    protected void waitUntilStopped() {
    }
}
