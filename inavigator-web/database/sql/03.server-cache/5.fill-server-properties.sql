---0. Addomg additional role to employee
INSERT INTO [EMPLOYEE_ROLES] VALUES(3,'OperatorMISAccess')
INSERT INTO [EMPLOYEES] ([EMPLOYEE_ROLE_ID],[EMPLOYEE_EMAIL], [EMPLOYEE_NAME], [leave_date], [EMPLOYEE_PASSWORD],[PASSWORD_CHANGED_DATE]) VALUES (3,'operator', 'operator', null, 'E10ADC3949BA59ABBE56E057F20F883E',getdate())
GO

--1. Deleting old, it simplifies restart
DELETE FROM SYNC_SERVICES
DELETE FROM SYNC_FOLDERS
go

--2. Filling templates of services and templates of properties
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.transport.SharedSigmaNetworkFileMover'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'networkSourceFolder','N','The folder at file transporter with files copied from Sigma'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder','N','The folder at local disk used for temporary storage while moving file from file transporter to local disk'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localDestFolder','N','After file is fully copied and MD5 is verified, file is copied to this folder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'networkSharedFolder','N','Before moving file to a local disk, file is moved to this folder at file transporter. It is required to prevent overwriting of file by next update from Alpha'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'staticSharedHosts','Y','The list of hosts the file should be delivered to. It is possible to use this param of FileLister defined in sharedHostsListerCode'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'sharedHostsListerCode','Y','The link to file lister service. It is required to identify the hosts the file should be moved to. This param could be undefined and staticSharedHosts could be defined'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'debugModeWithSMSOnDelivery','Y','This parameter could be used for debugging only'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.transport.LocalInflater'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localSourceFolder','N','After file appears in localSourceFolder the service moves it to localTempFolder1'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder1' ,'N','After file appears in this folder the service starts inflating file to a filer with same name in localTempFolder2'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder2' ,'N','This folder is used to store inflated files while process of inflation. After inflation finished the file is copied to localDestFolder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localDestFolder'  ,'N','The service moves to this folder completed inflated file'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.transport.LocalDeflater'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localSourceFolder','N','After file appears in localSourceFolder the service moves it to localTempFolder1'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder1' ,'N','After file appears in this folder the service starts deflating file to a filer with same name in localTempFolder2'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder2' ,'N','This folder is used to store deflated files while the deflation. After deflation finished the file is copied to localDestFolder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localDestFolder'  ,'N','The service moves to this folder completed inflated file'
go



declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.transport.LocalFileMover'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'srcFolder','N','Source folder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dstFolder','N','Destination folder'
go


declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,  'ru.sberbank.syncserver2.service.file.transport.LocalFileCopier'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'srcFolder' ,'N','Source folder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dstFolder1','N','Destination folder #1. Should be defined'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dstFolder2','Y','Destination folder #1. Could be undefined'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dstFolder3','Y','Destination folder #1. Could be undefined'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dstFolder4','Y','Destination folder #1. Could be undefined'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dstFolder5','Y','Destination folder #1. Could be undefined'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.cache.SingleFileLoader'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'inboxFolder'  ,'Y','The folder for incoming files'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'tempFolder'   ,'N','When file appears in incomingFolder, they are moved to tempFolder to prevent overwriting during splitting and loading'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'archiveFolder','N','After file is splitted'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'cacheFolder'  ,'N','This folder contains subfolders with content and files with information about loaded files'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'chunkSize'    ,'Y','The chunk size'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.cache.FileCache'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'debugModeWithoutLoadToMemory','Y','This option could be used for debugging purposes only'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.cache.FileCacheDraftSupported'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'debugModeWithoutLoadToMemory','Y','This option could be used for debugging purposes only'
go


declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.file.FileService'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.sql.SQLiteService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localIncomingFile','N','The folder the file should arrive to'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localWorkFolder'  ,'N','The file will be moved and used in this folder'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.sql.DataPowerService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'dataPowerURL1'   ,'N','URL of DataPower #1'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'dataPowerURL2'   ,'N','URL of DataPower #1'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'overrideProvider','N','Always should be equal DISPATCHER. Other values are deprecated'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'overrideService1','N','The name of host in Alpha the request is sent to'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'overrideService2','N','The name of host in Alpha the request is sent to'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'conversion'     ,'N','This parameter used to provide compatibility of DataPower'
go



declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.security.DataPowerSecurityService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dataPowerServiceBeanCode','N','Link to DataPower service'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'isAllowedToUseApp'       ,'N','Template for checking permission to use application'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'isAllowedToDownloadFile' ,'N','Template for checking permission to download file'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id ,'provider'                ,'N','SQLService provider, should be DISPATCHER'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id ,'service'                 ,'N','Database is same for both proxy services so there is no difference'

go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.security.SessionCachedSecurity'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'sessionTimeoutSeconds','Y','We do not send request to Alpha to recheck permissions during this time and use cached result'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.log.LogService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'logDbURL'     ,'N','JDBC Url to the database used for logging'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'logDbUser'    ,'N','User for database used for logging'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'logDbPassword','N','Password for database used for logging'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.log.DataPowerLogService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'dataPowerBeanCode','N','Link to datapowerService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'logSQL'           ,'N','SQL for saving logs to Alpha Generator database'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'provider'         ,'N','SQLService provider, should be DISPATCHER'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'service'          ,'N','Database is same for both proxy services so there is no difference'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.cache.list.DynamicFileLister'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'statusFile','N','The path to status file'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.cache.zip.MbrUnzipper'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'inboxFolder'  ,'Y','The folder for incoming files'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'tempFolder'   ,'N','When file appears in incomingFolder, they are moved to tempFolder to prevent overwriting during splitting and loading'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'archiveFolder','N','After file is splitted'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'cacheFolder'  ,'N','This folder contains subfolders with content and files with information about loaded files'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'chunkSize'    ,'Y','The chunk size'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.security.LdapUserCheckerServiceGroup'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'provider','N','URL to ldap server'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'domain'  ,'N','Windows Domain'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'username','N','User name for connection to LDAP server'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'password','N','Password for connection to LDAP server'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dataPowerBeanCode'		,'N','Link to dataPowerService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'databaseName'     		,'N','The name of database used by Alpha Monitor'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'databaseGeneratorName'	,'N','The name of database used by Alpha Generator'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id ,'provider'         		,'N','SQLService provider, should be DISPATCHER'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id ,'service'          		,'N','SQL Service at SQL Proxy Server for writing to generator database'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.file.diff.CacheDiffChecker'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dataPowerBeanCode','N','Link to dataPowerService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'fileCacheBean'    ,'N','The link to file cache bean'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.pushnotifications.PushNotificationService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'amount'	, 'N','Number of messages to load and send at one time'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.pushnotifications.senders.ApplePushNotificationSender'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'certificatesFolder'	, 'N','Path to config folder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'maxPayloadSize'	, 'N','Maximum payload size'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.file.cache.SingleFileStatusCacheService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'fileStatusRequestTemplateName','N','File status template name'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'userGroupRequestTemplateName'    ,'N','User group template name'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'fileCacheBeanCode'    ,'N','Bean code of filecache'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'datapowerServiceBeanCode'    ,'N','Bean code of datapower service'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'generatorServiceName'    ,'N','generator Target service name'
go


declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.core.event.impl.SigmaEventHandler'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'sendPushNotificationWhenFileLoaded'    ,'N','send push notification when new file loaded or not'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.ldap.LdapGroupManagementService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'provider'    ,'N','provider for ldap connection'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'domain'    ,'N','domain for ldap connection'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'settings'    ,'N','other settings'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.sql.MaintenanceService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'hosts'    ,'N','Hosts for status update'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.sql.SQLRequestAndCertificateEmailVerifier'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'skipEmailVerification'    ,'N','Пропускать сверку email'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'skipEmailVerificationIps', 'N', 'Список ip адресов, для которых не делается проверка email'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.pushnotifications.DataPowerPushnotificationsUploader'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'connectionService'          ,'N','SQL Service at SQL Proxy Server for getting push notifications'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'appName'          ,'N','Application name for push notifications'
go


--3. Filling list of folders
set identity_insert SYNC_FOLDERS on
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(1,'online'   , 1,'This folder contains services for online SQL requests and datapower services')
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(2,'single'   , 2,'This folder contains file cache for single files with known names and related services')
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(3,'changeset', 3,'This folder contains file cache for rubricator files with dynamic names and related services')
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(4,'common',    4,'This folder contains common services')
set identity_insert SYNC_FOLDERS off
go


