INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_TARGET5'              ,'mis-test'							,'Настройки Proxy TARGET 5'		,'имя сервиса (TARGET) для proxyMSSQLService. proxyDispatcherService будет использовать это имя для делегирования запроса нужному сервису');
INSERT INTO SYNC_CONFIG VALUES('MSSQL_URL5'                     ,'jdbc:sqlserver://finik2:1433;databaseName=MIS_IPAD_GENERATOR'		,'Настройки Proxy TARGET 5'		,'JDBC URL к источнику данных');
INSERT INTO SYNC_CONFIG VALUES('MSSQL_USER5'                    ,'mis_user'									,'Настройки Proxy TARGET 5'		,'имя пользователя источника данных');
INSERT INTO SYNC_CONFIG VALUES('MSSQL_PASSWORD5'                ,'mis'										,'Настройки Proxy TARGET 5'		,'Пароль пользователя источника данных');

set identity_insert SYNC_SERVICES on;
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY         ,START_ORDER,SERVLET_PATH , BEAN_DESC)
values(                   12             ,'proxy'       ,'proxyMSSQLService5'     ,'ru.sberbank.syncserver2.service.sql.MSSQLService'        ,'proxyDispatcherService' ,'subService'                 ,12          , null        ,'This service sends SQL to the database');
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY,START_ORDER         ,SERVLET_PATH , BEAN_DESC)
values(                   13             ,'proxy'       ,'proxySQLTemplateLoader5','ru.sberbank.syncserver2.service.sql.SQLTemplateLoader'   ,'proxyMSSQLService5'     ,'templateLoader'             ,13          , null        ,'This service loads templates from table SQL_TEMPLATES to memory');
set identity_insert SYNC_SERVICES off;

exec SP_SYNC_RESET_PROPERTIES 'proxy','proxyMSSQLService5';
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService5','mssqlURL'     ,'@MSSQL_URL5@';
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService5','mssqlUser'    ,'@MSSQL_USER5@';
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService5','mssqlPassword','@MSSQL_PASSWORD5@';
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService5','serviceName'  ,'@DATAPOWER_TARGET5@';
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService5','timeout'      ,'@MSSQL_TIMEOUT@';
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService5','forcedToTemplateUsage'      ,'@MSSQL_FORCED_TEMPLATE@';
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService5','maxIdle'      ,'8';
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService5','maxActive'      ,'24';
go
