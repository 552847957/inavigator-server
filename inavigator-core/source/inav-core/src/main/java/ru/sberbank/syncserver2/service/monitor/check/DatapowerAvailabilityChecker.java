package ru.sberbank.syncserver2.service.monitor.check;

import ru.sberbank.syncserver2.service.core.ServiceManager;
import ru.sberbank.syncserver2.service.datamappers.DatapowerDataResponseHandler;
import ru.sberbank.syncserver2.service.datamappers.DatapowerObjectMapperRequester;
import ru.sberbank.syncserver2.service.sql.DataPowerService;
import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.DataResponse.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * проблема этого чекера в том, что проверка происходит на стороне сигма части
 * а сообщение отсылается в альфу, а если ДП не работает, то этого не произойдет
 *
 * Заменен сервисом на стороне альфа AlphaDatapowerAvailabilityCheck
 * @author Ilya
 *
 */
public class DatapowerAvailabilityChecker extends AbstractCheckAction {
	public static final String CHECK_RESULT_CODE_DATAPOWER_IS_NULL = "DATAPOWER_IS_NULL";
	public static final String CHECK_RESULT_CODE_REQUEST_TO_DATAPOWER_WAS_FAILED = "REQUEST_ERROR";
	private String sqlRequest = "DATAPOWER_TEST";

	private String connectionService;
	{
		setDefaultDoNotNotifyCount(4);
	}

	@Override
	protected List<CheckResult> doCheck() {
		final List<CheckResult> result = new ArrayList<CheckResult>();
		DataPowerService DPService = (DataPowerService)ServiceManager.getInstance().findFirstServiceByClassCode(DataPowerService.class);

		if (DPService == null) {
			result.add(new CheckResult(CHECK_RESULT_CODE_DATAPOWER_IS_NULL, false, LOCAL_HOST_NAME+": DataPowerService is null"));
			tagLogger.log("DataPowerService is null");
			return result;
		} else {
			addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_DATAPOWER_IS_NULL, LOCAL_HOST_NAME+" says: DataPowerService has been found");
		}

		try {
			new DatapowerObjectMapperRequester<String>().request(sqlRequest, null, connectionService, DPService, new DatapowerDataResponseHandler() {

				@Override
				public void handleDataResponse(DataResponse dataset) throws IOException {
					if (dataset.getResult()!=Result.OK) {
						result.add(new CheckResult(CHECK_RESULT_CODE_REQUEST_TO_DATAPOWER_WAS_FAILED, false, LOCAL_HOST_NAME+" says: Datapower is not available. Error from DataPowerService: "+dataset.getError()));
						tagLogger.log("Request to DataPower completed with error: "+dataset.getError());
					} else {
						if (addSuccessfullCheckResultIfPreviousFailed(result, CHECK_RESULT_CODE_REQUEST_TO_DATAPOWER_WAS_FAILED, LOCAL_HOST_NAME+" says: Datapower is now available")) {
							tagLogger.log("Request to DataPower completed successfully");
						}
					}
				}
			});

		} catch (IOException e) {
			//по логике, исполнение не может зайти сюда, т.к. handleDataResponse не бросает такого исключения.
			result.add(new CheckResult(CHECK_RESULT_CODE_REQUEST_TO_DATAPOWER_WAS_FAILED, false, LOCAL_HOST_NAME+" says: Error while processing request to DataPower: "+e.getMessage()));
			tagLogger.log("Request to DataPower completed with error: "+e.getMessage());
		}

		return result;
	}

	public String getConnectionService() {
		return connectionService;
	}

	public void setConnectionService(String connectionService) {
		this.connectionService = connectionService;
	}

	@Override
	public String getDescription() {
		return "Чекер проверки доступности DataPower";
	}

}
