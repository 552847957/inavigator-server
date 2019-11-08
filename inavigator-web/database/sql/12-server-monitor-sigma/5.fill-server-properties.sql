--1. Deleting old, it simplifies restart
DELETE FROM SYNC_SERVICES
DELETE FROM SYNC_FOLDERS
go

--2. Filling list of folders
set identity_insert SYNC_FOLDERS on
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(1,'monitor'   , 1, 'All Monitor Services are running in this service folder')
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
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.sql.DataPowerService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'dataPowerURL1'   ,'N','URL of DataPower #1'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'dataPowerURL2'   ,'N','URL of DataPower #1'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'overrideProvider','N','Always should be equal DISPATCHER. Other values are deprecated'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'overrideService1','N','The name of host in Alpha the request is sent to'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'overrideService2','N','The name of host in Alpha the request is sent to'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'conversion'     ,'N','This parameter used to provide compatibility of DataPower'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dataPowerBeanCode','N','Link to dataPowerService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'databaseName'     ,'N','The name of database used by Alpha Monitor'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id ,'provider'         ,'N','SQLService provider, should be DISPATCHER'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id ,'service'          ,'N','SQL Service at SQL Proxy Server for writing to generator database'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.ldap.LdapGroupManagementService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'provider'    ,'N','provider for ldap connection'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'domain'    ,'N','domain for ldap connection'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'settings'    ,'N','other settings'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.monitor.check.ServerTLSCertificateCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'monitoredHosts'    	   ,'N','Список хостов для проверки'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'SSLProtocol'    ,'N','Протокол взаимодействия. По умолчанию - TLSv1.2'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'timeInterval'    ,'N','Время между успешными проверками, в сек (по умолчанию 86400, т.е. 24 часа)'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'notifyWithinDays'    ,'N','Количество дней до истечения сертификата для уведомлений'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.monitor.check.ServerPushCertificateCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'folders'    	   ,'N','Список корневых папок для проверки'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'notifyWithinDays'    ,'N','Количество дней до истечения сертификата для уведомлений'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.WritePermissionCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'includeFolders' ,'N','List of all monitored services'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.PushNotificationChecker'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'connectionService' ,'N','SQL Service at SQL Proxy Server for getting push notifications for monitor'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.OnlineExecutionTimeCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'hosts' ,'N','Hosts for online requests check'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'settingsFile' ,'N','Setting file path with queries'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'certificateFile' ,'N','Certificate file path'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'passwordFile' ,'N','Password file path for certificate'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'SSLProtocol' ,'N','Secure socket protocol'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'replaceParameters' ,'N','Параметры в запросах для замены значениями из replaceValues (через ;)'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'replaceValues' ,'N','Значения для замены параметров в запросе (через ;)'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.OnlineRequestErrorCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'hosts' ,'N','Hosts for online requests check'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'settingsFolder' ,'N','Setting folder path for groups of queries'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'certificateFile' ,'N','Certificate file path'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'passwordFile' ,'N','Password file path for certificate'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'SSLProtocol' ,'N','Secure socket protocol'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'timeInterval' ,'N','Time interval between successful check in seconds'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'replaceParameters' ,'N','Параметры в запросах для замены значениями из replaceValues (через ;)'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'replaceValues' ,'N','Значения для замены параметров в запросе (через ;)'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.OnlineInfrastructureCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'hosts' ,'N','Hosts for online requests check'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'certificateFile' ,'N','Certificate file path'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'passwordFile' ,'N','Password file path for certificate'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'alphaSourceServices' ,'N','Password file path for certificate'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'SSLProtocol' ,'N','Secure socket protocol'
go

