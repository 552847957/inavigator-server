
set identity_insert SYNC_FOLDERS on
declare @id int 
select @id = max(SYNC_FOLDER_ID) from SYNC_FOLDERS
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(@id+1 ,'adminconsole', @id+1,'This folder contains services for adminconsole application')
set identity_insert SYNC_FOLDERS off
go

set identity_insert SYNC_SERVICES on
declare @id int 
select @id = max(SYNC_SERVICE_ID) from SYNC_SERVICES
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE,BEAN_CLASS,PARENT_BEAN_CODE ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(   @id+1   ,'adminconsole' ,'adminConsoleService','ru.sberbank.syncserver2.service.pub.AdminConsoleService' ,null    ,null        , @id+1   , 'request.do', 'This is xml service for adminconsole')
set identity_insert SYNC_SERVICES off
go

