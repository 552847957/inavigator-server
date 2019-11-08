package ru.sbt.utils.backup.db.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import ru.sbt.utils.backup.configuration.DatabaseInfo;
import ru.sbt.utils.backup.configuration.DatabaseServerInfo;
import ru.sbt.utils.backup.util.Logger;

public class MSSQLConnectionHolder implements IConnectionHolder {
	static {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (Exception ex) {
			Logger.getInstance().error("Не найден драйвер MSSQL: "+ ex);
			System.err.println("Не найден драйвер MSSQL.");
		}
	}

	@Override
	public Connection getConnection(DatabaseServerInfo dbsinfo, DatabaseInfo dbinfo) {
		Logger.getInstance().info("Попытка подключения к серверу [" + dbsinfo.getHost() + "] на порту [" + dbsinfo.getPort() + "] "+(dbsinfo.isWindowsSecurity()?"используя учетную запись windows":"под учетной записью [" + dbsinfo.getUsername() + "]"));
		Connection conn = null;
		try {
			String port = (dbsinfo.getPort()!=null && !dbsinfo.getPort().equals(""))? (":" + dbsinfo.getPort()):"";
			if (dbsinfo.isWindowsSecurity())
				conn = DriverManager.getConnection("jdbc:sqlserver://" + dbsinfo.getHost() + port + ";databaseName="+dbinfo.getName()+";integratedSecurity=true;");
			else
				conn = DriverManager.getConnection("jdbc:sqlserver://" + dbsinfo.getHost() + port + ";databaseName="+dbinfo.getName(),dbsinfo.getUsername(), dbsinfo.getPassword());
			Logger.getInstance().success("Успешно получено соединение к серверу [" + dbsinfo.getHost() + "] на порту [" + dbsinfo.getPort() + "] под учетной записью [" + (dbsinfo.isWindowsSecurity()?"windows]":dbsinfo.getUsername() + "]"));
		} catch (SQLException sqle) {
			Logger.getInstance().error("Ошибки при получении соединения к серверу [" + dbsinfo.getHost() + "] на порту [" + dbsinfo.getPort() + "] под учетной записью [" + (dbsinfo.isWindowsSecurity()?"windows] (":dbsinfo.getUsername() + "] (") + sqle.getMessage() + ")");
		}
		return conn;
	}

}
