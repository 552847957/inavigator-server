BEGIN TRANSACTION;
DELETE FROM SYNC_CONFIG
WHERE PROPERTY_KEY LIKE 'QLIK_SENSE_%';
INSERT INTO SYNC_CONFIG VALUES ('QLIK_SENSE_TARGET1', 'QLIK_SENSE', 'Настройки Proxy QLIK_SENSE TARGET 1',
                                'имя сервиса (TARGET) для proxyQlikSenseService. proxyDispatcherService будет использовать это имя для делегирования запроса нужному сервису');
INSERT INTO SYNC_CONFIG VALUES
  ('QLIK_SENSE_SERVER_HOST1', 'sbt-csit-011.ca.sbrf.ru', 'Настройки Proxy QLIK_SENSE TARGET 1', 'Хост с Qlik Sense');
INSERT INTO SYNC_CONFIG
VALUES ('QLIK_SENSE_SERVER_PORT1', '4747', 'Настройки Proxy QLIK_SENSE TARGET 1', 'Порт на Qlik Sense');
INSERT INTO SYNC_CONFIG VALUES
  ('QLIK_SENSE_SERVER_CONTEXT1', '/app', 'Настройки Proxy QLIK_SENSE TARGET 1', 'Контекст (часть URL) на Qlik Sense');
INSERT INTO SYNC_CONFIG VALUES
  ('QLIK_SENSE_SERTIFICATE_DIR1', './qlik_sense/sertificate/1', 'Настройки Proxy QLIK_SENSE TARGET 1',
   'Директория с сертификатами для доступа на Qlik Sense');
INSERT INTO SYNC_CONFIG VALUES
  ('QLIK_SENSE_ROOT_SERTIFICATE_FILE_NAME1', 'root.pem', 'Настройки Proxy QLIK_SENSE TARGET 1',
   'Корневой сертификат для доступа на Qlik Sense');
INSERT INTO SYNC_CONFIG VALUES
  ('QLIK_SENSE_CLIENT_SERTIFICATE_FILE_NAME1', 'client.pem', 'Настройки Proxy QLIK_SENSE TARGET 1',
   'Сертификат клиента для доступа на Qlik Sense');
INSERT INTO SYNC_CONFIG VALUES
  ('QLIK_SENSE_CLIENT_KEY_FILE_NAME1', 'client_key_8.pem', 'Настройки Proxy QLIK_SENSE TARGET 1',
   'Файл с клиентским ключом для доступа на Qlik Sense');
INSERT INTO SYNC_CONFIG VALUES ('QLIK_SENSE_CLIENT_KEY_PASSWORD1', '', 'Настройки Proxy QLIK_SENSE TARGET 1',
                                'Пароль к файлу клиентского ключа для доступа на Qlik Sense');
INSERT INTO SYNC_CONFIG
VALUES ('QLIK_SENSE_USER_NAME1', 'sbt-biryukov-su', 'Настройки Proxy QLIK_SENSE TARGET 1', 'Имя пользователя');
INSERT INTO SYNC_CONFIG
VALUES ('QLIK_SENSE_USER_DOMAIN1', 'ALPHA', 'Настройки Proxy QLIK_SENSE TARGET 1', 'Домен пользователя');
INSERT INTO SYNC_CONFIG VALUES ('QLIK_SENSE_POOL_MAX_TOTAL1', '8', 'Настройки Proxy QLIK_SENSE TARGET 1',
                                'Максимальное количество соединений в пуле');
INSERT INTO SYNC_CONFIG VALUES
  ('QLIK_SENSE_POOL_MAX_IDLE1', '8', 'Настройки Proxy QLIK_SENSE TARGET 1', 'Максимальное количество ожиданий в пуле');
INSERT INTO SYNC_CONFIG VALUES
  ('QLIK_SENSE_POOL_MIN_IDLE1', '0', 'Настройки Proxy QLIK_SENSE TARGET 1', 'Минимальное количество ожиданий в пуле');
COMMIT;
GO


DECLARE @ListTamplateId INT;
SET @ListTamplateId = (SELECT LIST_TEMPLATE_ID
                       FROM PROPERTY_LIST_TEMPLATES
                       WHERE LIST_TEMPLATE_CODE = 'ru.sberbank.syncserver2.service.sql.QlikSenseService');
