package ru.sberbank.syncserver2.service.datamappers;

import ru.sberbank.syncserver2.service.sql.query.DatasetRow;

public interface DatapowerResponseMapper<T> {
	T convertResultToObject(DatasetRow row);
}