--4. Filing list of services
--4.1. Filling online and security services
set identity_insert SYNC_SERVICES on
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE    ,PARENT_BEAN_PROPERTY, START_ORDER,SERVLET_PATH    , BEAN_DESC)
values(                   1              ,'online'        ,'misDataPowerService' ,'ru.sberbank.syncserver2.service.sql.DataPowerService'                          ,'misSQLService'     ,'dataPowerService'  , 1          , null            ,'This service sends requests to DataPower')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE    ,PARENT_BEAN_PROPERTY, START_ORDER,SERVLET_PATH    , BEAN_DESC)
values(                   2              ,'online'        ,'misSQLiteNetworkMover','ru.sberbank.syncserver2.service.file.transport.SharedSigmaNetworkFileMover'   ,null                ,null                , 2          , null           ,'This service moves SQLite file for online queries from file transporter to local disk. Not in use at 01.07.2014' )
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE    ,PARENT_BEAN_PROPERTY, START_ORDER,SERVLET_PATH    , BEAN_DESC)
values(                   3              ,'online'        ,'misSQLiteService'    ,'ru.sberbank.syncserver2.service.sql.SQLiteService'                             ,'misSQLService'     ,'sqliteService'     , 3          , null           ,'This service process online requests to SQLite')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE    ,PARENT_BEAN_PROPERTY, START_ORDER,SERVLET_PATH    , BEAN_DESC)
values(                   4              ,'online'           ,'misSQLService'       ,'ru.sberbank.syncserver2.service.sql.SQLPublicService'                      ,'requestEmailVerifier'    ,'originalService'     , 4          , null   ,'This service accepts online requests from iPad and distribute it to other online services')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE    ,PARENT_BEAN_PROPERTY, START_ORDER,SERVLET_PATH    , BEAN_DESC)
values(                   5              ,'online'        ,'dpLogger'             ,'ru.sberbank.syncserver2.service.log.DataPowerLogService'                       ,null               ,null                , 5          , null           ,'This service used for logging events to Alpha databases and it uses datapower to send logging information to Alpha')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE               ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   6              ,'common'        , 'clusterManager'       ,'ru.sberbank.syncserver2.service.generator.ClusterManager'           			,null            ,null                ,6          , 'This service helps other services to understand whether this node of cluster is active node or passive node')

--4.2. Filling common security services
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY        ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   10             ,'online'        ,'misDataPowerSecurity' ,'ru.sberbank.syncserver2.service.security.DataPowerSecurityService'            ,'misCachedSecurity','originalSecurityService',10         ,null        , 'This service used for checking permissions. It sends requests to datapower service to check permissions')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY        ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   11             ,'online'        ,'misCachedSecurity'    ,'ru.sberbank.syncserver2.service.security.SessionCachedSecurity'               ,'misFileService','securityService'           ,11         ,null        , 'This service caches the result of permission check')

--4.3. Filling datapower notification logger
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY        ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   12             ,'online'        ,'misNotificationLogger','ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger'          ,null            ,null                        ,12         ,null        , 'This service sends notifications to Alpha using datapower')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY        ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   13             ,'online'        ,'misLdapUserCheckerService','ru.sberbank.syncserver2.service.security.LdapUserCheckerServiceGroup',null            ,null                        ,13         , null       , 'This service requests LDAP to check user certificate')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY        ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   14             ,'online'        ,'misRequestLogger','ru.sberbank.syncserver2.service.sql.SQLRequestLogService'					,'misMaintenanceService'  ,'originalService'                        ,14         , null       , 'This service used for logging online requests with error')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY        ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   15             ,'online'        ,'misMaintenanceService','ru.sberbank.syncserver2.service.sql.MaintenanceService'						  ,null            ,null                        ,15         , 'online.sql'       , 'This service used for maintenance')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY        ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   16             ,'online'        ,'requestEmailVerifier','ru.sberbank.syncserver2.service.sql.SQLRequestAndCertificateEmailVerifier'	,'misRequestLogger'       ,'originalService'  ,16         , null       , 'Сервис используется для сверки email в запросе и в клиентском сертификате')

