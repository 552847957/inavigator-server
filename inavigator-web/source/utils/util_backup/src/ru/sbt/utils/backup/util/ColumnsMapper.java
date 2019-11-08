package ru.sbt.utils.backup.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import ru.sbt.utils.backup.conversion.ConvertorFactory;
import ru.sbt.utils.backup.conversion.IConvertor;
import ru.sbt.utils.backup.model.Column;

public class ColumnsMapper {
	private InnerMapper[] mappers;
	
	
	public ColumnsMapper(List<Column> DBColumns, List<String> BackupedHeaders) {
		mappers = new InnerMapper[DBColumns.size()];
		for (int i = 0; i < DBColumns.size() ; i++) {
			Column column = DBColumns.get(i);
			int index = BackupedHeaders.indexOf(column.columnName);
			if (index>-1) {
				mappers[i] = new IndexMapper(i+1, ConvertorFactory.getConvertorFor(column.dataType), index);
			} else {
				mappers[i] = new DefaultValueMapper(i+1, ConvertorFactory.getConvertorFor(column.dataType));
			}
		}		
	}
	
	public void mapAllValues(PreparedStatement stmt, List<String> row) throws SQLException {
		for (InnerMapper mapper: mappers) {
			mapper.map(stmt, row);
		}
	}
	
	private static interface InnerMapper {		
		void map(PreparedStatement stmt, List<String> row) throws SQLException;		
	}
	
	private static class IndexMapper implements InnerMapper {
		IConvertor convertor;
		int index;
		int stmtIndex;
		public IndexMapper(int stmtIndex, IConvertor convertor, int index) {
			this.stmtIndex = stmtIndex;
			this.convertor = convertor;
			this.index = index;
		}

		@Override
		public void map(PreparedStatement stmt, List<String> row) throws SQLException {
			convertor.convert(stmt, stmtIndex, row.get(index));
		}		
	}
	
	private static class DefaultValueMapper implements InnerMapper {
		IConvertor convertor;
		int stmtIndex;
		public DefaultValueMapper(int stmtIndex, IConvertor convertor) {
			this.stmtIndex = stmtIndex;
			this.convertor = convertor;
		}

		@Override
		public void map(PreparedStatement stmt, List<String> row) throws SQLException {
			convertor.setDefaultValue(stmt, stmtIndex);
		}		
	}	
	

}
