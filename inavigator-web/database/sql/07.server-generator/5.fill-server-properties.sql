---0. Addomg additional role to employee
INSERT INTO [EMPLOYEE_ROLES] VALUES(2,'Operator')
INSERT INTO [EMPLOYEES] ([EMPLOYEE_ROLE_ID],[EMPLOYEE_EMAIL], [EMPLOYEE_NAME], [leave_date], [EMPLOYEE_PASSWORD],[PASSWORD_CHANGED_DATE]) VALUES (2,'operator', 'operator', null, 'E10ADC3949BA59ABBE56E057F20F883E',getdate())
GO

--1. Deleting old, it simplifies restart
DELETE FROM SYNC_SERVICES
DELETE FROM SYNC_FOLDERS
go

--2. Filling list of folders
set identity_insert SYNC_FOLDERS on
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(1,'common'   , 1,'This folder contains common services used by other folders')
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(2,'single'   , 2,'This folder contains services required to generate single files')
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(3,'changeset', 3,'This folder contains services required to generate changeset for generator')
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(4,'public'   , 4,'This folder contains services for public access form other applications')
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(5,'qlikview' , 5,'This folder contains services for QlikView')
set identity_insert SYNC_FOLDERS off

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.transport.LocalDeflater'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localSourceFolder','N','After file appears in localSourceFolder the service moves it to localTempFolder1. '
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder1' ,'N','After file appears in localTempFolder1 the service starts deflating file to a file with same name in localTempFolder2'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder2' ,'N','localTempFolder2 used to store deflated files while the deflation. After deflation finished the file is copied to localDestFolder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localDestFolder'  ,'N','The service moves to localDestFolder completed inflated file'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'maxFragmentFileSizeMb'  ,'N','Max fragment file size from offline files.'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.generator.single.SingleGeneratorService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'threadCount'         ,'N', 'Maximum number of files generated at same time'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'configHome'          ,'N', 'Path to folder with ETL files'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localFileHome'       ,'N', 'This folder contains subfolders with temporary and generated files'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localArchiveFolder'  ,'N', 'The link to network mover''s localArchiveFolder. Used to  repeat file copy.'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'networkTempFolder'   ,'N', 'The link to network mover''s networkTempFolder. Used to repeat file copy.'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'networkTargetFolder'  ,'N', 'The link to network mover''s networkTargetFolder. Used to repeat file copy.'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'logsStorageTimes'  ,'N', 'The maximum number of log generation to store.'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.generator.rubricator.ChangeSetGeneratorService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localFolder'         ,'N','The local folder used for temporary storage and storage of generated files'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dbDriverClassname'   ,'N','JDBC Driver name for MIS_RUBRICATOR'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dbUrl'               ,'N','JDBC URL for MIS_RUBRICATOR'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dbUser'              ,'N','JDBC User'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dbPassword'          ,'N','JDBC Password'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'loadUpdatesSQL'      ,'N','loadUpdatesSQL used to request one last change from a change log in MIS_RUBRICATOR'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'databaseName','N','Alpha Monitor database name'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.file.cache.list.DatabaseFileLister'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.file.transport.AlphaNetworkFileMover'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localSourceFolder'    ,'N','The source folder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localTempFolder'      ,'N','When file appears in source folder it is copied to localTempFolder and only then it is copied to a file transporter'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'networkTempFolder'    ,'N','Service moves files to the folder at file transporter defined by networkTempFolder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'networkTargetFolder'  ,'N','Service renames files to networkTargetFolder from networkTempFolder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localArchiveFolder'   ,'N','After file is ready for move to file transporter we keep the copy for repetition'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'staticSharedHosts'    ,'N','Static list of hosts the file should be delivered to'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'sharedHostsListerCode','N','The link to dynamic lister that provides list of hosts the file should be delivered to'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'duplicateFolder'      ,'N','If exists, it copy files in this folder when move one to file transporter main cluster node.'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.core.event.impl.AlphaEventHandler'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.qlikview.QlikViewRequestService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'cronExpression'       ,'N','Cron expression start jobs for QlikView integration'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'qlikviewDocumentUser'       ,'N', 'User QlikView'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'qlikviewDocumentPassword'       ,'N', 'Password QlikView'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'qlikviewDriver'       ,'N', 'JDBC Driver class'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'qlikviewUrl'       ,'N', 'JDBC URL to data source with QlickView object id'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'qlikviewUser'       ,'N', 'User for qlikviewUrl'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'qlikviewPassword'       ,'N', 'Password for qlikviewUrl'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'stageDriver'       ,'N', 'JDBC Driver class'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'stageUrl'       ,'N', 'JDBC URL to data source with stage table'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'stageUser'       ,'N', 'User for stageUrl'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'stagePassword'       ,'N', 'Password for stageUrl'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'clustered'       ,'N', 'Is working in cluster?'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'reentrant'       ,'N', 'Is working in reentrant mode?'
go

