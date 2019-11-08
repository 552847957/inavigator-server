use master;
go;
SELECT @@version;
GO
IF SUSER_ID('generator') IS NOT NULL
  DROP LOGIN generator;
GO
IF USER_ID('generator') IS NOT NULL
  DROP USER generator;
GO
IF db_id('generator') IS NOT NULL
  DROP DATABASE generator;
GO
CREATE DATABASE generator
ON PRIMARY
  (
  NAME = N'generator',
  FILENAME = N'D:\Program Files\Microsoft SQL Server\MSSQL10_50.MSSQLSERVER\MSSQL\DATA\generator2.mdf'
  )
LOG ON
  (
  NAME = N'generator_log',
  FILENAME = N'D:\Program Files\Microsoft SQL Server\MSSQL10_50.MSSQLSERVER\MSSQL\DATA\generator2_log.ldf'
  );
GO
CREATE LOGIN generator WITH PASSWORD = 'Qwerty123456';
GO
CREATE USER generator FOR LOGIN generator WITH DEFAULT_SCHEMA = dbo;
GO
GRANT ALL TO generator;
GO
GRANT CONNECT SQL TO generator;
GO
GRANT CONNECT TO generator;
GO
EXECUTE sp_addrolemember db_datawriter, generator;
GO
EXECUTE sp_addrolemember db_datareader, generator;
GO
EXECUTE sp_addrolemember db_ddladmin, generator;
GO
EXECUTE sp_addrolemember db_owner, generator;
GO
grant create TABLE to generator;
GO
grant select to generator;
GO
GRANT CONTROL TO generator;
go
GRANT insert TO generator;
go
GRANT update TO generator;
go
GRANT delete TO generator;
go
alter user generator WITH DEFAULT_SCHEMA = dbo;
go
