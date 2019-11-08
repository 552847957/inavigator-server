package ru.sbt.utils.backup.db.connection;

import net.sourceforge.jtds.jdbcx.JtdsDataSource;
import ru.sbt.utils.backup.configuration.DatabaseInfo;
import ru.sbt.utils.backup.configuration.DatabaseServerInfo;
import ru.sbt.utils.backup.util.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class JtdsConnectionHolder implements IConnectionHolder {

    static {
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver");
        } catch (Exception ex) {
            Logger.getInstance().error("Не найден драйвер Jtds: "+ ex);
            System.err.println("Не найден драйвер Jtds.");
        }
    }

    @Override
    public Connection getConnection(DatabaseServerInfo dbsinfo, DatabaseInfo dbinfo) {
        Logger.getInstance().info("Попытка подключения к серверу [" + dbsinfo.getHost() + "] на порту [" + dbsinfo.getPort() + "] "+(dbsinfo.isWindowsSecurity()?"используя учетную запись windows":"под учетной записью [" + dbsinfo.getUsername() + "]"));
        Connection conn = null;
        try {
            String port = (dbsinfo.getPort()!=null && !dbsinfo.getPort().equals(""))? (":" + dbsinfo.getPort()):"";
            if (dbsinfo.isWindowsSecurity()) {
                String username = dbsinfo.getUsername();
                String domain = "SIGMA";
                if (username.contains("\\")) {
                    String[] split = username.split("\\\\");
                    domain = split[0];
                    username = split[1];
                    Logger.getInstance().info("Домен: " + domain + ", пользователь: " + username);
                }
                JtdsDataSource source = new JtdsDataSource();
                source.setServerName(dbsinfo.getHost());
                source.setPortNumber(Integer.parseInt(dbsinfo.getPort()));
                source.setUseNTLMV2(true);
                source.setDomain(domain);
                source.setUser(username);
                source.setPassword(dbsinfo.getPassword());
                source.setDatabaseName(dbinfo.getName());
                return source.getConnection();
            } else {
                conn = DriverManager.getConnection("jdbc:jtds:sqlserver://" + dbsinfo.getHost() + port + "/" + dbinfo.getName(), dbsinfo.getUsername(), dbsinfo.getPassword());
            }
            Logger.getInstance().success("Успешно получено соединение к серверу [" + dbsinfo.getHost() + "] на порту [" + dbsinfo.getPort() + "] под учетной записью [" + (dbsinfo.isWindowsSecurity()?"windows]":dbsinfo.getUsername() + "]"));
        } catch (SQLException sqle) {
            Logger.getInstance().error("Ошибки при получении соединения к серверу [" + dbsinfo.getHost() + "] на порту [" + dbsinfo.getPort() + "] под учетной записью [" + (dbsinfo.isWindowsSecurity()?"windows] (":dbsinfo.getUsername() + "] (") + sqle.getMessage() + ")");
        }
        return conn;
    }
}
