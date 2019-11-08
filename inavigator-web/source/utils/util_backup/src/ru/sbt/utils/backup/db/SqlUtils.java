package ru.sbt.utils.backup.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import ru.sbt.utils.backup.configuration.DatabaseInfo;
import ru.sbt.utils.backup.configuration.DatabaseServerInfo;
import ru.sbt.utils.backup.configuration.TableInfo;
import ru.sbt.utils.backup.db.connection.IConnectionHolder;
import ru.sbt.utils.backup.db.connection.JtdsConnectionHolder;
import ru.sbt.utils.backup.db.connection.MSSQLConnectionHolder;

/**
 * Утилитные методы для работы SQL
 * @author sbt-gordienko-mv
 *
 */
public class SqlUtils {

	private static IConnectionHolder connectionHolder = new JtdsConnectionHolder();
//	private static IConnectionHolder connectionHolder = new MSSQLConnectionHolder();
	/**
	 * Получить соединение к БД
	 * @param dbsinfo
	 * @param dbinfo
	 * @return
	 */
	public static Connection getConnection(DatabaseServerInfo dbsinfo,DatabaseInfo dbinfo) {
		return connectionHolder.getConnection(dbsinfo, dbinfo);
	}
	
	/**
	 * Закрыть resultset
	 * @param rs
	 */
	public static void closeSqlObject(ResultSet rs) {
		if (rs == null) return;
		try {
			rs.close();
		} catch (SQLException se) {}
	}
	
	/**
	 * Закрыть Statement
	 * @param st
	 */
	public static void closeSqlObject(Statement st) {
		if (st == null) return;
		try {
			st.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}
	}

	/**
	 * Закрыть PreparedStatement
	 */
	public static void closeSqlObject(PreparedStatement pst) {
		if (pst == null) return;
		try {
			pst.close();
		} catch (SQLException se) {
			se.printStackTrace();
		}
	}
	
	/**
	 * Закрыть соединение
	 * @param conn
	 */
	public static void closeConnection(Connection conn) {
		if (conn == null) return;
		try {
			conn.close();
		} catch (SQLException se) {
			//ignore
		}
	}
	
	public static void setConnectionHolder(IConnectionHolder connectionHolder) {
		SqlUtils.connectionHolder = connectionHolder;
	}
	
	public static void setIdentityInsertOn(Statement stmt, TableInfo table) throws SQLException {
		stmt.execute(QueryManager.IdentityInsertOn(table));
	}
	
	public static void setIdentityInsertOff(Statement stmt, TableInfo table) throws SQLException {
		stmt.execute(QueryManager.IdentityInsertOff(table));
	}
}
