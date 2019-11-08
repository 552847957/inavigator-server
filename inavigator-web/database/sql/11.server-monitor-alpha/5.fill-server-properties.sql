
--1. Deleting old, it simplifies restart
DELETE FROM SYNC_SERVICES
DELETE FROM SYNC_FOLDERS
go

--2. Filling list of folders
set identity_insert SYNC_FOLDERS on
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER, SYNC_FOLDER_DESC) values(1,'monitor'   , 1, 'All Monitor Services are running in this service folder')
set identity_insert SYNC_FOLDERS off

exec SP_SYNC_ADD_TEMPLATE 'ru.sberbank.syncserver2.service.monitor.MonitorService' 
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.SpaceAndLastModifiedCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'includeFolders' ,'N','List of all monitored services, it should include UNC-address of file transporter'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'includeMaxMB'   ,'N','If space used by files at file transporter in includeFolders become bigger than this value then notification will be sent'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'includeMaxHours','N','If any file in includeFolders at file transporter becomes older by more hours than this value then notification will be sent'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'excludeFolders' ,'N','These folders will be ignored during the space and date check'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'tempFolders'    ,'N','Old files in temp folders are deleted automatically'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'tempMaxHours'   ,'N','Any files in tempFolders older than this value will be deleted'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.EmailSender'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'mailHost'    ,'N','SMTP Host Name'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'mailPort'    ,'N','SMTP Port'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'mailFrom'    ,'N','SMTP Default From'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'mailUser'    ,'N','SMTP User'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'mailPassword','N','SMTP Password'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.SmsSender'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'smsProxyUrl','N','Address of DpSMSProxy'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.NotificationService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'alertTransports','N','Alert transports - SMS or SMTP or SMS;SMTP'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'alertAddresses' ,'N','List of emails to send notificatins by email'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'alertPhones'    ,'N','List of phones like 79161111111 to send notifications by SMS'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'databaseName','N','Alpha Monitor database name'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.GeneratorAutoGenCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'waitingIntervalMinuties','N','Waiting interval for error detected'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'urls','N','Generator urls for monitoring'

go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.WritePermissionCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'includeFolders' ,'N','List of all monitored services'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.AlphaDatapowerAvailabilityCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'sigmaHosts','N','List of monitor-sigma hosts'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'timeInterval','N','Maximum waiting ping time in seconds'
go

--3. Filing list of services
set identity_insert SYNC_SERVICES on
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE         ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   1              ,'monitor'     , 'monitor'          ,'ru.sberbank.syncserver2.service.monitor.MonitorService'                   ,null            ,null                ,1          , 'This service starts spaceAndTimeCheck and use databaseLogger to save notification to the database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE         ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   2              ,'monitor'     , 'notifier'          ,'ru.sberbank.syncserver2.service.monitor.NotificationService'             ,null            ,null                ,2          ,'This service reads notifications from database and distributes them. Notifications could be generated by other servers or this server')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   3              ,'monitor'     , 'spaceAndTimeCheck','ru.sberbank.syncserver2.service.monitor.check.SpaceAndLastModifiedCheck'  ,'monitor'       ,'checkAction'       ,3          ,'This service used by "monitor" to check disk space and too old files at file transporter')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   4              ,'monitor'     , 'emailSender'      ,'ru.sberbank.syncserver2.service.monitor.EmailSender'                      ,null            ,null                ,4          ,'This service used by "notifier" to sent notifications by email')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   5              ,'monitor'     , 'smsSender'        ,'ru.sberbank.syncserver2.service.monitor.SmsSender'                        ,null            ,null                ,5          ,'This service used by "notifier" to sent notifications by SMS')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   6              ,'monitor'     , 'databaseLogger'   ,'ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger'       ,'monitor'       ,'databaseLogger'    ,6          ,'This service used by "monitor" to save notifications to the database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   7              ,'monitor'     , 'permissionCheck'  ,'ru.sberbank.syncserver2.service.monitor.check.WritePermissionCheck'  	 ,'monitor'       ,'checkAction'       ,7          ,'This service used by "monitor" to check write permission')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   8              ,'monitor'     , 'autogenCheck'  	,'ru.sberbank.syncserver2.service.monitor.check.GeneratorAutoGenCheck'  	 ,'monitor'       ,'checkAction'       ,8          ,'This service used by "monitor" to check write permission')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   9              ,'monitor' , 'datapowerAvailabilityCheck' ,'ru.sberbank.syncserver2.service.monitor.check.AlphaDatapowerAvailabilityCheck','monitor','checkAction'    ,9          ,'This service used by "monitor" to check DataPower avilability')
go
set identity_insert SYNC_SERVICES off
go

