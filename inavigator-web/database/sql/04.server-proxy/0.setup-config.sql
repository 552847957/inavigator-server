go
if object_id('SYNC_CONFIG') is not null DROP TABLE SYNC_CONFIG
GO
CREATE TABLE SYNC_CONFIG(
   PROPERTY_KEY    VARCHAR(256) PRIMARY KEY,
   PROPERTY_VALUE  VARCHAR(1024) NOT NULL, 
   PROPERTY_GROUP  VARCHAR(128),
   PROPERTY_DESC   VARCHAR(1024) 
)
GO
SET NOCOUNT ON
INSERT INTO SYNC_CONFIG VALUES('ROOT_FOLDER'                    ,'E:/Disk_E/mis_file/cacheUAT/proxy'		,NULL		,'Корневая папка для локального кэша')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_TIMEOUT'                  ,'120'										,NULL		,'Максимальное время выполнения запроса в MSSQL для всех TARGET-ов в секундах')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_FORCED_TEMPLATE'          ,'true'										,NULL		,'Принудительное использование шаблонов в запросах для всех TARGET-ов MSSQL')
INSERT INTO SYNC_CONFIG VALUES('IS_DB_LOGGING_ENABLED'          ,'true'										,NULL		,'Включить/выключить логирование в БД. Скрыто используется сервисом adminDbLogService.')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_TARGET1'              ,'finik1-new'								,'Настройки Proxy TARGET 1'		,'имя сервиса (TARGET) для proxyMSSQLService. proxyDispatcherService будет использовать это имя для делегирования запроса нужному сервису')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_URL1'                     ,'jdbc:sqlserver://finik1:1433;databaseName=MIS_IPAD_PROXYSERVER'	,'Настройки Proxy TARGET 1'		,'JDBC URL к источнику данных')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_USER1'                    ,'mis_user'									,'Настройки Proxy TARGET 1'		,'имя пользователя источника данных')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_PASSWORD1'                ,'mis'										,'Настройки Proxy TARGET 1'		,'Пароль пользователя источника данных')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_TARGET2'              ,'finik2-new'								,'Настройки Proxy TARGET 2'		,'имя сервиса (TARGET) для proxyMSSQLService. proxyDispatcherService будет использовать это имя для делегирования запроса нужному сервису')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_URL2'                     ,'jdbc:sqlserver://finik2:1433;databaseName=MIS_IPAD_PROXYSERVER'	,'Настройки Proxy TARGET 2'		,'JDBC URL к источнику данных')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_USER2'                    ,'mis_user'									,'Настройки Proxy TARGET 2'		,'имя пользователя источника данных')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_PASSWORD2'                ,'mis'										,'Настройки Proxy TARGET 2'		,'Пароль пользователя источника данных')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_TARGET3'              ,'finik4-new'								,'Настройки Proxy TARGET 3'		,'имя сервиса (TARGET) для proxyMSSQLService. proxyDispatcherService будет использовать это имя для делегирования запроса нужному сервису')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_URL3'                     ,'jdbc:sqlserver://finik4:1433;databaseName=MIS_RUBRICATOR'			,'Настройки Proxy TARGET 3'		,'JDBC URL к источнику данных')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_USER3'                    ,'mis_user'									,'Настройки Proxy TARGET 3'		,'имя пользователя источника данных')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_PASSWORD3'                ,'mis'										,'Настройки Proxy TARGET 3'		,'Пароль пользователя источника данных')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_TARGET4'              ,'mis-generator'							,'Настройки Proxy TARGET 4'		,'имя сервиса (TARGET) для proxyMSSQLService. proxyDispatcherService будет использовать это имя для делегирования запроса нужному сервису')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_URL4'                     ,'jdbc:sqlserver://finik2:1433;databaseName=MIS_IPAD_GENERATOR'		,'Настройки Proxy TARGET 4'		,'JDBC URL к источнику данных')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_USER4'                    ,'mis_user'									,'Настройки Proxy TARGET 4'		,'имя пользователя источника данных')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_PASSWORD4'                ,'mis'										,'Настройки Proxy TARGET 4'		,'Пароль пользователя источника данных')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SOURCE_DB'                ,'MIS_IPAD'									,'Названия БД (используются шаблонами)'		,'Название БД (на finik1/2) с процедурой проверки прав SP_IS_ALLOWED_TO_DOWNLOAD_FILE and SP_IS_ALLOWED_TO_USE_APP')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_DB'               ,'MIS_IPAD_MONITOR'							,'Названия БД (используются шаблонами)'		,'Название БД приложения Alpha Monitor')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_GENERATOR_DB'             ,'MIS_IPAD_GENERATOR'						,'Названия БД (используются шаблонами)'		,'Название БД приложения Generator')
GO