--4.4. Filling single services
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY    ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   20             ,'single'        ,'misNetworkMover'      ,'ru.sberbank.syncserver2.service.file.transport.SharedSigmaNetworkFileMover'   ,null            ,null                    ,100        ,null        , 'This service moves files from file transporter to local disk. It designed to work in cluster so that several services from several hosts could move same file to their hosts')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY    ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   21             ,'single'        ,'misLocalInflater'    ,'ru.sberbank.syncserver2.service.file.transport.LocalInflater'                  ,null            ,null                    ,21         ,null        , 'This service inflates file at local disk')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   22             ,'single'        ,'misDbFileLister'        ,'ru.sberbank.syncserver2.service.file.cache.list.DatabaseFileLister'         ,'misFileLoader'       ,'fileLister'      ,22         ,null           , 'This service queries database to get hostnames the file should be moved to')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY    ,START_ORDER,SERVLET_PATH   , BEAN_DESC)
values(                   23             ,'single'        ,'misFileLoader'        ,'ru.sberbank.syncserver2.service.file.cache.SingleFileLoader'                  ,'misFileCache'  ,'loader'                ,23         ,null           , 'This service split files to chunks and loads them to memory (to fileCache service)' )
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY    ,START_ORDER,SERVLET_PATH   , BEAN_DESC)
values(                   24             ,'single'        ,'misFileCache'        ,'ru.sberbank.syncserver2.service.file.cache.FileCacheDraftSupported'            ,'misFileService','fileCache'             ,24         ,null           , 'This service stores data in memory and reply to requests from misFileService')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY    ,START_ORDER,SERVLET_PATH   , BEAN_DESC)
values(                   25             ,'single'        ,'misFileService'       ,'ru.sberbank.syncserver2.service.file.FileService'                             ,null           ,null                    ,25         , 'file.do'      , 'This service accepts requests from iPad')
                                                                                                                                                                                                            
--4.5. Filling changeset services
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   30             ,'changeset'     ,'misChangesetNetworkMover','ru.sberbank.syncserver2.service.file.transport.SharedSigmaNetworkFileMover',null                  ,null                 ,30         , null       , 'This service moves files from file transporter to local disk. It designed to work in cluster so that several services from several hosts could move same file to their hosts')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   32             ,'changeset'     ,'misDynFileLister'        ,'ru.sberbank.syncserver2.service.file.cache.list.DynamicFileLister'         ,'misMbrLoader'        ,'fileLister'         ,32         , null       , 'This file helps fileCache to do dynamic listing of rubricator files')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   33             ,'changeset'     ,'misMbrLoader'         ,'ru.sberbank.syncserver2.service.file.cache.zip.MbrUnzipper'                   ,'changesetFileCache'  ,'loader'             ,33         , null       , 'This service extract files from changesets and split files to chunks and loads them to memory (to fileCache service)')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   34             ,'changeset'     ,'changesetFileCache'        ,'ru.sberbank.syncserver2.service.file.cache.FileCache'                    ,'changesetFileService','fileCache'          ,34         , null       , 'This file stores data in memory and reply to requests from changesetFileService')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   35             ,'changeset'     ,'changesetFileService'       ,'ru.sberbank.syncserver2.service.file.FileService'                       ,null                  ,null                 ,35         , 'file.do'  , 'This service accepts requests from iPad')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   36             ,'changeset'     ,'changesetFileCacheChecker','ru.sberbank.syncserver2.service.file.diff.CacheDiffChecker'               ,null                  ,null                 ,35         , 'filecheck.do', 'This service compares content of cache in Sigma with database in Alpha')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   43             ,'common'        ,'pushNotificationService','ru.sberbank.syncserver2.service.pushnotifications.PushNotificationService'               ,null                  ,null                 ,43         , null, 'This service sends push notifications to out.')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   38             ,'common'        ,'xmlPublicService','ru.sberbank.syncserver2.service.pub.SyncserverPublicServices'               ,null                  ,null                 ,38         , 'request.do', 'This is public xml service')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   39             ,'single'        ,'misFileStatusCacheServer','ru.sberbank.syncserver2.service.file.cache.SingleFileStatusCacheService','misFileLoader'                  ,'fileStatusCacheService'                 ,39         , null, 'Single file statis cache server')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   40             ,'common'        ,'misEventHandler','ru.sberbank.syncserver2.service.core.event.impl.SigmaEventHandler'               ,null                  ,null                 ,40         , null, 'This is system event handler for sigma')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   41             ,'online'        ,'mdmGroupManager','ru.sberbank.syncserver2.service.ldap.LdapGroupManagementService'               ,null                  ,null                 ,41         , null, 'Mdm group manager')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   42             ,'common'        ,'pushNotificationUploader','ru.sberbank.syncserver2.service.pushnotifications.DataPowerPushnotificationsUploader'    ,null     ,null                 ,42         , null, 'This service load push notifications from Alpha source DB to push notification service')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   37             ,'common'        ,'AppleSender','ru.sberbank.syncserver2.service.pushnotifications.senders.ApplePushNotificationSender'    ,'pushNotificationService'     ,'sender'                 ,37         , null, 'This service used by pushNotificationService to send notifications')


