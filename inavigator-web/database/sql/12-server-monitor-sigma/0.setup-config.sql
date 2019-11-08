set nocount on
go
IF OBJECT_ID('SYNC_CONFIG') IS NOT NULL DROP TABLE SYNC_CONFIG
GO
CREATE TABLE SYNC_CONFIG(
   PROPERTY_KEY    VARCHAR(256)  PRIMARY KEY,
   PROPERTY_VALUE  VARCHAR(max)     NOT NULL,
   PROPERTY_GROUP  VARCHAR(128)	     NULL,
   PROPERTY_DESC   VARCHAR(1024)     NULL,
)
GO
SET NOCOUNT ON
INSERT INTO SYNC_CONFIG VALUES('FP_ROOT'                        ,'\\\\brass2\\box1\\i-navigator',NULL		,'Корневая папка на файлоперекладчике для мониторинга')
INSERT INTO SYNC_CONFIG VALUES('IS_DB_LOGGING_ENABLED'          ,'true'							,NULL		,'Включить/выключить логирование в БД. Скрыто используется сервисом adminDbLogService.')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_URL1'                 ,'http://10.21.136.238:4004'	,'Настройки DataPower'		,'DataPower URL #1. Сервер будет автоматически балансировать запросы между DataPower URL #1 and DataPower URL #2. При ошибке передачи запроса до MSSQL будет предпринята попытка отправить через другой DataPower')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_URL2'                 ,'http://10.21.136.238:4004'	,'Настройки DataPower'		,'DataPower URL #2. Сервер будет автоматически балансировать запросы между DataPower URL #1 and DataPower URL #2. При ошибке передачи запроса до MSSQL будет предпринята попытка отправить через другой DataPower')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SQLPROXY_HOST1'           ,'finik2-new'					,'Настройки DataPower'		,'Адрес SQL Proxy Server используемый c DataPower #1')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SQLPROXY_HOST2'           ,'finik2-new'					,'Настройки DataPower'		,'Адрес SQL Proxy Server используемый c DataPower #2')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_DB'               ,'MIS_IPAD_MONITOR2'			,'Настройки ALPHA'		,'имя базы данных Alpha Monitor-а')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_SERVICE'          ,'finik2-new'					,'Настройки ALPHA'		,'имя сервиса (TARGET) в SQL Proxy для доступа к базе данных Alpha Monitor-а')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_ALL_TARGET_SERVICES'          ,'finik2-new;finik1-new'		,'Настройки ONLINE запросов'		,'имя сервисов (TARGET) в SQL Proxy для мониторинга инфраструктуры через ";" (не более 3)')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SOURCE_SERVICE'          ,'finik2-new'					,'Настройки ONLINE запросов'		,'имя сервиса (TARGET) в SQL Proxy для тестовых онлайн запросов к БД источника')
--INSERT INTO SYNC_CONFIG VALUES('PUSH_NOTIFICATIONS_SOURCE_SERVICE','finik1-new'					,'Настройки ONLINE запросов'		,'The service name at SQL Proxy used to communicate with Alpha push notification source database')
INSERT INTO SYNC_CONFIG VALUES('ONLINE_REQUESTS_HOSTS'			,'https://i-navigator.sbrf.ru'	,'Настройки ONLINE запросов'		,'Адреса серверов (с использованием https) для проверочных онлайн запросов через ";"')
INSERT INTO SYNC_CONFIG VALUES('ONLINE_REQUESTS_SETTING_FOLDER'	,'C:/Disk_E'					,'Настройки ONLINE запросов'		,'Корневая папка для хранения сертификата, пароля и проверочных онлайн запросов')
INSERT INTO SYNC_CONFIG VALUES('CERTIFICATE_CHECK_HOSTS'	    ,'config1.mobile-test.sbrf.ru:9443'	,'Настройки прочих чекеров'		,'Список хостов для проверки сертификатов в формате name:port;name:port (port является необязательным, по умолчанию 443)')
INSERT INTO SYNC_CONFIG VALUES('CERTIFICATE_CHECK_DAYS_BEFORE'	,'30'								,'Настройки прочих чекеров'		,'Количество дней до истечения сертификата для уведомлений')
INSERT INTO SYNC_CONFIG VALUES('PUSH_CERTIFICATES_FOLDERS'		,'C:/usr/push'								,'Настройки прочих чекеров'		,'Список корневых папок для мониторинга сертификатов (через ;)')
INSERT INTO SYNC_CONFIG VALUES('SSL_PROTOCOL'					,'TLSv1.2'						,'Настройки ONLINE запросов'		,'Версия Secure socket protocol используемая для соедиения с online серверами (syncserver)')
INSERT INTO SYNC_CONFIG VALUES('LDAP_PROVIDER'                  ,'ldap://lake1.sigma.sbrf.ru:389;ldap://lake2.sigma.sbrf.ru:389','Настройки LDAP'		,'Адреса LDAP Server через ";"')
INSERT INTO SYNC_CONFIG VALUES('LDAP_DOMAIN'                    ,'SIGMA'                   		,'Настройки LDAP'		,'имя Windows Domain')
INSERT INTO SYNC_CONFIG VALUES('LDAP_USER_GROUP_MANAGER_SETTINGS','IncidentManagement//c,th,fyr2013//CN=i-Navigator-U,OU=NetApp,DC=sigma,DC=sbrf,DC=ru//DC=sigma,DC=sbrf,DC=ru','Настройки LDAP'		,'Java настройки в XML формате для ldap соединения (provider, domain, username, password, iNavigatorGroupLdapDN, base_ctx)')
GO