--3. Filing list of services
set identity_insert SYNC_SERVICES on
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   1              ,'monitor'     , 'spaceAndTimeCheck','ru.sberbank.syncserver2.service.monitor.check.SpaceAndLastModifiedCheck'  ,'monitor'       ,'checkAction'       ,1          ,'This service used by "monitor" to check disk space and too old files at file transporter')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   2              ,'monitor'     , 'datapowerLogger'   ,'ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger'     ,'monitorPingService'       ,'datapowerLogger'   ,2          ,'This server used by monitor to send notifications and pings to Alpha. It sends requests to SQL Proxy Server and it executes in Alpha Monitor database. This sevices uses dataPowerService for sending requests')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   3              ,'monitor'      ,'datapowerService' ,'ru.sberbank.syncserver2.service.sql.DataPowerService'                     ,null            ,null                ,3          ,'This service sends requests to DataPower and further to SQL Proxy Server in Alpha')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE         ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   4              ,'monitor'     , 'monitor'          ,'ru.sberbank.syncserver2.service.monitor.MonitorService'                   ,null            ,null                ,4          ,'This service starts spaceAndTimeCheck and use datapowerLogger to send notification to the database used by Alpha Monitor. It also sends pings to Alpha Monitor')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   5              ,'monitor'     , 'permissionCheck','ru.sberbank.syncserver2.service.monitor.check.WritePermissionCheck'  ,'monitor'       ,'checkAction'       ,5          ,'This service used by "monitor" to check write permission at file transporter')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   6              ,'monitor'     , 'pushNotificationCheck','ru.sberbank.syncserver2.service.monitor.check.PushNotificationChecker'  ,'monitor'       ,'checkAction'       ,6          ,'This service used by "monitor" to check notifications with error')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   7              ,'monitor'     , 'requestTimeCheck','ru.sberbank.syncserver2.service.monitor.check.OnlineExecutionTimeCheck'  ,'monitor'       ,'checkAction'       ,7          ,'This service used by "monitor" to check online requests execution time')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                        ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   8             ,'monitor'        ,'mdmGroupManager','ru.sberbank.syncserver2.service.ldap.LdapGroupManagementService'               ,'LDAPCheck'   ,'ldapService'      ,8         ,'Mdm group manager for checking LDAP')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   9              ,'monitor'     , 'LDAPCheck','ru.sberbank.syncserver2.service.monitor.check.LDAPAvailabilityCheck'  	,'monitor'       ,'checkAction'       ,9          ,'This service used by "monitor" to check LDAP availability')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   10              ,'monitor'     , 'onlineMonitor','ru.sberbank.syncserver2.service.monitor.check.OnlineInfrastructureCheck'  	,'monitor'       ,'checkAction'        ,10          ,'This service used by "monitor" to check infrastructure')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   11              ,'monitor'     , 'requestErrorsCheck','ru.sberbank.syncserver2.service.monitor.check.OnlineRequestErrorCheck'  ,'monitor'       ,'checkAction'       ,11          ,'This service used by "monitor" to check online request errors')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   12              ,'monitor'     , 'serversCertificateCheck','ru.sberbank.syncserver2.service.monitor.check.ServerTLSCertificateCheck'  ,'monitor'       ,'checkAction'       ,12          ,'This service used by "monitor" to check server''s certificates')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   13              ,'monitor'     , 'pushCertificateCheck','ru.sberbank.syncserver2.service.monitor.check.ServerPushCertificateCheck'  ,'monitor'       ,'checkAction'       ,13          ,'This service used by "monitor" to check push certificates')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   14              ,'monitor'     , 'monitorPingService','ru.sberbank.syncserver2.service.monitor.MonitorPingService'  ,'monitor'       ,'monitorPingService'       ,14          ,'This service sends pings to Alpha')
go
set identity_insert SYNC_SERVICES off
go

--4. Filling properties
--4.1 For MonitorServuice
exec SP_SYNC_RESET_PROPERTIES 'monitor','monitor'
go
exec SP_SYNC_RESET_PROPERTIES 'monitor','spaceAndTimeCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','includeFolders' ,'@FP_ROOT@'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','includeMaxMB'   ,'8192'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','includeMaxHours','24'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','excludeFolders' ,''
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','tempFolders'    ,'@FP_ROOT@\\OUT\\temp;@FP_ROOT@\\IN\\temp'

--exec SP_SYNC_SET_PROPERTY   'monitor','spaceAndTimeCheck','excludeFolders' ,'@FP_ROOT@\\IN\\prod\\mbr\\mbr_december_for_mis;@FP_ROOT@\\OUT\\temp;@FP_ROOT@\\IN\\temp'
--exec SP_SYNC_SET_PROPERTY   'monitor','spaceAndTimeCheck','tempFolders'    ,'@FP_ROOT@\\OUT\\temp;@FP_ROOT@\\IN\\temp'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','tempMaxHours'   ,'24'
go
exec SP_SYNC_RESET_PROPERTIES 'monitor','datapowerService'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerService','dataPowerURL1'   ,'@DATAPOWER_URL1@'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerService','dataPowerURL2'   ,'@DATAPOWER_URL2@'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerService','overrideProvider','DISPATCHER'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerService','overrideService1','@ALPHA_SQLPROXY_HOST1@'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerService','overrideService2','@ALPHA_SQLPROXY_HOST2@'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerService','conversion'      ,'CONVERT_TO_NEW_SIGMA'
go
exec SP_SYNC_RESET_PROPERTIES 'monitor','datapowerLogger'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerLogger','dataPowerBeanCode','datapowerService'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerLogger','databaseName','@ALPHA_MONITOR_DB@'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerLogger','provider'    ,'DISPATCHER'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerLogger','service'     ,'@ALPHA_MONITOR_SERVICE@'
go
exec SP_SYNC_RESET_PROPERTIES 'monitor','permissionCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','permissionCheck','includeFolders' ,'@FP_ROOT@\\IN'
go

exec SP_SYNC_RESET_PROPERTIES 'monitor','serversCertificateCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','serversCertificateCheck','monitoredHosts' ,'@CERTIFICATE_CHECK_HOSTS@'
exec SP_SYNC_SET_PROPERTY     'monitor','serversCertificateCheck','SSLProtocol' ,'@SSL_PROTOCOL@'
exec SP_SYNC_SET_PROPERTY     'monitor','serversCertificateCheck','timeInterval' ,'86400'
exec SP_SYNC_SET_PROPERTY     'monitor','serversCertificateCheck','notifyWithinDays' ,'@CERTIFICATE_CHECK_DAYS_BEFORE@'
go