go
set identity_insert SYNC_SERVICES off
go


--4. Filling properties
--4.1. Filling online services
--4.1.1. Filling datapower service
exec SP_SYNC_RESET_PROPERTIES 'online','misDataPowerService'
exec SP_SYNC_SET_PROPERTY     'online','misDataPowerService','dataPowerURL1'   ,'@DATAPOWER_URL1@'
exec SP_SYNC_SET_PROPERTY     'online','misDataPowerService','dataPowerURL2'   ,'@DATAPOWER_URL2@'
exec SP_SYNC_SET_PROPERTY     'online','misDataPowerService','overrideProvider','DISPATCHER'
exec SP_SYNC_SET_PROPERTY     'online','misDataPowerService','overrideService1','@ALPHA_SQLPROXY_HOST1@'
exec SP_SYNC_SET_PROPERTY     'online','misDataPowerService','overrideService2','@ALPHA_SQLPROXY_HOST2@'
exec SP_SYNC_SET_PROPERTY     'online','misDataPowerService','conversion'      ,'CONVERT_TO_NEW_SIGMA'

exec SP_SYNC_RESET_PROPERTIES 'online','misDataPowerSecurity'
exec SP_SYNC_SET_PROPERTY     'online','misDataPowerSecurity', 'dataPowerServiceBeanCode','misDataPowerService'
exec SP_SYNC_SET_PROPERTY     'online','misDataPowerSecurity', 'isAllowedToUseApp','SYNCSERVER.IS_ALLOWED_TO_USE_APP'
exec SP_SYNC_SET_PROPERTY     'online','misDataPowerSecurity', 'isAllowedToDownloadFile','SYNCSERVER.IS_ALLOWED_TO_DOWNLOAD_FILE'
exec SP_SYNC_SET_PROPERTY     'online','misDataPowerSecurity',  'provider'         ,'DISPATCHER'
exec SP_SYNC_SET_PROPERTY     'online','misDataPowerSecurity',  'service'          ,'@ALPHA_SOURCE_SERVICE@'

--4.1.2. Filling sqlite services
exec SP_SYNC_RESET_PROPERTIES 'online','misSQLiteNetworkMover'
exec SP_SYNC_SET_PROPERTY     'online','misSQLiteNetworkMover','networkSourceFolder','@NETWORK_ROOT_FOLDER@/sqlite/files'
exec SP_SYNC_SET_PROPERTY     'online','misSQLiteNetworkMover','localTempFolder'    ,'@ROOT_FOLDER@/@DB_NAME@/online/network/temp'
exec SP_SYNC_SET_PROPERTY     'online','misSQLiteNetworkMover','localDestFolder'    ,'@ROOT_FOLDER@/@DB_NAME@/online/local/inbox'
exec SP_SYNC_SET_PROPERTY     'online','misSQLiteNetworkMover','networkSharedFolder','@NETWORK_ROOT_FOLDER@/sqlite/shared'
exec SP_SYNC_SET_PROPERTY     'online','misSQLiteNetworkMover','staticSharedHosts'  ,'localhost'
exec SP_SYNC_SET_PROPERTY     'online','misSQLiteNetworkMover','debugModeWithSMSOnDelivery','false'
go