--3. Filing list of services
set identity_insert SYNC_SERVICES on
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE               ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   1              ,'common'        , 'databaseLogger'       ,'ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger'            ,null            ,null                ,1          , 'This service logs notifications to Alpha Monitor database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE               ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   2              ,'common'        , 'clusterManager'       ,'ru.sberbank.syncserver2.service.generator.ClusterManager'            ,null            ,null                ,2          , 'This service helps other services to understand whether this node of cluster is active node or passive node')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE               ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   3              ,'single'        , 'singleGenerator'      ,'ru.sberbank.syncserver2.service.generator.single.SingleGeneratorService'       ,null            ,null                ,3          , 'This service generates single files')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE, BEAN_CODE              ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   4              ,'single'        , 'singleLocalDeflater'  ,'ru.sberbank.syncserver2.service.file.transport.LocalDeflater'                  ,null            ,null                ,4          , 'This service deflates generated files')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE, BEAN_CODE              ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   5              ,'single'        , 'singleFileLister'     ,'ru.sberbank.syncserver2.service.file.cache.list.DatabaseFileLister'            ,null            ,null                ,5          , 'This service used to prepare notifications for not delivered files. It loads list of hosts the files should be delivered to')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE, BEAN_CODE              ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   6              ,'single'        , 'singleNetworkMover'   ,'ru.sberbank.syncserver2.service.file.transport.AlphaNetworkFileMover'          ,null            ,null                ,6          , 'This service moves deflated files to file transporter')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE, BEAN_CODE              ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   7              ,'changeset'     , 'changesetGenerator'   ,'ru.sberbank.syncserver2.service.generator.rubricator.ChangeSetGeneratorService',null            ,null                ,7          , 'This service generates changesets (zip archives) for rubricator files')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE, BEAN_CODE              ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   8              ,'changeset'     , 'changesetNetworkMover','ru.sberbank.syncserver2.service.file.transport.AlphaNetworkFileMover'          ,null            ,null                ,8          , 'This service moves deflated files to file transporter')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   9             ,'common'        ,'misEventHandler','ru.sberbank.syncserver2.service.core.event.impl.AlphaEventHandler'               ,null                  ,null                 ,9         , null, 'This is system event handler for alpha')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   10             ,'public'        ,'genStatusRequester','ru.sberbank.syncserver2.service.generator.single.pub.GeneratorAutogenStatusPublicService'               ,null                  ,null                 ,10         , 'request.do', 'This is system event handler for alpha')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   11             ,'qlikview'        ,'qlikViewRequestService','ru.sberbank.syncserver2.service.qlikview.QlikViewRequestService'               ,null                  ,null                 ,11         , null, 'Integration with QlikVierw')
go
set identity_insert SYNC_SERVICES off
go


--4. Filling properties
--4.0.0. For database notification logger
exec SP_SYNC_RESET_PROPERTIES 'common','databaseLogger'
exec SP_SYNC_SET_PROPERTY     'common','databaseLogger','databaseName','@ALPHA_MONITOR_DB@'

