BEGIN TRANSACTION;
delete from SYNC_SERVICES where SYNC_FOLDER_CODE = 'qlikview'
go
DELETE FROM SYNC_FOLDERS
WHERE SYNC_FOLDER_CODE = 'qlikview'
go
set identity_insert SYNC_FOLDERS on
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(6,'qlikview', 6,'This folder contains services for QlikView')
set identity_insert SYNC_FOLDERS off
go
-- delete all values by list_id
delete from property_values where list_id in (select list_id from property_lists where list_code like 'qlikview%')
-- delete all properties for [folder]
delete from property_templates where list_template_id in (select list_template_id from property_lists where list_code like 'qlikview%')
-- delete records from cross table between list of properties and list of services
delete from property_lists where list_code like 'qlikview%'
-- delete all services templates
delete from property_list_templates where list_template_code like '%qlikview%'
COMMIT;
go

BEGIN TRANSACTION;
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

declare @NEXT_SYNC_SERVICE_ID int
select @NEXT_SYNC_SERVICE_ID = max(SYNC_SERVICE_ID) + 1 from SYNC_SERVICES

set identity_insert SYNC_SERVICES on
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   @NEXT_SYNC_SERVICE_ID             ,'qlikview'        ,'qlikViewRequestService','ru.sberbank.syncserver2.service.qlikview.QlikViewRequestService'               ,null                  ,null                 ,@NEXT_SYNC_SERVICE_ID         , null, 'Integration with QlikVierw')
go
set identity_insert SYNC_SERVICES off
go

exec SP_SYNC_RESET_PROPERTIES 'qlikview','qlikViewRequestService'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','cronExpression'  , '0 0/5 * * * ?'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','qlikviewDocumentUser'  , 'tech_navigator'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','qlikviewDocumentPassword'  , 'k7ZP4dcEC@'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','qlikviewDriver'  , 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','qlikviewUrl'  , '@MSSQL_URL2@'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','qlikviewUser'  , '@MSSQL_USER2@'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','qlikviewPassword'  , '@MSSQL_PASSWORD2@'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','stageDriver'  , 'com.microsoft.sqlserver.jdbc.SQLServerDriver'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','stageUrl'  , 'jdbc:sqlserver://10.116.205.60\INTEGRATION;databaseName=mis_navigator_data_imp'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','stageUser'  , 'qw_imp_user'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','stagePassword'  , 'qw_imp_user'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','clustered'  , 'true'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','reentrant'  , 'false'
go

/*
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','cronExpression'  , '0/15 * * * * ?'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','qlikviewUrl'  , 'jdbc:sqlserver://v-icheck-8r2-BD:1433;databaseName=generator'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','qlikviewUser'  , 'generator'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','qlikviewPassword'  , 'Qwerty123456'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','stageUrl'  , 'jdbc:sqlserver://v-icheck-8r2-BD:1433;databaseName=generator'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','stageUser'  , 'generator'
exec SP_SYNC_SET_PROPERTY 'qlikview','qlikViewRequestService','stagePassword'  , 'Qwerty123456'
go
*/

COMMIT;
GO

GO
SET NOCOUNT ON
INSERT INTO SYNC_CONFIG VALUES('ALPHA_FILE_MOVER_DUPLICATE_FOLDER'  ,''	,'Настройки ALPHA' ,'Если указано, то файлы дополнительно копируются в эту папку при переносе на файл перекладчик главной нодой кластера.')
GO

exec SP_SYNC_RESET_PROPERTIES 'changeset','changesetNetworkMover'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetNetworkMover','localSourceFolder'  , '@ROOT_FOLDER@/generator/changeset/local/generated'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetNetworkMover','localTempFolder'    , '@ROOT_FOLDER@/generator/changeset/network/temp'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetNetworkMover','networkTempFolder'  , '@NETWORK_TEMP_FOLDER@/changeset/files'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetNetworkMover','networkTargetFolder', '@NETWORK_ROOT_FOLDER@/changeset/files'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetNetworkMover','staticSharedHosts'  , '@NETWORK_SHARED_HOSTS_FOR_CHANGESETS@'
--exec SP_SYNC_SET_PROPERTY 'rubricator','changesetNetworkMover','localArchiveFolder' , '@ROOT_FOLDER@/generator/changeset/changesetGenerator/network/archive'
exec SP_SYNC_SET_PROPERTY 'changeset','changesetNetworkMover','duplicateFolder'  , '@ALPHA_FILE_MOVER_DUPLICATE_FOLDER@'
go