exec SP_SYNC_RESET_PROPERTIES 'online','misSQLiteService'
exec SP_SYNC_SET_PROPERTY     'online','misSQLiteService','localIncomingFile','@ROOT_FOLDER@/@DB_NAME@/online/local/inbox/online.sqlite'
exec SP_SYNC_SET_PROPERTY     'online','misSQLiteService','localWorkFolder'  ,'@ROOT_FOLDER@/@DB_NAME@/online/local/work'
go

--4.9 For datapower logger
exec SP_SYNC_RESET_PROPERTIES 'online','dpLogger'
exec SP_SYNC_SET_PROPERTY     'online','dpLogger','dataPowerBeanCode','misDataPowerService'
exec SP_SYNC_SET_PROPERTY     'online','dpLogger','logSQL'           ,'SYNCSERVER.LOG_SQL'
exec SP_SYNC_SET_PROPERTY     'online','dpLogger','provider'         ,'DISPATCHER'
exec SP_SYNC_SET_PROPERTY     'online','dpLogger','service'          ,'@ALPHA_GENERATOR_SERVICE@'

--4.9 For notification logger
exec SP_SYNC_RESET_PROPERTIES 'online','misNotificationLogger'
exec SP_SYNC_SET_PROPERTY     'online','misNotificationLogger','dataPowerBeanCode','misDataPowerService'
exec SP_SYNC_SET_PROPERTY     'online','misNotificationLogger','databaseName','@ALPHA_MONITOR_DB@'
exec SP_SYNC_SET_PROPERTY     'online','misNotificationLogger','databaseGeneratorName','@ALPHA_GENERATOR_DB@'
exec SP_SYNC_SET_PROPERTY     'online','misNotificationLogger','provider'    ,'DISPATCHER'
exec SP_SYNC_SET_PROPERTY     'online','misNotificationLogger','service'     ,'@ALPHA_MONITOR_SERVICE@'
go

exec SP_SYNC_RESET_PROPERTIES 'online','misLdapUserCheckerService'
exec SP_SYNC_SET_PROPERTY     'online','misLdapUserCheckerService','provider','@LDAP_PROVIDER@'
exec SP_SYNC_SET_PROPERTY     'online','misLdapUserCheckerService','domain','@LDAP_DOMAIN@'
exec SP_SYNC_SET_PROPERTY     'online','misLdapUserCheckerService','username','@LDAP_USERNAME@'
exec SP_SYNC_SET_PROPERTY     'online','misLdapUserCheckerService','password' ,'@LDAP_PASSWORD@'
go

--4.2. Filling single file services
--4.2.1 For misNetworkMover
exec SP_SYNC_RESET_PROPERTIES 'single','misNetworkMover'
exec SP_SYNC_SET_PROPERTY     'single','misNetworkMover','networkSourceFolder'  ,'@NETWORK_ROOT_FOLDER@/single/files'
exec SP_SYNC_SET_PROPERTY     'single','misNetworkMover','networkSharedFolder'  ,'@NETWORK_ROOT_FOLDER@/single/shared'
exec SP_SYNC_SET_PROPERTY     'single','misNetworkMover','localTempFolder'      ,'@ROOT_FOLDER@/@DB_NAME@/single/network/temp'
exec SP_SYNC_SET_PROPERTY     'single','misNetworkMover','localDestFolder'      ,'@ROOT_FOLDER@/@DB_NAME@/single/local/inflater/inbox'
exec SP_SYNC_SET_PROPERTY     'single','misNetworkMover','sharedHostsListerCode','misDbFileLister'
exec SP_SYNC_SET_PROPERTY     'single','misNetworkMover','debugModeWithSMSOnDelivery','false'
go

--4.2.2 For misLocalInflater
exec SP_SYNC_RESET_PROPERTIES 'single','misLocalInflater'
exec SP_SYNC_SET_PROPERTY     'single','misLocalInflater','localSourceFolder'  ,'@ROOT_FOLDER@/@DB_NAME@/single/local/inflater/inbox'
exec SP_SYNC_SET_PROPERTY     'single','misLocalInflater','localTempFolder1'   ,'@ROOT_FOLDER@/@DB_NAME@/single/local/inflater/temp1'
exec SP_SYNC_SET_PROPERTY     'single','misLocalInflater','localTempFolder2'   ,'@ROOT_FOLDER@/@DB_NAME@/single/local/inflater/temp2'
exec SP_SYNC_SET_PROPERTY     'single','misLocalInflater','localDestFolder'    ,'@ROOT_FOLDER@/@DB_NAME@/single/local/inflater/inflated'
go

