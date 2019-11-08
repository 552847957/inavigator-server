package ru.sbt.utils.backup.conversion;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class IntConvertor extends DefaultConvertor {		

	@Override
	public String getType() {
		return "int";
	}

	@Override
	public void convert(PreparedStatement ps, int i, String value)
			throws SQLException {
		if (value == null) {
			ps.setObject(i, null);
		} else {
			ps.setInt(i, Integer.parseInt(value));		
		}
	}

	@Override
	public void setDefaultValue(PreparedStatement ps, int i)
			throws SQLException {
		ps.setInt(i, 0);
	}
	
	
	

}
