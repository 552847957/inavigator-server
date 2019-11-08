package ru.sberbank.syncserver2.version;

import org.apache.commons.io.IOUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by sbt-kozhinsky-lb on 15.05.14.
 */
public class ManifestVersionReader {

    public static Properties getVersionProperties(ServletConfig config){
        Properties prop = new Properties();
        ServletContext servletContext = config.getServletContext();
        InputStream fis = null;
        try {
            fis = servletContext.getResourceAsStream("/META-INF/MANIFEST.MF");
            if (fis != null)
            	prop.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(fis);
        }
        return prop;
    }
}