--4.1.1 For singleGenerator
exec SP_SYNC_RESET_PROPERTIES 'single','singleGenerator'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'threadCount' ,'8'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'configHome'         ,'@ROOT_FOLDER@/generator/config'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'localFileHome'      ,'@ROOT_FOLDER@/generator/single/generator'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'localArchiveFolder' ,'@ROOT_FOLDER@/generator/single/network/archive'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'networkTempFolder'  ,'@NETWORK_TEMP_FOLDER@/single/files'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'networkTargetFolder','@NETWORK_ROOT_FOLDER@/single/files'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'logsStorageTimes'   ,'5'
go

--4.1.2. For local deflater
exec SP_SYNC_RESET_PROPERTIES 'single','singleLocalDeflater'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','localSourceFolder'  ,'@ROOT_FOLDER@/generator/single/generator/generated'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','localTempFolder1'   ,'@ROOT_FOLDER@/generator/single/deflater/temp1'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','localTempFolder2'   ,'@ROOT_FOLDER@/generator/single/deflater/temp2'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','localDestFolder'    ,'@ROOT_FOLDER@/generator/single/deflater/outbox'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','maxFragmentFileSizeMb'    ,'@FILE_FRAGMENT_SIZE_MB@'
go

--4.1.3. For alpha network mover
exec SP_SYNC_RESET_PROPERTIES 'single','singleFileLister'
go
exec SP_SYNC_RESET_PROPERTIES 'single','singleNetworkMover'
exec SP_SYNC_SET_PROPERTY 'single','singleNetworkMover','localSourceFolder'    , '@ROOT_FOLDER@/generator/single/deflater/outbox'
exec SP_SYNC_SET_PROPERTY 'single','singleNetworkMover','localTempFolder'      , '@ROOT_FOLDER@/generator/single/network/temp'
exec SP_SYNC_SET_PROPERTY 'single','singleNetworkMover','networkTempFolder'    , '@NETWORK_TEMP_FOLDER@/single/files'
exec SP_SYNC_SET_PROPERTY 'single','singleNetworkMover','networkTargetFolder'  , '@NETWORK_ROOT_FOLDER@/single/files'
exec SP_SYNC_SET_PROPERTY 'single','singleNetworkMover','localArchiveFolder'   , '@ROOT_FOLDER@/generator/single/network/archive'
exec SP_SYNC_SET_PROPERTY 'single','singleNetworkMover','sharedHostsListerCode', 'singleFileLister'
go

--4.2. For rubricator
exec SP_SYNC_RESET_PROPERTIES 'changeset','changesetGenerator'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetGenerator','localFolder'      , '@ROOT_FOLDER@/generator/changeset/local'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetGenerator','dbDriverClassname', 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetGenerator','dbUrl'            , '@MSSQL_URL2@'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetGenerator','dbUser'           , '@MSSQL_USER2@'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetGenerator','dbPassword'       , '@MSSQL_PASSWORD2@'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetGenerator','loadUpdatesSQL'   , 'exec getChangeAfterId ?'

exec SP_SYNC_RESET_PROPERTIES 'changeset','changesetNetworkMover'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetNetworkMover','localSourceFolder'  , '@ROOT_FOLDER@/generator/changeset/local/generated'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetNetworkMover','localTempFolder'    , '@ROOT_FOLDER@/generator/changeset/network/temp'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetNetworkMover','networkTempFolder'  , '@NETWORK_TEMP_FOLDER@/changeset/files'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetNetworkMover','networkTargetFolder', '@NETWORK_ROOT_FOLDER@/changeset/files'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetNetworkMover','staticSharedHosts'  , '@NETWORK_SHARED_HOSTS_FOR_CHANGESETS@'
--exec SP_SYNC_SET_PROPERTY 'rubricator','changesetNetworkMover','localArchiveFolder' , '@ROOT_FOLDER@/generator/changeset/changesetGenerator/network/archive'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetNetworkMover','duplicateFolder'  , '@ALPHA_FILE_MOVER_DUPLICATE_FOLDER@'
go


