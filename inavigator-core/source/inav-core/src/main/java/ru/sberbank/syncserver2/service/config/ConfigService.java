package ru.sberbank.syncserver2.service.config;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;

import ru.sberbank.syncserver2.service.core.AbstractService;
import ru.sberbank.syncserver2.service.core.PublicService;
import ru.sberbank.syncserver2.service.core.ServiceContainer;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.log.LogEventType;
import ru.sberbank.syncserver2.util.HttpRequestUtils;
import ru.sberbank.syncserver2.util.XMLHelper;
import ru.sberbank.syncserver2.util.constants.INavConstants;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static ru.sberbank.syncserver2.util.constants.INavConstants.RU_SBERBANK_SYNCSERVER2_SERVICE_CONFIG_CONFIGSERVICE_GET_PROPERTIES;

/**
 * Created by sbt-kozhinsky-lb on 05.03.14.
 */
public class ConfigService extends AbstractService implements PublicService {
    private DataSource dataSource;
    private JdbcTemplate jdbcTemplate = null;
    private Map<CounterKey, AtomicLong> counters = new HashMap<CounterKey, AtomicLong>();

    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    /**
     * Sample of request: http://10.21.138.60:9443/config/config/config.do?appBundle=balance&appVersion=1.0
     */
    public void request(HttpServletRequest request, HttpServletResponse response) {
        //1. Parsing params
        String appBundle = request.getParameter("appBundle");
        String appVersion = request.getParameter("appVersion");

        String username = HttpRequestUtils.getUsernameFromRequest(request);
        String clientIp = HttpRequestUtils.getClientIpAddr(request);
        //String fulRequestPath = HttpRequestUtils.getFulRequestPath(request);

        String msg = "appBundle=" + appBundle + "&appVersion=" + appVersion;
        logUserEvent(LogEventType.CONFIG_GET, username, clientIp, "config request: ", msg);

        // 2. Loading
		appVersion = checkAppVersion(appBundle, appVersion);
        ConfigList list = getProperties(appBundle, appVersion);
        msg += " : "+"configList=" + String.valueOf(list);
        logUserEvent(LogEventType.CONFIG_GET, username, clientIp, "config response: ", msg);

        // 3. Serializing
        String result = XMLHelper.writeXMLToString(list, true,
                ConfigList.class, ConfigProperty.class);

        //4. Writing
        response.setContentType("text/xml");
        PrintWriter out = null;
        try {
            out = response.getWriter();
            if (out != null) {
                out.print(result);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }
	
	/**
	 * Метод запрашивает все доступные версии для приложения, ищет среди них равную appVersion. В случае если не находит, 
	 * то возвращает самую свежую версию из существующих.
	 *     
	 * @param appBundle
	 * @param appVersion
	 * @return
	 */
    public String checkAppVersion(final String appBundle, final String appVersion) {
    	// запрос возвращает либо
    	// - 2 записи: appVersion и maxVersion
    	// - 1 запись: maxVersion, если appVersion не найден в БД
    	String sql = INavConstants.RU_SBERBANK_SYNCSERVER2_SERVICE_CONFIG_CONFIGSERVICE_CHECK_APP_VERSION;

        List<String> versions = jdbcTemplate.query(sql,
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement) throws SQLException {
                        preparedStatement.setString(1, appBundle);
                        preparedStatement.setString(2, appVersion);
                        preparedStatement.setString(3, appBundle);
                    }
                },
                new RowMapper<String>() {
                    @Override
                    public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new String(
                            rs.getString("app_version"));
                    }
                }
        );
    	String resultVersion = appVersion;

    	if (versions.size() > 0)
    		resultVersion = versions.get(0);
    	
    	if (versions.size() < 1)
        	tagLogger.log("Can't find requested version "+appVersion+" for "+appBundle + ". Used " + resultVersion + " version.");
    	
    	return resultVersion;
    	
    }

    public ConfigList getProperties(final String appBundle, final String appVersion) {
        String sql = INavConstants.RU_SBERBANK_SYNCSERVER2_SERVICE_CONFIG_CONFIGSERVICE_GET_PROPERTIES;
        List<ConfigProperty> props = jdbcTemplate.query(sql,
                new PreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement) throws SQLException {
                        preparedStatement.setString(1, appBundle);
                        preparedStatement.setString(2, appVersion);
                    }
                },
                new RowMapper<ConfigProperty>() {
                    @Override
                    public ConfigProperty mapRow(ResultSet rs, int rowNum) throws SQLException {
                        return new ConfigProperty(
                                rs.getString("property"),
                                rs.getString("value"));
                    }
                }
        );
        long counter = getCounter(appBundle, appVersion);
        ConfigProperty counterProperty = new ConfigProperty("requestCounter", String.valueOf(counter));
        props.add(counterProperty);
        return new ConfigList(props);
    }

    @Override
    protected void doStop() {
        logServiceMessage(LogEventType.SERV_STOP, "stopping service");
        logServiceMessage(LogEventType.SERV_STOP, "stopped service");
    }

    @Override
    protected void waitUntilStopped() {
    }

    @Override
    protected void doStart() {
        logServiceMessage(LogEventType.SERV_START, "starting service");
        ServiceContainer container = getServiceContainer();
        ServiceManager manager = container.getServiceManager();
        dataSource = manager.getConfigSource();
        if (jdbcTemplate == null) jdbcTemplate = new JdbcTemplate(dataSource);
        logServiceMessage(LogEventType.SERV_START, "started service");
    }

    private long getCounter(final String appBundle, final String appVersion) {
        AtomicLong counter = null;
        CounterKey key = new CounterKey(appBundle, appVersion);
        synchronized (counters) {
            counter = counters.get(key);
            if (counter == null) {
                counter = new AtomicLong(0);
                counters.put(key, counter);
            }
        }
        return counter.addAndGet(1);
    }

    private static class CounterKey {
        private String appBundle;
        private String appVersion;

        private CounterKey(String appBundle, String appVersion) {
            this.appBundle = appBundle;
            this.appVersion = appVersion;
        }

        public String getAppBundle() {
            return appBundle;
        }

        public void setAppBundle(String appBundle) {
            this.appBundle = appBundle;
        }

        public String getAppVersion() {
            return appVersion;
        }

        public void setAppVersion(String appVersion) {
            this.appVersion = appVersion;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CounterKey)) return false;

            CounterKey that = (CounterKey) o;

            if (appBundle != null ? !appBundle.equals(that.appBundle) : that.appBundle != null) return false;
            if (appVersion != null ? !appVersion.equals(that.appVersion) : that.appVersion != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = appBundle != null ? appBundle.hashCode() : 0;
            result = 31 * result + (appVersion != null ? appVersion.hashCode() : 0);
            return result;
        }
    }
}
