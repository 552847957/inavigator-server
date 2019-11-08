
--1. Deleting old, it simplifies restart
DELETE FROM SYNC_SERVICES
DELETE FROM SYNC_FOLDERS
go

--2. Filling list of folders
set identity_insert SYNC_FOLDERS on
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,SYNC_FOLDER_DESC,START_ORDER) values(1,'config','config folder url'   , 1)
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(2,'public', 2,'This folder for new public services')
set identity_insert SYNC_FOLDERS off

--3. Filing list of services
set identity_insert SYNC_SERVICES on
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                               ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER,SERVLET_PATH)
values(                   1             ,'config'            ,'configService'     ,'ru.sberbank.syncserver2.service.config.ConfigService'   ,null            ,null       ,1                   , 'config.do')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   2             ,'public'     ,'xmlPublicService','ru.sberbank.syncserver2.service.pub.ConfserverPublicServices'               ,null                  ,null                 ,2         , 'request.do', 'This is public xml service')

go
set identity_insert SYNC_SERVICES off
go

