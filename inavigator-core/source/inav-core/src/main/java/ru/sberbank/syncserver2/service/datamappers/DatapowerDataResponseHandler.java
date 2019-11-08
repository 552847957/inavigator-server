package ru.sberbank.syncserver2.service.datamappers;

import ru.sberbank.syncserver2.service.sql.query.DataResponse;

import java.io.IOException;

public interface DatapowerDataResponseHandler {
	public void handleDataResponse(DataResponse dataset) throws IOException;
}
