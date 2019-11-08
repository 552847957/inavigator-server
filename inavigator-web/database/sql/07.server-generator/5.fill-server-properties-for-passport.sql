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
set identity_insert SYNC_FOLDERS off

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.transport.LocalDeflater'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localSourceFolder','N','After file appears in localSourceFolder the service moves it to localTempFolder1. '
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder1' ,'N','After file appears in localTempFolder1 the service starts deflating file to a file with same name in localTempFolder2'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder2' ,'N','localTempFolder2 used to store deflated files while the deflation. After deflation finished the file is copied to localDestFolder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localDestFolder'  ,'N','The service moves to localDestFolder completed inflated file'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.generator.single.SingleGeneratorService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'threadCount'         ,'N', 'Maximum number of files generated at same time'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'configHome'          ,'N', 'Path to folder with ETL files'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localFileHome'       ,'N', 'This folder contains subfolders with temporary and generated files'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localArchiveFolder'  ,'N', 'The link to network mover''s localArchiveFolder. Used to  repeat file copy.'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'networkTempFolder'   ,'N', 'The link to network mover''s networkTempFolder. Used to repeat file copy.'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'networkTargetFolder' ,'N', 'The link to network mover''s networkTargetFolder. Used to repeat file copy.'
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
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'networkTargetFolder'  ,'N','Services renames files from networkTempFolder to networkTargetFolder and then file transporter soft moves it to Sigma'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localArchiveFolder'   ,'N','After file is ready for move to file transporter we keep the copy for repetition'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'staticSharedHosts'    ,'N','Static list of hosts the file should be delivered to'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'sharedHostsListerCode','N','The link to dynamic lister that provides list of hosts the file should be delivered to'
go

--3. Filing list of services
set identity_insert SYNC_SERVICES on
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE               ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   1              ,'common'        , 'databaseLogger'       ,'ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger'            ,null            ,null                ,1          , 'This service logs notifications to Alpha Monitor database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE               ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   2              ,'single'        , 'singleGenerator'      ,'ru.sberbank.syncserver2.service.generator.single.SingleGeneratorService'       ,null            ,null                ,2          , 'This service generates single files')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE, BEAN_CODE              ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   3              ,'single'        , 'singleLocalDeflater'  ,'ru.sberbank.syncserver2.service.file.transport.LocalDeflater'                  ,null            ,null                ,3          , 'This service deflates generated files')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE, BEAN_CODE              ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   4              ,'single'        , 'singleFileLister'     ,'ru.sberbank.syncserver2.service.file.cache.list.DatabaseFileLister'            ,null            ,null                ,4          , 'This service used to prepare notifications for not delivered files. It loads list of hosts the files should be delivered to')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE, BEAN_CODE              ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   5              ,'single'        , 'singleNetworkMover'   ,'ru.sberbank.syncserver2.service.file.transport.AlphaNetworkFileMover'          ,null            ,null                ,5          , 'This service moves deflated files to file transporter')
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
go

--4.1.2. For local deflater
exec SP_SYNC_RESET_PROPERTIES 'single','singleLocalDeflater'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','localSourceFolder'  ,'@ROOT_FOLDER@/generator/single/generator/generated'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','localTempFolder1'   ,'@ROOT_FOLDER@/generator/single/deflater/temp1'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','localTempFolder2'   ,'@ROOT_FOLDER@/generator/single/deflater/temp2'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','localDestFolder'    ,'@ROOT_FOLDER@/generator/single/deflater/outbox'
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



