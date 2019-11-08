package ru.sbt.utils.backup.conversion;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DefaultConvertor implements IConvertor {	

	@Override
	public String getType() {
		return "default";
	}

	@Override
	public String convert(ResultSet resultSet, int i) throws SQLException {
		return resultSet.getString(i);		
	}

	@Override
	public void convert(PreparedStatement ps, int i, String value) throws SQLException {
		ps.setString(i, value);		
	}

	@Override
	public void setDefaultValue(PreparedStatement ps, int i)
			throws SQLException {
		ps.setString(i, "");
	}

}