--4. Filling properties
--4.1 For MonitorServuice
exec SP_SYNC_RESET_PROPERTIES 'monitor','monitor'

--4.2. For notification service
exec SP_SYNC_RESET_PROPERTIES 'monitor','notifier'
exec SP_SYNC_SET_PROPERTY     'monitor','notifier'     ,'alertTransports','@ALERT_TRANSPORTS@'
exec SP_SYNC_SET_PROPERTY     'monitor','notifier'     ,'alertAddresses' ,'@ALERT_ADDRESSES@'
exec SP_SYNC_SET_PROPERTY     'monitor','notifier'     ,'alertPhones'    ,'@ALERT_PHONES@'

exec SP_SYNC_RESET_PROPERTIES 'monitor','spaceAndTimeCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','includeFolders' ,'@FP_ROOT@'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','includeMaxMB'   ,'8192'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','includeMaxHours','24'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','excludeFolders' ,''
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','tempFolders'    ,'@FP_ROOT@\\OUT\\temp;@FP_ROOT@\\IN\\temp'
--exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','excludeFolders' ,'@FP_ROOT@\\IN\\prod\\mbr\\mbr_december_for_mis;@FP_ROOT@\\OUT\\temp;@FP_ROOT@\\IN\\temp'
--exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','tempFolders'    ,'@FP_ROOT@\\OUT\\temp;@FP_ROOT@\\IN\\temp'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','tempMaxHours'   ,'24'

exec SP_SYNC_RESET_PROPERTIES 'monitor','emailSender'
exec SP_SYNC_SET_PROPERTY 'monitor','emailSender'    ,'mailHost'         ,'@SMTP_HOST@'
exec SP_SYNC_SET_PROPERTY 'monitor','emailSender'    ,'mailPort'         ,'@SMTP_PORT@'
exec SP_SYNC_SET_PROPERTY 'monitor','emailSender'    ,'mailFrom'         ,'@SMTP_FROM@'
exec SP_SYNC_SET_PROPERTY 'monitor','emailSender'    ,'mailUser'         ,'@SMTP_USER@'
exec SP_SYNC_SET_PROPERTY 'monitor','emailSender'    ,'mailPassword'     ,'@SMTP_PASSWORD@'

exec SP_SYNC_RESET_PROPERTIES 'monitor','smsSender'
exec SP_SYNC_SET_PROPERTY     'monitor','smsSender'  ,'smsProxyUrl'      ,'@SMS_PROXY_SERVER@'

exec SP_SYNC_RESET_PROPERTIES 'monitor','permissionCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','permissionCheck','includeFolders' ,'@FP_ROOT@\\IN'

exec SP_SYNC_RESET_PROPERTIES 'monitor','databaseLogger'
exec SP_SYNC_SET_PROPERTY     'monitor','databaseLogger','databaseName','@ALPHA_MONITOR_DB@'

exec SP_SYNC_RESET_PROPERTIES 'monitor','autogenCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','autogenCheck','waitingIntervalMinuties','@WAITING_AUTOGEN_INTERVAL@'
exec SP_SYNC_SET_PROPERTY     'monitor','autogenCheck','urls','@GENERATOR_MONITORING_URLS@'

exec SP_SYNC_RESET_PROPERTIES 'monitor','datapowerAvailabilityCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerAvailabilityCheck','sigmaHosts','@SIGMA_MONITOR_HOSTS@'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerAvailabilityCheck','timeInterval','180'

go
