package ru.sberbank.syncserver2.service.sql;

import ru.sberbank.syncserver2.service.sql.query.DataResponse;
import ru.sberbank.syncserver2.service.sql.query.OnlineRequest;


public interface SQLService {
    String PROVIDER_SQLITE     = "SQLITE";
    String PROVIDER_DATAPOWER  = "DATAPOWER";
    String PROVIDER_MSSQL      = "MSSQL";
    String PROVIDER_DISPATCHER = "DISPATCHER";
    DataResponse request(OnlineRequest request);
}