--4.2.3. For misFileLoader
exec SP_SYNC_RESET_PROPERTIES 'single','misFileLoader'
exec SP_SYNC_SET_PROPERTY     'single','misFileLoader','chunkSize','1048576'
exec SP_SYNC_SET_PROPERTY     'single','misFileLoader','inboxFolder','@ROOT_FOLDER@/@DB_NAME@/single/local/inflater/inflated'
exec SP_SYNC_SET_PROPERTY     'single','misFileLoader','tempFolder','@ROOT_FOLDER@/@DB_NAME@/single/local/loader/temp'
exec SP_SYNC_SET_PROPERTY     'single','misFileLoader','archiveFolder','@ROOT_FOLDER@/@DB_NAME@/single/local/loader/archive'
exec SP_SYNC_SET_PROPERTY     'single','misFileLoader','cacheFolder','@ROOT_FOLDER@/@DB_NAME@/single/local/fileCache'
go
--4.2.4. For misFileCache
exec SP_SYNC_RESET_PROPERTIES 'single','misFileCache'
exec SP_SYNC_SET_PROPERTY     'single','misFileCache','debugModeWithoutLoadToMemory','false'
go

--4.3. Filling changeset services
--4.3.1 For misChangesetNetworkMover
exec SP_SYNC_RESET_PROPERTIES 'changeset','misChangesetNetworkMover'
exec SP_SYNC_SET_PROPERTY     'changeset','misChangesetNetworkMover','networkSourceFolder','@NETWORK_ROOT_FOLDER@/changeset/files'
exec SP_SYNC_SET_PROPERTY     'changeset','misChangesetNetworkMover','networkSharedFolder','@NETWORK_ROOT_FOLDER@/changeset/shared'
exec SP_SYNC_SET_PROPERTY     'changeset','misChangesetNetworkMover','localTempFolder'    ,'@ROOT_FOLDER@/@DB_NAME@/changeset/network/temp'
exec SP_SYNC_SET_PROPERTY     'changeset','misChangesetNetworkMover','localDestFolder'    ,'@ROOT_FOLDER@/@DB_NAME@/changeset/network/received'
exec SP_SYNC_SET_PROPERTY     'changeset','misChangesetNetworkMover','staticSharedHosts'  ,'@NETWORK_SHARED_HOSTS_FOR_CHANGESETS@'
exec SP_SYNC_SET_PROPERTY     'changeset','misChangesetNetworkMover','debugModeWithSMSOnDelivery','false'
go

--4.3.2. For misDynFileLister
exec SP_SYNC_RESET_PROPERTIES 'changeset','misDynFileLister'
exec SP_SYNC_SET_PROPERTY     'changeset','misDynFileLister','statusFile','@ROOT_FOLDER@/@DB_NAME@/changeset/local/loader/list.bin'
go

--4.3.3. For misDynFileLister
exec SP_SYNC_RESET_PROPERTIES 'changeset','misMbrLoader'
exec SP_SYNC_SET_PROPERTY     'changeset','misMbrLoader','chunkSize'    ,'4194304'
exec SP_SYNC_SET_PROPERTY     'changeset','misMbrLoader','inboxFolder'  ,'@ROOT_FOLDER@/@DB_NAME@/changeset/network/received'
exec SP_SYNC_SET_PROPERTY     'changeset','misMbrLoader','tempFolder'   ,'@ROOT_FOLDER@/@DB_NAME@/changeset/local/loader/temp'
exec SP_SYNC_SET_PROPERTY     'changeset','misMbrLoader','archiveFolder','@ROOT_FOLDER@/@DB_NAME@/changeset/local/loader/archive'
exec SP_SYNC_SET_PROPERTY     'changeset','misMbrLoader','cacheFolder'  ,'@ROOT_FOLDER@/@DB_NAME@/changeset/fileCache'
go

