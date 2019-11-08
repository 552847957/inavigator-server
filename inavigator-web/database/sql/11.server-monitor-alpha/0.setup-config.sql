go
set nocount on
go
IF OBJECT_ID('SYNC_CONFIG') IS NOT NULL DROP TABLE SYNC_CONFIG
GO
CREATE TABLE SYNC_CONFIG(
   PROPERTY_KEY    VARCHAR(256) PRIMARY KEY,
   PROPERTY_VALUE  VARCHAR(1024) NOT NULL,
   PROPERTY_GROUP  VARCHAR(128),
   PROPERTY_DESC   VARCHAR(1024)
)
GO
SET NOCOUNT ON
INSERT INTO SYNC_CONFIG VALUES('ALERT_TRANSPORTS'               ,'SMS'                           ,'Настройки уведомлений'		,'Способ уведомления. Поддерживаются: SMS | SMTP | SMS;SMTP')
INSERT INTO SYNC_CONFIG VALUES('ALERT_ADDRESSES'                ,''                              ,'Настройки уведомлений'		,'Список e-mail-ов через ";"')
INSERT INTO SYNC_CONFIG VALUES('ALERT_PHONES'                   ,''                              ,'Настройки уведомлений'		,'Список телефонов в формате 79857619675 через ";"')
INSERT INTO SYNC_CONFIG VALUES('SMS_PROXY_SERVER'               ,'http://10.67.3.52:9080/DpSmsProxy/sendSms','Настройки уведомлений'		,'URL сервера DpSmsProxy для отправки смс')
INSERT INTO SYNC_CONFIG VALUES('SMTP_HOST'                      ,'127.0.0.1'                     ,'Настройки уведомлений'		,'SMTP Host')
INSERT INTO SYNC_CONFIG VALUES('SMTP_PORT'                      ,'25'                            ,'Настройки уведомлений'		,'SMTP Port')
INSERT INTO SYNC_CONFIG VALUES('SMTP_FROM'                      ,'navmonitor'                    ,'Настройки уведомлений'		,'e-mail отправителя')
INSERT INTO SYNC_CONFIG VALUES('SMTP_USER'                      ,''                              ,'Настройки уведомлений'		,'SMTP User')
INSERT INTO SYNC_CONFIG VALUES('SMTP_PASSWORD'                  ,''                              ,'Настройки уведомлений'		,'SMTP Password')
INSERT INTO SYNC_CONFIG VALUES('FP_ROOT'                        ,'\\\\bronze2\\vol1\\i-navigator',NULL					,'Корневая папка на файлоперекладчике для мониторинга')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_DB'               ,'MIS_IPAD_MONITOR'				 ,NULL					,'имя базы данных Alpha Monitor-а. Эта БД должна быть на том же MSSQL Server что и datasource MONITOR_DB на WebSphere')
INSERT INTO SYNC_CONFIG VALUES('WAITING_AUTOGEN_INTERVAL'       ,'1'							 ,'Настройки автогенерации'				,'Если генерация не начнется в течение указанных минут, будет добавлено уведомление в ТП')
INSERT INTO SYNC_CONFIG VALUES('GENERATOR_MONITORING_URLS'      ,'http://localhost:9090/generator/public/request.do','Настройки автогенерации'		,'URL к сервису generator-а для мониторинга автогенерации')
INSERT INTO SYNC_CONFIG VALUES('SIGMA_MONITOR_HOSTS'            ,'sbtrpu004'					 ,NULL					,'Список всех хостов monitor-sigma через ";"')
INSERT INTO SYNC_CONFIG VALUES('IS_DB_LOGGING_ENABLED'          ,'true'							 ,NULL					,'Включить/выключить логирование в БД. Скрыто используется сервисом adminDbLogService.')
GO
