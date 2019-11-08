package ru.sberbank.syncserver2.service.datamappers;

import java.io.IOException;
import java.util.List;

public interface DatapowerResultObjectListHandler<T> {
	public void handleResultObjectList(List<T> results) throws IOException;
}
