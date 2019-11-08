--execute must under jdbc:sqlserver://dbhost:dbport;databaseName=master;user=sa_admin;

USE master;
GO
IF SUSER_ID('MIS_IPAD_SYNCSERVER') IS NOT NULL
  BEGIN
    DROP LOGIN MIS_IPAD_SYNCSERVER;
    SELECT 'kill login MIS_IPAD_SYNCSERVER';
  END;
GO
IF USER_ID('MIS_IPAD_SYNCSERVER') IS NOT NULL
  BEGIN
    DROP USER MIS_IPAD_SYNCSERVER;
    SELECT 'kill user MIS_IPAD_SYNCSERVER';
  END;
GO
IF db_id('MIS_IPAD_SYNCSERVER') IS NOT NULL
  BEGIN
    DROP DATABASE MIS_IPAD_SYNCSERVER;
    SELECT 'kill database MIS_IPAD_SYNCSERVER';
  END;
GO
CREATE DATABASE MIS_IPAD_SYNCSERVER
ON PRIMARY
  (
  NAME = N'MIS_IPAD_SYNCSERVER',
  FILENAME = N'D:\Program Files\Microsoft SQL Server\MSSQL10_50.MSSQLSERVER\MSSQL\DATA\MIS_IPAD_SYNCSERVER2.mdf'
  )
LOG ON
  (
  NAME = N'MIS_IPAD_SYNCSERVER_log',
  FILENAME = N'D:\Program Files\Microsoft SQL Server\MSSQL10_50.MSSQLSERVER\MSSQL\DATA\MIS_IPAD_SYNCSERVER2_log.ldf'
  );
GO
USE MIS_IPAD_SYNCSERVER;
GO
CREATE LOGIN MIS_IPAD_SYNCSERVER WITH PASSWORD = 'Qwerty123456';
GO
CREATE USER MIS_IPAD_SYNCSERVER FOR LOGIN MIS_IPAD_SYNCSERVER;
GO
GRANT ALL TO MIS_IPAD_SYNCSERVER;
GO
GRANT CONNECT TO MIS_IPAD_SYNCSERVER;
GO
EXECUTE sp_addrolemember db_datawriter, MIS_IPAD_SYNCSERVER;
GO
EXECUTE sp_addrolemember db_datareader, MIS_IPAD_SYNCSERVER;
GO
EXECUTE sp_addrolemember db_ddladmin, MIS_IPAD_SYNCSERVER;
GO
EXECUTE sp_addrolemember db_owner, MIS_IPAD_SYNCSERVER;
GO