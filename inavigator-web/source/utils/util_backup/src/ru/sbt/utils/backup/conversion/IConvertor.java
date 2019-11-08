package ru.sbt.utils.backup.conversion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface IConvertor {
	public abstract String getType();
	public abstract String convert(ResultSet resultSet, int i) throws SQLException;
	public abstract void convert(PreparedStatement ps, int i, String value) throws SQLException;	
	public abstract void setDefaultValue(PreparedStatement ps, int i) throws SQLException;

}
