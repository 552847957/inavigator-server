package ru.sberbank.syncserver2.service.datamappers;

import ru.sberbank.syncserver2.service.sql.DataPowerService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;
import ru.sberbank.syncserver2.service.sql.query.DatasetRow;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest.Arguments;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest.Arguments.Argument;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DatapowerObjectMapperRequester<T> {

	private OnlineRequest makeRequest(String sqlRequest,List<Argument> arguments,String alphaConnectionService) {
		OnlineRequest dpRequest = new OnlineRequest();
		dpRequest.setStoredProcedure(sqlRequest);
		dpRequest.setProvider("DISPATCHER");
		dpRequest.setService(alphaConnectionService);
		Arguments requestArguments = new Arguments();
		requestArguments.setArgument(arguments);
		dpRequest.setArguments(requestArguments);
		return  dpRequest;
	}

	private DataResponse makeDatapowerRequest(DataPowerService dpService, OnlineRequest request) {
		return dpService.request(request);
	}

	private List<T> requestObjects(DataPowerService dpService,OnlineRequest request,DatapowerResponseMapper<T> mapper) {
		List<T> results = new ArrayList<T>();
		DataResponse dpResponse = makeDatapowerRequest(dpService, request);
		if (dpResponse.getResult() != Result.OK) return null;


		List<DatasetRow> rows = dpResponse.getDataset().getRows();

		if (rows != null)
			for(DatasetRow row:rows) {
				results.add(mapper.convertResultToObject(row));
			}

		return results;
	}

	public void request(String sqlRequest,List<Argument> arguments,String alphaConnectionService,DataPowerService dpService,DatapowerResponseMapper<T> objectMapper,DatapowerResultObjectListHandler<T> resultHandler) throws IOException {
		OnlineRequest dpRequest = makeRequest(sqlRequest, arguments, alphaConnectionService);
		List<T> resultObjects = requestObjects(dpService, dpRequest,objectMapper);
		if (resultHandler != null)
			resultHandler.handleResultObjectList(resultObjects);
	}

	public void request(String sqlRequest,List<Argument> arguments,String alphaConnectionService,DataPowerService dpService,DatapowerDataResponseHandler resultHandler) throws IOException {
		OnlineRequest dpRequest = makeRequest(sqlRequest, arguments, alphaConnectionService);
		DataResponse dpResponse = makeDatapowerRequest(dpService, dpRequest);
		if (resultHandler != null)
			resultHandler.handleDataResponse(dpResponse);
	}


}