exec SP_SYNC_RESET_PROPERTIES 'monitor','pushCertificateCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','pushCertificateCheck','folders' ,'@PUSH_CERTIFICATES_FOLDERS@;@ONLINE_REQUESTS_SETTING_FOLDER@'
exec SP_SYNC_SET_PROPERTY     'monitor','pushCertificateCheck','notifyWithinDays' ,'@CERTIFICATE_CHECK_DAYS_BEFORE@'
go

exec SP_SYNC_RESET_PROPERTIES 'monitor','pushNotificationCheck'
--exec SP_SYNC_SET_PROPERTY     'monitor','pushNotificationCheck','connectionService' ,'@PUSH_NOTIFICATIONS_SOURCE_SERVICE@'
go
exec SP_SYNC_RESET_PROPERTIES 'monitor','requestTimeCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','requestTimeCheck','hosts' 				,'@ONLINE_REQUESTS_HOSTS@'
exec SP_SYNC_SET_PROPERTY     'monitor','requestTimeCheck','settingsFile' 		,'@ONLINE_REQUESTS_SETTING_FOLDER@\\queries.txt'
exec SP_SYNC_SET_PROPERTY     'monitor','requestTimeCheck','certificateFile'	,'@ONLINE_REQUESTS_SETTING_FOLDER@\\certificate.p12'
exec SP_SYNC_SET_PROPERTY     'monitor','requestTimeCheck','passwordFile' 		,'@ONLINE_REQUESTS_SETTING_FOLDER@\\settings.properties'
exec SP_SYNC_SET_PROPERTY     'monitor','requestTimeCheck','SSLProtocol' 		,'@SSL_PROTOCOL@'
exec SP_SYNC_SET_PROPERTY     'monitor','requestTimeCheck','replaceParameters' 	,'@service@'
exec SP_SYNC_SET_PROPERTY     'monitor','requestTimeCheck','replaceValues' 		,'@ALPHA_SOURCE_SERVICE@'
go
exec SP_SYNC_RESET_PROPERTIES 'monitor','requestErrorsCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','requestErrorsCheck','hosts' 			,'@ONLINE_REQUESTS_HOSTS@'
exec SP_SYNC_SET_PROPERTY     'monitor','requestErrorsCheck','settingsFolder' 	,'@ONLINE_REQUESTS_SETTING_FOLDER@\\groups'
exec SP_SYNC_SET_PROPERTY     'monitor','requestErrorsCheck','certificateFile'	,'@ONLINE_REQUESTS_SETTING_FOLDER@\\certificate.p12'
exec SP_SYNC_SET_PROPERTY     'monitor','requestErrorsCheck','passwordFile' 	,'@ONLINE_REQUESTS_SETTING_FOLDER@\\settings.properties'
exec SP_SYNC_SET_PROPERTY     'monitor','requestErrorsCheck','SSLProtocol' 		,'@SSL_PROTOCOL@'
exec SP_SYNC_SET_PROPERTY     'monitor','requestErrorsCheck','timeInterval' 	,'600'
exec SP_SYNC_SET_PROPERTY     'monitor','requestErrorsCheck','replaceParameters','@service@'
exec SP_SYNC_SET_PROPERTY     'monitor','requestErrorsCheck','replaceValues' 	,'@ALPHA_SOURCE_SERVICE@'
go
exec SP_SYNC_RESET_PROPERTIES 'monitor','onlineMonitor'
exec SP_SYNC_SET_PROPERTY     'monitor','onlineMonitor','alphaSourceServices' 	,'@ALPHA_ALL_TARGET_SERVICES@'
exec SP_SYNC_SET_PROPERTY     'monitor','onlineMonitor','hosts' 			,'@ONLINE_REQUESTS_HOSTS@'
exec SP_SYNC_SET_PROPERTY     'monitor','onlineMonitor','certificateFile' 	,'@ONLINE_REQUESTS_SETTING_FOLDER@\\certificate.p12'
exec SP_SYNC_SET_PROPERTY     'monitor','onlineMonitor','passwordFile' 		,'@ONLINE_REQUESTS_SETTING_FOLDER@\\settings.properties'
exec SP_SYNC_SET_PROPERTY     'monitor','onlineMonitor','SSLProtocol' 		,'@SSL_PROTOCOL@'
go

exec SP_SYNC_RESET_PROPERTIES 'monitor','mdmGroupManager'
exec SP_SYNC_SET_PROPERTY     'monitor','mdmGroupManager','provider'    ,'@LDAP_PROVIDER@'
exec SP_SYNC_SET_PROPERTY     'monitor','mdmGroupManager','domain'      ,'@LDAP_DOMAIN@'
exec SP_SYNC_SET_PROPERTY     'monitor','mdmGroupManager','settings'    ,'@LDAP_USER_GROUP_MANAGER_SETTINGS@'
go