IF @ListTamplateId IS NULL
  BEGIN
    DECLARE @id INT;
    EXEC SP_SYNC_ADD_TEMPLATE2 @id OUT, 'ru.sberbank.syncserver2.service.sql.QlikSenseService';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'serviceName', 'N',
                                        'This parameter used for distribution request between several services according to the content of tag "service" in online request';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'serverHost', 'N', 'Host Qlik Sense server';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'serverPort', 'N', 'Port Qlik Sense server';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'serverContext', 'N', 'URI context for request';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'certificateDir', 'N', 'Directory with sertificates';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'rootCertificateFileName', 'N',
                                        'Root sertificate file name in certificateDir for access to Qlik Sense';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'clientCertificateFileName', 'N',
                                        'Client sertificate file name in certificateDir for access to Qlik Sense';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'clientKeyPathFileName', 'N',
                                        'File name for file with client key in certificateDir for access to client sertificate';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'clientKeyPassword', 'N', 'Password for client key file';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'user', 'N', 'User name to access Qlik Sense server';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'domain', 'N', 'Domain for user';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'maxTotal', 'N',
                                        'The maximum number of active connections that can be allocated from this pool at the same time';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'maxIdle', 'N',
                                        'The maximum number of active connections that can remain idle in the pool, without extra ones being created';
    EXEC SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'minIdle', 'N',
                                        'The minimum number of active connections that can remain idle in the pool, without extra ones being created';
  END;
GO


BEGIN TRANSACTION;
DECLARE @proxyQlikSenseService1Cnt INT;
SET @proxyQlikSenseService1Cnt = (SELECT count(1)
                                  FROM SYNC_SERVICES
                                  WHERE BEAN_CODE = 'proxyQlikSenseService1');
IF @proxyQlikSenseService1Cnt > 0
  BEGIN
    DELETE FROM SYNC_SERVICES
    WHERE BEAN_CODE = 'proxyQlikSenseService1';
  END;
DECLARE @LastServiceId INT;
SET @LastServiceId = (SELECT max(SYNC_SERVICE_ID) + 1 LastServiceId
                      FROM SYNC_SERVICES);
SELECT @LastServiceId;
SET IDENTITY_INSERT SYNC_SERVICES ON;
INSERT INTO SYNC_SERVICES (SYNC_SERVICE_ID, SYNC_FOLDER_CODE, BEAN_CODE, BEAN_CLASS, PARENT_BEAN_CODE, PARENT_BEAN_PROPERTY, START_ORDER, SERVLET_PATH, BEAN_DESC)
VALUES (@LastServiceId, 'proxy', 'proxyQlikSenseService1', 'ru.sberbank.syncserver2.service.sql.QlikSenseService',
        'proxyDispatcherService', 'subService', @LastServiceId, NULL,
        'This service sends wss request to the Qlik Sense server.');
SET IDENTITY_INSERT SYNC_SERVICES OFF
COMMIT;
GO

EXEC SP_SYNC_RESET_PROPERTIES 'proxy', 'proxyQlikSenseService1'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'serviceName', '@QLIK_SENSE_TARGET1@'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'serverHost', '@QLIK_SENSE_SERVER_HOST1@'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'serverPort', '@QLIK_SENSE_SERVER_PORT1@'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'serverContext', '@QLIK_SENSE_SERVER_CONTEXT1@'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'certificateDir', '@QLIK_SENSE_SERTIFICATE_DIR1@'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'rootCertificateFileName',
                              '@QLIK_SENSE_ROOT_SERTIFICATE_FILE_NAME1@'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'clientCertificateFileName',
                              '@QLIK_SENSE_CLIENT_SERTIFICATE_FILE_NAME1@'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'clientKeyPathFileName',
                              '@QLIK_SENSE_CLIENT_KEY_FILE_NAME1@'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'clientKeyPassword',
                              '@QLIK_SENSE_CLIENT_KEY_PASSWORD1@'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'user', '@QLIK_SENSE_USER_NAME1@'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'domain', '@QLIK_SENSE_USER_DOMAIN1@'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'maxTotal', '@QLIK_SENSE_POOL_MAX_TOTAL1@'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'maxIdle', '@QLIK_SENSE_POOL_MAX_IDLE1@'
EXEC SP_SYNC_SET_PROPERTY     'proxy', 'proxyQlikSenseService1', 'minIdle', '@QLIK_SENSE_POOL_MIN_IDLE1@'
GO