--4.3.4. For changesetFileCache
exec SP_SYNC_RESET_PROPERTIES 'changeset','changesetFileCache'
exec SP_SYNC_SET_PROPERTY     'changeset','changesetFileCache','debugModeWithoutLoadToMemory','false'
go


--4.3.5. For changesetFileCacheChecker
exec SP_SYNC_RESET_PROPERTIES 'changeset','changesetFileCacheChecker'
exec SP_SYNC_SET_PROPERTY     'changeset','changesetFileCacheChecker','fileCacheBean'    ,'changesetFileCache'
exec SP_SYNC_SET_PROPERTY     'changeset','changesetFileCacheChecker','dataPowerBeanCode','misDataPowerService'
go

exec SP_SYNC_RESET_PROPERTIES 'common','pushNotificationService'
exec SP_SYNC_SET_PROPERTY     'common','pushNotificationService','amount'   ,'0'
go

exec SP_SYNC_RESET_PROPERTIES 'common','AppleSender'
exec SP_SYNC_SET_PROPERTY     'common','AppleSender','certificatesFolder'   ,'@PUSH_CERTIFICATE_CONFIG_FOLDER@/IOS'
exec SP_SYNC_SET_PROPERTY     'common','AppleSender','maxPayloadSize'   ,'2048'
go

exec SP_SYNC_RESET_PROPERTIES 'common','pushNotificationUploader'
exec SP_SYNC_SET_PROPERTY     'common','pushNotificationUploader','connectionService'   ,'@PUSH_NOTIFICATIONS_SOURCE_SERVICE@' 
exec SP_SYNC_SET_PROPERTY     'common','pushNotificationUploader','appName'   ,'@PUSH_NOTIFICATIONS_APP_NAME@' 
go

exec SP_SYNC_RESET_PROPERTIES 'single','misFileStatusCacheServer'
exec SP_SYNC_SET_PROPERTY     'single','misFileStatusCacheServer','fileStatusRequestTemplateName'    ,'SYNCSERVER.SINGLE_FILES_REQUEST_STATUS'
exec SP_SYNC_SET_PROPERTY     'single','misFileStatusCacheServer','userGroupRequestTemplateName','SYNCSERVER.GENERATOR_USER_GROUP_DRAFT'
exec SP_SYNC_SET_PROPERTY     'single','misFileStatusCacheServer','fileCacheBeanCode','misFileCache'
exec SP_SYNC_SET_PROPERTY     'single','misFileStatusCacheServer','datapowerServiceBeanCode','misDataPowerService'
exec SP_SYNC_SET_PROPERTY     'single','misFileStatusCacheServer','generatorServiceName','@ALPHA_GENERATOR_SERVICE@'
go

exec SP_SYNC_RESET_PROPERTIES 'common','misEventHandler'
exec SP_SYNC_SET_PROPERTY     'common','misEventHandler','sendPushNotificationWhenFileLoaded'    ,'@PUSH_NOTIFICATION_WHEN_NEW_FILE_LOADED@'
go

exec SP_SYNC_RESET_PROPERTIES 'online','mdmGroupManager'
exec SP_SYNC_SET_PROPERTY     'online','mdmGroupManager','provider'    ,'@LDAP_PROVIDER@'
exec SP_SYNC_SET_PROPERTY     'online','mdmGroupManager','domain'    ,'@LDAP_DOMAIN@'
exec SP_SYNC_SET_PROPERTY     'online','mdmGroupManager','settings'    ,'@LDAP_USER_GROUP_MANAGER_SETTINGS@'
go

exec SP_SYNC_RESET_PROPERTIES 'online','misMaintenanceService'
exec SP_SYNC_SET_PROPERTY     'online','misMaintenanceService','hosts'    ,'@MAINTENANCE_HOSTS@'
go

exec SP_SYNC_RESET_PROPERTIES 'online','requestEmailVerifier'
exec SP_SYNC_SET_PROPERTY     'online','requestEmailVerifier','skipEmailVerification'    ,'@SKIP_EMAIL_VERIFICATION@'
exec SP_SYNC_SET_PROPERTY     'online','requestEmailVerifier','skipEmailVerificationIps'    ,'@SKIP_EMAIL_VERIFICATION_IP_LIST@'
go


