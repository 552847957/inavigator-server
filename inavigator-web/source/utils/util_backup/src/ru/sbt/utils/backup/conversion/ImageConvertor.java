package ru.sbt.utils.backup.conversion;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class ImageConvertor implements IConvertor {	

	@Override
	public String getType() {
		return "image";
	}

	@Override
	public void convert(PreparedStatement ps, int i, String value)
			throws SQLException {
		byte[] decoded = Base64.getDecoder().decode(value.getBytes(Charset.defaultCharset()));
		ps.setBlob(i, new ByteArrayInputStream(decoded));		
	}

	@Override
	public void setDefaultValue(PreparedStatement ps, int i)
			throws SQLException {
		ps.setBlob(i, new ByteArrayInputStream(new byte[0]));
	}

	@Override
	public String convert(ResultSet resultSet, int i) throws SQLException {
		byte[] bytes = resultSet.getBytes(i);
		return new String(Base64.getEncoder().encode(bytes), Charset.defaultCharset());		
	}
	
	
	

}
