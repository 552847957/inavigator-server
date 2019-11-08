--1. Deleting old, it simplifies restart
DELETE FROM SYNC_SERVICES
DELETE FROM SYNC_FOLDERS
go


--2. Fill All services class structure (see example below)

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.transport.SharedSigmaNetworkFileMover'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'networkSourceFolder','N','The folder at file transporter with files copied from Sigma'

--3. Filling list of service folders

set identity_insert SYNC_FOLDERS on

insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(1,'online'   , 1,'This folder contains services for online SQL requests and datapower services')

set identity_insert SYNC_FOLDERS off
go

--4. Filing list of services

set identity_insert SYNC_SERVICES on

insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE    ,PARENT_BEAN_PROPERTY, START_ORDER,SERVLET_PATH    , BEAN_DESC)
values(                   1              ,'online'        ,'misDataPowerService' ,'ru.sberbank.syncserver2.service.sql.DataPowerService'                          ,'misSQLService'     ,'dataPowerService'  , 1          , null            ,'This service sends requests to DataPower')

set identity_insert SYNC_SERVICES off
go

--5. Filling service properties
exec SP_SYNC_RESET_PROPERTIES 'online','misDataPowerService'
exec SP_SYNC_SET_PROPERTY     'online','misDataPowerService','dataPowerURL1'   ,'@DATAPOWER_URL1@'
exec SP_SYNC_SET_PROPERTY     'online','misDataPowerService','dataPowerURL2'   ,'123321'
