
set nocount on
go
IF OBJECT_ID('SYNC_CONFIG') IS NOT NULL DROP TABLE SYNC_CONFIG
GO
CREATE TABLE SYNC_CONFIG(
   PROPERTY_KEY    VARCHAR(256)  PRIMARY KEY,
   PROPERTY_VALUE  VARCHAR(1024) NOT NULL,
   PROPERTY_GROUP  VARCHAR(128)	     NULL,
   PROPERTY_DESC   VARCHAR(1024)     NULL
)
GO
SET NOCOUNT ON
INSERT INTO SYNC_CONFIG VALUES('ROOT_FOLDER'                           ,'D:/usr/cache'								,NULL		,'Корневая папка для локального кэша')
INSERT INTO SYNC_CONFIG VALUES('NETWORK_ROOT_FOLDER'                   ,'\\\\brass2\\box1\\i-navigator\\IN\\uat4'	,NULL		,'Корневая папка на файлоперекладчике')
INSERT INTO SYNC_CONFIG VALUES('NETWORK_SHARED_HOSTS_FOR_CHANGESETS'   ,'localhost'									,NULL		,'Список хостов для iRubricator через ";". Changeset-ы будут загружены только на указанные сервера')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_URL1'                        ,'http://10.21.136.238:4004'					,'Настройки DataPower'	,'DataPower URL #1. Сервер будет автоматически балансировать запросы между DataPower URL #1 and DataPower URL #2. При ошибке передачи запроса до MSSQL будет предпринята попытка отправить через другой DataPower')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_URL2'                        ,'http://10.21.136.238:4004'					,'Настройки DataPower'	,'DataPower URL #2. Сервер будет автоматически балансировать запросы между DataPower URL #1 and DataPower URL #2. При ошибке передачи запроса до MSSQL будет предпринята попытка отправить через другой DataPower')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SQLPROXY_HOST1'                  ,'finik2-new'								,'Настройки DataPower'	,'Адрес SQL Proxy Server используемый c DataPower #1')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SQLPROXY_HOST2'                  ,'finik2-new'								,'Настройки DataPower'	,'Адрес SQL Proxy Server используемый c DataPower #2')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SOURCE_SERVICE'                  ,'finik2-new'								,'Настройки ALPHA'		,'имя сервиса (TARGET) в SQL Proxy для доступа к ALPHA_SOURCE_DB на finik1/finik2')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_DB'                      ,'MIS_IPAD_MONITOR'							,'Настройки ALPHA'		,'имя базы данных Alpha Monitor-а')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_SERVICE'                 ,'finik2-new'								,'Настройки ALPHA'		,'имя сервиса (TARGET) в SQL Proxy для доступа к базе данных Alpha Monitor-а')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_GENERATOR_DB'                    ,'MIS_IPAD_GENERATOR'						,'Настройки ALPHA'		,'имя базы данных Generator-а')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_GENERATOR_SERVICE'               ,'finik2-new'								,'Настройки ALPHA'		,'имя сервиса (TARGET) в SQL Proxy для доступа к базе данных Generator-а')

INSERT INTO SYNC_CONFIG VALUES('LDAP_PROVIDER'                         ,'ldap://lake1.sigma.sbrf.ru:389;ldap://lake2.sigma.sbrf.ru:389'	,'Настройки LDAP'	,'Адреса серверов LDAP через ";"')
INSERT INTO SYNC_CONFIG VALUES('LDAP_DOMAIN'                           ,'SIGMA'                 					,'Настройки LDAP'		,'имя Windows Domain')
INSERT INTO SYNC_CONFIG VALUES('LDAP_USERNAME'                         ,'IncidentManagement'    					,'Настройки LDAP'		,'имя пользователя LDAP')
INSERT INTO SYNC_CONFIG VALUES('LDAP_PASSWORD'                         ,'c,th,fyr2013'          					,'Настройки LDAP'		,'Пароль пользователя LDAP')
INSERT INTO SYNC_CONFIG VALUES('PUSH_CERTIFICATE_CONFIG_FOLDER'        ,'D:/usr/cache/push'							,'Настройки PUSH сервисов','Путь до корневой папки с push сертификатами')
INSERT INTO SYNC_CONFIG VALUES('PUSH_NOTIFICATION_WHEN_NEW_FILE_LOADED','false'										,'Настройки PUSH сервисов','Отправлять Push-уведомление пользователям при загрузке файла в sigma')
INSERT INTO SYNC_CONFIG VALUES('LDAP_USER_GROUP_MANAGER_SETTINGS'	,''												,'Настройки LDAP'		,'Java настройки в XML формате для ldap соединения (provider, domain, username, password, iNavigatorGroupLdapDN, base_ctx)')
INSERT INTO SYNC_CONFIG VALUES('PUSH_NOTIFICATIONS_APP_NAME'		,'iNavigator2'									,'Настройки PUSH сервисов','имя приложений (appName) через запятую, пользователям которых предназначаются push уведомления из БД источника')
INSERT INTO SYNC_CONFIG VALUES('PUSH_NOTIFICATIONS_SOURCE_SERVICE'	,'finik2-new'									,'Настройки PUSH сервисов','имя сервиса (TARGET) в SQL Proxy для доступа к источнику push уведомлений в Alpha')
INSERT INTO SYNC_CONFIG VALUES('MAINTENANCE_HOSTS'					,'http://10.21.137.27:9080'						,NULL		,'Внутренние адреса серверов monitor-sigma через ";", которые будут использоваться для обновления статуса режима тех. поддержки')
INSERT INTO SYNC_CONFIG VALUES('IS_DB_LOGGING_ENABLED'                 ,'true'										,NULL		,'Включить/выключить логирование в БД. Скрыто используется сервисом adminDbLogService')
INSERT INTO SYNC_CONFIG VALUES('SKIP_EMAIL_VERIFICATION'               ,'false'										,'Настройки ALPHA'		,'Пропускать сверку email в запросе с email в личном сертификате')
INSERT INTO SYNC_CONFIG VALUES('SKIP_EMAIL_VERIFICATION_IP_LIST'       ,''						    				,'Настройки ALPHA'		,'Список ip адресов, для которых надо пропускать сверку email в запросе с email в личном сертификате')
GO
