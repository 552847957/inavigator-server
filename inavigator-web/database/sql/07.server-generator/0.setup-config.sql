go
set nocount on
go
IF OBJECT_ID('SYNC_CONFIG') IS NOT NULL DROP TABLE SYNC_CONFIG
GO
CREATE TABLE SYNC_CONFIG(
   PROPERTY_KEY    VARCHAR(256) PRIMARY KEY,
   PROPERTY_VALUE  VARCHAR(1024) NOT NULL,
   PROPERTY_GROUP  VARCHAR(128),
   PROPERTY_DESC   VARCHAR(1024)
)
GO
SET NOCOUNT ON
INSERT INTO SYNC_CONFIG VALUES('ROOT_FOLDER'                        ,'C:/Disk_E/mis_file/cacheUAT'						,NULL		,'Корневая папка для локального кэша')
INSERT INTO SYNC_CONFIG VALUES('NETWORK_TEMP_FOLDER'                ,'\\\\bronze2\\vol1\\i-navigator\\IN\\uat4\\temp'	,NULL		,'Корневая папка на файлоперекладчике для временных файлов')
INSERT INTO SYNC_CONFIG VALUES('NETWORK_ROOT_FOLDER'                ,'\\\\bronze2\\vol1\\i-navigator\\OUT\\uat4'		,NULL		,'Корневая папка на файлоперекладчике для файлов, которые должны быть перенесены в Sigma')
INSERT INTO SYNC_CONFIG VALUES('NETWORK_SHARED_HOSTS_FOR_CHANGESETS','localhost'										,NULL		,'Список хостов для iRubricator через ";". Changeset-ы будут загружены только на указанные сервера')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_URL2'                         ,'jdbc:sqlserver://finik2:1433;databaseName=mis_rubricator'	,'Настройки источника данных'		,'JDBC URL к источнику данных (DataSource) для generator-а')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_USER2'                        ,'mis_user'											,'Настройки источника данных'		,'имя пользователя источника данных')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_PASSWORD2'                    ,'mis'												,'Настройки источника данных'		,'Пароль пользователя источника данных')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_DB'                   ,'MIS_IPAD_MONITOR2'								,NULL		,'имя базы данных Alpha Monitor-а. Эта БД должна быть на том же MSSQL Server что и источник данных jdbc/GENERATOR_DB у этой WebSphere')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SOURCE_DB'                    ,'MIS_IPAD2'										,NULL		,'имя базы данных источника (на finik1/finik2). Обычно здесь должна быть указана БД MIS_IPAD или MIS_IPAD2 (скрыто используется зависимостями генератора')
INSERT INTO SYNC_CONFIG VALUES('FILE_FRAGMENT_SIZE_MB'              ,'1'												,NULL		,'Максимальных размер фрагмента для офлайн файлов')
INSERT INTO SYNC_CONFIG VALUES('LOGGING_SERVICE'                    ,'SynchronousDbLogService'							,NULL		,'Сервис логирования. Допустимые значения: SynchronousDbLogService и DbLogService.')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_FILE_MOVER_DUPLICATE_FOLDER'  ,''							,'Настройки ALPHA'		,'Если указано, то файлы дополнительно копируются в эту папку при переносе на файл перекладчик главной нодой кластера.')
GO
