--1. Deleting old, it simplifies restart
DELETE FROM SYNC_SERVICES
DELETE FROM SYNC_FOLDERS
go

--2. Filling templates for services
--2.1. For MSSQLService
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,  'ru.sberbank.syncserver2.service.sql.MSSQLService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'mssqlURL'     ,'N', 'JDBC Url to source database'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'mssqlUser'    ,'N', 'MSSQL User to source database'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'mssqlPassword','N', 'MSSQL Password to source database'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'serviceName'  ,'N', 'This parameter used for distribution request between several services according to the content of tag "service" in online request'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'timeout'      ,'N', 'Query timeout in seconds'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'forcedToTemplateUsage'      ,'N', 'Принудительное использование шаблонов в запросах'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'maxIdle'      ,'N', 'The maximum number of connections that can remain idle in the pool, without extra ones being released'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'maxActive'    ,'N', 'The maximum number of active connections that can be allocated from this pool at the same time'
--exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'maxWait'      ,'N', 'The maximum number of milliseconds that the pool will wait for a connection to be returned before throwing an exception'
--exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'poolingStatements'      ,'N', 'Prepared statement pooling for this pool'
--exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'maxOpenStatements'      ,'N', 'The maximum number of open statements that can be allocated from the statement pool at the same time'
--exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'minIdle'      ,'N', 'The minimum number of active connections that can remain idle in the pool, without extra ones being created'
go

--2.2. For SQLTemplateLoader
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.sql.SQLTemplateLoader'
go

--2.3. For SQLDispatcherService
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.sql.SQLDispatcherService'
go

--2.4. For SQLService
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.sql.SQLPublicService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'conversion'  ,'N', 'This parameter used to provide compatibility of DataPower'
go

--3. Filling list of folders
set identity_insert SYNC_FOLDERS on
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(1,'proxy'   , 1,'This folder contains all services but admin services')
set identity_insert SYNC_FOLDERS off

--4. Filing list of services
set identity_insert SYNC_SERVICES on                                          
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY         ,START_ORDER,SERVLET_PATH , BEAN_DESC)
values(                   1             ,'proxy'       ,'proxyMSSQLService1'     ,'ru.sberbank.syncserver2.service.sql.MSSQLService'        ,'proxyDispatcherService' ,'subService'                 ,1          , null        ,'This service sends SQL to the database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY,START_ORDER         ,SERVLET_PATH , BEAN_DESC)
values(                   2             ,'proxy'       ,'proxySQLTemplateLoader1','ru.sberbank.syncserver2.service.sql.SQLTemplateLoader'   ,'proxyMSSQLService1'     ,'templateLoader'             ,2          , null        ,'This service loads templates from table SQL_TEMPLATES to memory')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY         ,START_ORDER,SERVLET_PATH , BEAN_DESC)
values(                   3             ,'proxy'       ,'proxyMSSQLService2'     ,'ru.sberbank.syncserver2.service.sql.MSSQLService'        ,'proxyDispatcherService' ,'subService'                 ,3          , null        ,'This service sends SQL to the database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY,START_ORDER         ,SERVLET_PATH , BEAN_DESC)
values(                   4             ,'proxy'       ,'proxySQLTemplateLoader2','ru.sberbank.syncserver2.service.sql.SQLTemplateLoader'   ,'proxyMSSQLService2'     ,'templateLoader'             ,4          , null        ,'This service loads templates from table SQL_TEMPLATES to memory')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY         ,START_ORDER,SERVLET_PATH , BEAN_DESC)
values(                   7             ,'proxy'       ,'proxyMSSQLService3'     ,'ru.sberbank.syncserver2.service.sql.MSSQLService'        ,'proxyDispatcherService' ,'subService'                 ,7          , null        ,'This service sends SQL to the database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY,START_ORDER         ,SERVLET_PATH , BEAN_DESC)
values(                   8             ,'proxy'       ,'proxySQLTemplateLoader3','ru.sberbank.syncserver2.service.sql.SQLTemplateLoader'   ,'proxyMSSQLService3'     ,'templateLoader'             ,8          , null        ,'This service loads templates from table SQL_TEMPLATES to memory')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY         ,START_ORDER,SERVLET_PATH , BEAN_DESC)
values(                   9             ,'proxy'       ,'proxyMSSQLService4'     ,'ru.sberbank.syncserver2.service.sql.MSSQLService'        ,'proxyDispatcherService' ,'subService'                 ,9          , null        ,'This service sends SQL to the database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY,START_ORDER         ,SERVLET_PATH , BEAN_DESC)
values(                   10             ,'proxy'       ,'proxySQLTemplateLoader4','ru.sberbank.syncserver2.service.sql.SQLTemplateLoader'   ,'proxyMSSQLService4'     ,'templateLoader'             ,10          , null        ,'This service loads templates from table SQL_TEMPLATES to memory')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             , BEAN_CLASS                                               ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY         ,START_ORDER,SERVLET_PATH , BEAN_DESC)
values(                   5             ,'proxy'       ,'proxyDispatcherService' ,'ru.sberbank.syncserver2.service.sql.SQLDispatcherService','proxySQLService'        ,'sqlDispatcherService'       ,5          , null        , 'This service accepts requests from proxySQLService and dispatch them to proxyMSSQLService1 and proxyMSSQLService2')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY         ,START_ORDER,SERVLET_PATH , BEAN_DESC)
values(                   6             ,'proxy'       ,'proxySQLService'        ,'ru.sberbank.syncserver2.service.sql.SQLPublicService'          ,null                     ,null                         ,6          , 'online.sql', 'This service accepts requests from DataPower')
go
set identity_insert SYNC_SERVICES off
go


