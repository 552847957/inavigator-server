--execute must under jdbc:sqlserver://dbhost:dbport;databaseName=master;user=sa_admin;

USE master;
GO
IF SUSER_ID('proxyserver') IS NOT NULL
  BEGIN
    DROP LOGIN proxyserver;
    SELECT 'kill login proxyserver';
  END;
GO
IF USER_ID('proxyserver') IS NOT NULL
  BEGIN
    DROP USER proxyserver;
    SELECT 'kill user proxyserver';
  END;
GO
IF db_id('proxyserver') IS NOT NULL
  BEGIN
    DROP DATABASE proxyserver;
    SELECT 'kill database proxyserver';
  END;
GO
CREATE DATABASE proxyserver
ON PRIMARY
  (
  NAME = N'proxyserver',
  FILENAME = N'D:\Program Files\Microsoft SQL Server\MSSQL10_50.MSSQLSERVER\MSSQL\DATA\proxyserver2.mdf'
  )
LOG ON
  (
  NAME = N'proxyserver_log',
  FILENAME = N'D:\Program Files\Microsoft SQL Server\MSSQL10_50.MSSQLSERVER\MSSQL\DATA\proxyserver2_log.ldf'
  );
GO
USE proxyserver;
GO
CREATE LOGIN proxyserver WITH PASSWORD = 'Qwerty123456';
GO
CREATE USER proxyserver FOR LOGIN proxyserver;
GO
GRANT ALL TO proxyserver;
GO
GRANT CONNECT TO proxyserver;
GO
EXECUTE sp_addrolemember db_datawriter, proxyserver;
GO
EXECUTE sp_addrolemember db_datareader, proxyserver;
GO
EXECUTE sp_addrolemember db_ddladmin, proxyserver;
GO
EXECUTE sp_addrolemember db_owner, proxyserver;
GO
