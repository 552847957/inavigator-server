package ru.sbt.utils.backup.model;

public class Column {
	public final boolean identity;
	public final String columnName;
	public final String dataType;
	public final Boolean nullable;
	public final String columnDefault;
	
	public Column(boolean identity, String columnName, String dataType, Boolean isNullable, String columnDefault) {
		this.identity = identity;
		this.columnDefault = columnDefault;
		this.columnName = columnName;
		this.nullable = isNullable;
		this.dataType = dataType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((columnDefault == null) ? 0 : columnDefault.hashCode());
		result = prime * result + ((columnName == null) ? 0 : columnName.hashCode());
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + (identity ? 1231 : 1237);
		result = prime * result + ((nullable == null) ? 0 : nullable.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Column other = (Column) obj;
		if (columnDefault == null) {
			if (other.columnDefault != null)
				return false;
		} else if (!columnDefault.equals(other.columnDefault))
			return false;
		if (columnName == null) {
			if (other.columnName != null)
				return false;
		} else if (!columnName.equals(other.columnName))
			return false;
		if (dataType == null) {
			if (other.dataType != null)
				return false;
		} else if (!dataType.equals(other.dataType))
			return false;
		if (identity != other.identity)
			return false;
		if (nullable == null) {
			if (other.nullable != null)
				return false;
		} else if (!nullable.equals(other.nullable))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return columnName;
	}
	
	

}
