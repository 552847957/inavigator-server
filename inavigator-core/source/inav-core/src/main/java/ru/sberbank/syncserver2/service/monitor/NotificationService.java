package ru.sberbank.syncserver2.service.monitor;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.SingleThreadBackgroundService;
import ru.sberbank.syncserver2.service.core.config.SQLiteConfigLoader;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.service.monitor.check.AbstractCheckAction;

import javax.sql.DataSource;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sbt-kozhinsky-lb on 15.05.14.
 */
public class NotificationService extends SingleThreadBackgroundService {
    public final static String LOCAL_HOST_NAME;
    static {
        String hostName = "";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } finally {
        }
        LOCAL_HOST_NAME = hostName;
    }


    private static final String emailSenderCode = "emailSender";
    private static final String smsSenderCode = "smsSender";

    protected EmailSender emailSender;
    protected SmsSender smsSender;
    private JdbcTemplate jdbcTemplate;

    private String alertTransports;
    private String alertAddresses;
    private String alertPhones;

    public NotificationService() {
        super(60);
    }

    public String getAlertTransports() {
        return alertTransports;
    }

    public void setAlertTransports(String alertTransports) {
        this.alertTransports = alertTransports;
    }

    public String getAlertAddresses() {
        return alertAddresses;
    }

    public void setAlertAddresses(String alertAddresses) {
        this.alertAddresses = alertAddresses;
    }

    public String getAlertPhones() {
        return alertPhones;
    }

    public void setAlertPhones(String alertPhones) {
        this.alertPhones = alertPhones;
    }

    @Override
    public void doInit() {
        //1. Finding sms and email senders
        ServiceManager serviceManager = getServiceContainer().getServiceManager();
        try {
            ServiceContainer container = serviceManager.findServiceByBeanCode(emailSenderCode);
            if (container != null) {
                emailSender = (EmailSender) container.getService();
            }
            container = serviceManager.findServiceByBeanCode(smsSenderCode);
            if (container != null) {
                smsSender = (SmsSender) container.getService();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //2. Creating jdbc template to read data from database
        try {
            if (! (serviceManager.getConfigLoader() instanceof SQLiteConfigLoader)) {
                DataSource dataSource = serviceManager.getConfigSource();//new DriverManagerDataSource(logDbURL, props);
                jdbcTemplate = new JdbcTemplate(dataSource);
            }
        } catch (Throwable th) {
            tagLogger.log("Error at starting Notification Service " + th.getMessage());
            throw new RuntimeException(th);
        }
    }

    @Override
    public void doRun() {
        //1. Listing notifications to send
        tagLogger.log("Start notification service doRun()");
        try {
            List<Notification> notifications = jdbcTemplate.query("exec SP_LIST_NOTIFICATIONS ?",
                    new PreparedStatementSetter() {

                        @Override
                        public void setValues(PreparedStatement preparedStatement) throws SQLException {
                            preparedStatement.setString(1, LOCAL_HOST_NAME);
                        }
                    },
                    new RowMapper<Notification>() {

                        @Override
                        public Notification mapRow(ResultSet rs, int rowNum) throws SQLException {
                            return new Notification(
                                    rs.getInt(1),
                                    rs.getString(2),
                                    rs.getString(3),
                                    rs.getString(4)
                            );
                        }

                    }
            );

            //2. Sending notications and mark them sent
            for (int i = 0; i < notifications.size(); i++) {
                //2.1. Notify
                Notification notification =  notifications.get(i);
                String text = notification.getNotificationText();
                try {
                    notify(text);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //2. Mark as sent
                int notificationId = notification.notificationId;
                String sourceHost  = notification.sourceHost;
                String sourceApp   = notification.sourceApp;
                jdbcTemplate.update("SP_SET_NOFIFY_TIME ?,?,?",notificationId,sourceHost,sourceApp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        tagLogger.log("Finish notification service doRun()");
    }

    public void notify(String text) {
        if (alertTransports.toUpperCase().contains("SMTP") && emailSender != null) {
            tagLogger.log("sending notifications to emails: " + alertAddresses);
            String[] mails = split(alertAddresses); //NPE happens in case of null and StringUtils.split
            if (mails.length>0) {
                if (!emailSender.sendMail(text, mails, text))
                	tagLogger.log("Can't send notifications to emails: " + alertAddresses);
            }
        }
        if (alertTransports.toUpperCase().contains("SMS") && smsSender != null) {
            tagLogger.log("sending notifications to phones: " + alertPhones);
            String[] phoneList = split(alertPhones); //NPE happens in case of null and StringUtils.split
            List<String> phones = Arrays.asList(phoneList);
            if (!smsSender.sendSms(text, phones))
            	tagLogger.log("Can't send notifications to phones: " + alertPhones);
        }
        tagLogger.log("MONITOR REPORT: SENDING A NOTIFICATION : "+text);
    }

    private static String[] split(String toSplit){
        toSplit = toSplit==null ? null:toSplit.trim();
        if(toSplit==null || toSplit.length()==0){
            return new String[0];
        }
        String[] result = toSplit.split(";");
        return result==null ? new String[0]:result;
    }

    private static class Notification {
        private int notificationId;
        private String notificationText;
        private String sourceHost;
        private String sourceApp;

        private Notification(int notificationId, String notificationText, String sourceHost, String sourceApp) {
            this.notificationId = notificationId;
            this.notificationText = notificationText;
            this.sourceHost = sourceHost;
            this.sourceApp = sourceApp;
        }

        public int getNotificationId() {
            return notificationId;
        }

        public void setNotificationId(int notificationId) {
            this.notificationId = notificationId;
        }

        public String getNotificationText() {
            return notificationText;
        }

        public void setNotificationText(String notificationText) {
            this.notificationText = notificationText;
        }

        public String getSourceHost() {
            return sourceHost;
        }

        public void setSourceHost(String sourceHost) {
            this.sourceHost = sourceHost;
        }

        public String getSourceApp() {
            return sourceApp;
        }

        public void setSourceApp(String sourceApp) {
            this.sourceApp = sourceApp;
        }
    }

    public static void main(String[] args) {
        //1. Local split method runs without exception with null
        String[] q1 = split(null);
        System.out.println(Arrays.asList(q1));

        //2. Local split method runs without exception with null
        String[] q12 = split("qqqq;qqqq");
        System.out.println(Arrays.asList(q12));

        //2. StringUtils.split runs with exceptions with null
        String[] q2 = StringUtils.split("qqqq;qqqq", "\n ;,");
        System.out.println(Arrays.asList(q2));
    }
}