--4. Filling properties
--4.1. For misMSSQLService
exec SP_SYNC_RESET_PROPERTIES 'proxy','proxyMSSQLService1'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','mssqlURL'     ,'@MSSQL_URL1@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','mssqlUser'    ,'@MSSQL_USER1@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','mssqlPassword','@MSSQL_PASSWORD1@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','serviceName'  ,'@DATAPOWER_TARGET1@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','timeout'      ,'@MSSQL_TIMEOUT@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','forcedToTemplateUsage'      ,'@MSSQL_FORCED_TEMPLATE@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','maxIdle'      ,'8'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','maxActive'      ,'24'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','maxWait'      ,'-1'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','poolingStatements'      ,'false'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','maxOpenStatements'      ,'-1'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','minIdle'      ,'0'
go
exec SP_SYNC_RESET_PROPERTIES 'proxy','proxyMSSQLService2'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','mssqlURL'     ,'@MSSQL_URL2@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','mssqlUser'    ,'@MSSQL_USER2@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','mssqlPassword','@MSSQL_PASSWORD2@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','serviceName'  ,'@DATAPOWER_TARGET2@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','timeout'      ,'@MSSQL_TIMEOUT@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','forcedToTemplateUsage'      ,'@MSSQL_FORCED_TEMPLATE@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','maxIdle'      ,'8'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','maxActive'      ,'24'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','maxWait'      ,'-1'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','poolingStatements'      ,'false'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','maxOpenStatements'      ,'-1'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','minIdle'      ,'0'

go
exec SP_SYNC_RESET_PROPERTIES 'proxy','proxyMSSQLService3'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','mssqlURL'     ,'@MSSQL_URL3@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','mssqlUser'    ,'@MSSQL_USER3@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','mssqlPassword','@MSSQL_PASSWORD3@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','serviceName'  ,'@DATAPOWER_TARGET3@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','timeout'      ,'@MSSQL_TIMEOUT@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','forcedToTemplateUsage'      ,'@MSSQL_FORCED_TEMPLATE@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','maxIdle'      ,'8'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','maxActive'      ,'24'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','maxWait'      ,'-1'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','poolingStatements'      ,'false'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','maxOpenStatements'      ,'-1'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','minIdle'      ,'0'

go

exec SP_SYNC_RESET_PROPERTIES 'proxy','proxyMSSQLService4'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','mssqlURL'     ,'@MSSQL_URL4@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','mssqlUser'    ,'@MSSQL_USER4@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','mssqlPassword','@MSSQL_PASSWORD4@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','serviceName'  ,'@DATAPOWER_TARGET4@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','timeout'      ,'@MSSQL_TIMEOUT@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','forcedToTemplateUsage'      ,'@MSSQL_FORCED_TEMPLATE@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','maxIdle'      ,'8'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','maxActive'      ,'24'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','maxWait'      ,'-1'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','poolingStatements'      ,'false'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','maxOpenStatements'      ,'-1'
--exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','minIdle'      ,'0'

go

exec SP_SYNC_RESET_PROPERTIES 'proxy','proxySQLService'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxySQLService','conversion'  ,'CONVERT_TO_OLD_ALPHA'
go
