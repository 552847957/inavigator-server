package ru.sberbank.syncserver2.gui.data;
/**
 * @author Leonid Kozhinsky
 *
 */
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface SQLDescriptor<T> {

	public static final int SQL_SELECT = 1;

	public static final int SQL_GET_WHERE = 2;

	public static final int SQL_UPDATE = 3;

	public static final int SQL_INSERT = 4;

	public static final int SQL_DELETE = 5;

	public static final int SQL_DUBLICATE = 6;

	public static final int SQL_EXTRA_QUERY = 7;

	public static final int SQL_GET_CURRVAL = 8;

	public String composeSQL(Object o, int queryType);

	public String composePrepareSQL(int queryType);

	public void setParameters(Object o, PreparedStatement st, int queryType) throws SQLException;

	public T newInstance(ResultSet rs) throws SQLException;
}
