package ru.sberbank.syncserver2.version;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.core.config.ConfigLoader;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by sbt-kozhinsky-lb on 27.05.14.
 */
public class DatabaseVersionReader {
    public static String getDatabaseVersion(){
        try {
            ServiceManager serviceManager = ServiceManager.getInstance();
            ConfigLoader configLoader = serviceManager.getConfigLoader();
            String version = (String) configLoader.getValue("SELECT PROPERTY_VALUE FROM READONLY_CONFIG WHERE PROPERTY_KEY='VERSION'",new ResultSetExtractor<Object>() {
                @Override
                public String extractData(ResultSet resultSet) throws SQLException, DataAccessException {
                    if (resultSet.next()) {
                        return resultSet.getString(1);
                    } else {
                        return "";
                    }
                }
            });
            return version;
        } catch (Exception e){
            e.printStackTrace();
        } finally {
        }
        return "";
    }
}
