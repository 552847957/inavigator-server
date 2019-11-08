package ru.sbt.utils.backup.db.connection;

import java.sql.Connection;

import ru.sbt.utils.backup.configuration.DatabaseInfo;
import ru.sbt.utils.backup.configuration.DatabaseServerInfo;

public interface IConnectionHolder {
	Connection getConnection(DatabaseServerInfo dbsinfo,DatabaseInfo dbinfo);
}
