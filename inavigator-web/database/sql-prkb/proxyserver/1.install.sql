go
if object_id('SYNC_CONFIG') is not null DROP TABLE SYNC_CONFIG
GO
CREATE TABLE SYNC_CONFIG(
   PROPERTY_KEY    VARCHAR(256) ,
   PROPERTY_VALUE  VARCHAR(1024), 
   PROPERTY_DESC   VARCHAR(1024) 
)
GO
SET NOCOUNT ON
INSERT INTO SYNC_CONFIG VALUES('ROOT_FOLDER'                    ,'E:/Disk_E/mis_file/cacheUAT/proxy','Root folder for local cache')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_TARGET1'              ,'finik1-new','proxyDispatcherService uses DATAPOWER_TARGET1 to dispatch request to a correct proxyMSSQLService')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_URL1'                     ,'jdbc:sqlserver://finik1:1433;databaseName=MIS_IPAD_PROXYSERVER','JDBC Url to first source database')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_USER1'                    ,'mis_user','JDBC User')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_PASSWORD1'                ,'mis','JDBC Password')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_TARGET2'              ,'finik2-new','proxyDispatcherService uses DATAPOWER_TARGET1 to dispatch request to a correct proxyMSSQLService')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_URL2'                     ,'jdbc:sqlserver://finik2:1433;databaseName=MIS_IPAD_PROXYSERVER','JDBC Url to second source database')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_USER2'                    ,'mis_user','JDBC User')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_PASSWORD2'                ,'mis','JDBC Password')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_TARGET3'              ,'finik4-new','proxyDispatcherService uses DATAPOWER_TARGET3 to dispatch request to a correct proxyMSSQLService')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_URL3'                     ,'jdbc:sqlserver://finik4:1433;databaseName=MIS_RUBRICATOR','JDBC Url to third source database')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_USER3'                    ,'mis_user','JDBC User')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_PASSWORD3'                ,'mis','JDBC Password')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_TARGET4'              ,'mis-generator','proxyDispatcherService uses DATAPOWER_TARGET4 to dispatch request to a correct proxyMSSQLService')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_URL4'                     ,'jdbc:sqlserver://finik2:1433;databaseName=MIS_IPAD_GENERATOR','JDBC Url to fouth source database')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_USER4'                    ,'mis_user','JDBC User')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_PASSWORD4'                ,'mis','JDBC Password')
INSERT INTO SYNC_CONFIG VALUES('IS_DB_LOGGING_ENABLED'          ,'true','Enables or disables logging to the database')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_CONVERSION'           ,'CONVERT_TO_PASSPORT_ALPHA','Defines interaction with datapower: CONVERT_TO_TEMP_ALPHA or CONVERT_TO_OLD are applicable')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SOURCE_DB'                ,'MIS_IPAD','The name of database at finik1/2 with procedures for checking permissions SP_IS_ALLOWED_TO_DOWNLOAD_FILE and SP_IS_ALLOWED_TO_USE_APP')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_DB'               ,'MIS_IPAD_MONITOR','Alpha Monitor Database name')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_GENERATOR_DB'             ,'MIS_IPAD_GENERATOR','The name of database in Alpha used by Generator')

GO

set nocount
on
if object_id('SYNC_SERVICES') is not null DROP TABLE SYNC_SERVICES
GO
if object_id('SYNC_FOLDERS') is not null DROP TABLE SYNC_FOLDERS
GO
if object_id('PROPERTY_VALUES') is not null DROP TABLE PROPERTY_VALUES
GO
if object_id('PROPERTY_LISTS') is not null DROP TABLE PROPERTY_LISTS
go
if object_id('PROPERTY_TEMPLATES') is not null DROP TABLE PROPERTY_TEMPLATES
go
if object_id('PROPERTY_LIST_TEMPLATES') is not null DROP TABLE PROPERTY_LIST_TEMPLATES
go
if object_id('EMPLOYEES') is not null DROP TABLE EMPLOYEES
GO
if object_id('EMPLOYEE_ROLES') is not null DROP TABLE EMPLOYEE_ROLES
GO
if object_id('SP_SYNC_LIST_FOLDERS') is not null DROP PROCEDURE SP_SYNC_LIST_FOLDERS
GO
if object_id('SP_SYNC_COMPOSE_PROPERTY_LIST_CODE') is not null DROP PROCEDURE SP_SYNC_COMPOSE_PROPERTY_LIST_CODE
GO
if object_id('SP_SYNC_LIST_PROPERTIES') is not null DROP PROCEDURE SP_SYNC_LIST_PROPERTIES
go
if object_id('SP_SYNC_RESET_PROPERTIES') is not null DROP PROCEDURE SP_SYNC_RESET_PROPERTIES
go
if object_id('SP_SYNC_SET_PROPERTY') is not null DROP PROCEDURE SP_SYNC_SET_PROPERTY
go
if object_id('SP_SYNC_LIST_TEMPLATES') is not null DROP PROCEDURE SP_SYNC_LIST_TEMPLATES
go
if object_id('SP_SYNC_LIST_SERVICES') is not null DROP PROCEDURE SP_SYNC_LIST_SERVICES
go
if object_id('SP_SYNC_LIST_SERVICES_FOR_DISPATCHER') is not null DROP PROCEDURE SP_SYNC_LIST_SERVICES_FOR_DISPATCHER
go

/*
 *
 */
CREATE TABLE EMPLOYEES(
  EMPLOYEE_ID       INT IDENTITY(1,1) PRIMARY KEY,
  EMPLOYEE_ROLE_ID  INT          NOT NULL,
  EMPLOYEE_EMAIL    VARCHAR(128) NOT NULL,
  EMPLOYEE_NAME     VARCHAR(256) NOT NULL,
  leave_date        DATETIME,
  EMPLOYEE_PASSWORD VARCHAR(256) NOT NULL,
  PASSWORD_CHANGED_DATE DATETIME	
)
go
CREATE TABLE EMPLOYEE_ROLES(
  EMPLOYEE_ROLE_ID     INT          PRIMARY KEY,
  EMPLOYEE_ROLE_NAME   VARCHAR(128) NOT NULL
)
go

CREATE TABLE PROPERTY_LIST_TEMPLATES(
  LIST_TEMPLATE_ID     INT           IDENTITY(1,1) PRIMARY KEY,
  LIST_TEMPLATE_TYPE   CHAR(1)       NOT NULL CHECK (LIST_TEMPLATE_TYPE in ('D','C','S','A')),
  LIST_TEMPLATE_CODE   VARCHAR(128)  NOT NULL
)
go
CREATE TABLE PROPERTY_TEMPLATES(
  TEMPLATE_ID         INT IDENTITY(1,1) PRIMARY KEY,
  LIST_TEMPLATE_ID    INT NOT NULL  REFERENCES PROPERTY_LIST_TEMPLATES(LIST_TEMPLATE_ID),
  TEMPLATE_CODE       VARCHAR(128)  NOT NULL,
  TEMPLATE_DESC       VARCHAR(2048) NOT NULL,
  IS_OPTIONAL         CHAR(1)       NOT NULL
)
go

CREATE TABLE PROPERTY_LISTS(
  LIST_ID                  INT IDENTITY(1,1) PRIMARY KEY,
  LIST_TEMPLATE_ID         INT NOT NULL      REFERENCES PROPERTY_LIST_TEMPLATES(LIST_TEMPLATE_ID),
  LIST_CODE                VARCHAR(512) NOT NULL UNIQUE
)
go

CREATE TABLE PROPERTY_VALUES(
  VALUE_ID          INT IDENTITY(1,1) PRIMARY KEY,
  LIST_ID           INT NOT NULL REFERENCES PROPERTY_LISTS(LIST_ID),
  TEMPLATE_ID       INT NOT NULL REFERENCES PROPERTY_TEMPLATES(TEMPLATE_ID),
  VALUE             VARCHAR(1024) NULL,
  UNIQUE(LIST_ID,TEMPLATE_ID)
)
go

CREATE TABLE SYNC_FOLDERS(
  SYNC_FOLDER_ID   INT IDENTITY(1,1)           ,
  SYNC_FOLDER_CODE VARCHAR(128)  NOT NULL UNIQUE,
  SYNC_FOLDER_DESC VARCHAR(2048) NOT NULL,
  START_ORDER      INT           NOT NULL
)
go
CREATE TABLE SYNC_SERVICES(
  SYNC_SERVICE_ID           INT IDENTITY(1,1) PRIMARY KEY                                  ,
  SYNC_FOLDER_CODE          VARCHAR(128) NOT NULL REFERENCES SYNC_FOLDERS(SYNC_FOLDER_CODE),
  SERVLET_PATH              VARCHAR(256)     NULL                                          ,
  BEAN_CODE                 VARCHAR(256) NOT NULL UNIQUE                                   ,
  BEAN_CLASS                VARCHAR(256)                                                   ,
  PARENT_BEAN_CODE          VARCHAR(256)     NULL                                          ,
  PARENT_BEAN_PROPERTY      VARCHAR(256)     NULL                                          ,
  START_ORDER               INT          NOT NULL                                          ,
  BEAN_DESC                 VARCHAR(2048)    NULL
)
go

CREATE PROCEDURE SP_SYNC_LIST_FOLDERS
AS
  SELECT SYNC_FOLDER_CODE, START_ORDER, SYNC_FOLDER_DESC
  FROM SYNC_FOLDERS
  ORDER BY START_ORDER
GO

CREATE PROCEDURE SP_SYNC_LIST_SERVICES
   @sync_folder_code varchar(256)
AS
  SELECT SYNC_SERVICE_ID,BEAN_CODE,BEAN_CLASS,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, SERVLET_PATH as PUBLIC_SERVLET_PATH, BEAN_DESC
  FROM SYNC_SERVICES
  WHERE SYNC_FOLDER_CODE=@sync_folder_code
  ORDER BY START_ORDER
GO
--CREATE PROCEDURE SP_SYNC_LIST_SERVICES_FOR_DISPATCHER
--   @sync_folder_code varchar(256)
--AS
--  SELECT s.SYNC_SERVICE_ID,s.BEAN_CODE,SERVLET_PATH, f.SYNC_FOLDER_CODE
--  FROM SYNC_SERVICES s join SYNC_FOLDERS f on f.SYNC_FOLDER_CODE=s.SYNC_FOLDER_CODE
--  WHERE SERVLET_PATH IS NOT NULL and s.SYNC_FOLDER_CODE=@sync_folder_code
--O

--CREATE PROCEDURE SP_SYNC_LIST_TEMPLATES
--AS
--  SELECT LIST_TEMPLATE_ID,
--       case when LIST_TEMPLATE_TYPE='C' then 'For Client'
--            when LIST_TEMPLATE_TYPE='S' then 'For Service'
--            when LIST_TEMPLATE_TYPE='A' then 'For Admin GUI'
--       end LIST_TEMPLATE_TYPE,LIST_TEMPLATE_CODE
--  FROM PROPERTY_LIST_TEMPLATES
--  ORDER BY 3
--O

CREATE PROCEDURE SP_SYNC_COMPOSE_PROPERTY_LIST_CODE
  @sync_folder_code varchar(256),
  @bean_code        varchar(256),
  @list_code              varchar(256) out
as
  set @list_code=@sync_folder_code+'/'+@bean_code
GO

CREATE PROCEDURE SP_SYNC_LIST_PROPERTIES
  @sync_folder_code varchar(256),
  @bean_code        varchar(256)
as
  declare @list_code varchar(1024)
  exec SP_SYNC_COMPOSE_PROPERTY_LIST_CODE @sync_folder_code,@bean_code,@list_code out
  declare @list_id int
  select @list_id=list_id from PROPERTY_LISTS where LIST_CODE=@list_code

  select v.VALUE_ID, t.TEMPLATE_CODE, v.VALUE, t.TEMPLATE_DESC
  from PROPERTY_VALUES v join PROPERTY_TEMPLATES t on t.TEMPLATE_ID=v.TEMPLATE_ID
  where LIST_ID=@list_id
  order by 1
GO


CREATE PROCEDURE SP_SYNC_SET_PROPERTY
  @sync_folder_code varchar(256) ,
  @bean_code        varchar(256) ,
  @property_code    varchar(256) ,
  @value            varchar(1024)
as
  --1. Compose key, list id and template id
  declare @list_code varchar(1024)
  exec SP_SYNC_COMPOSE_PROPERTY_LIST_CODE @sync_folder_code,@bean_code,@list_code out

  declare @list_id int
  declare @list_template_id int
  select @list_id=list_id, @list_template_id = LIST_TEMPLATE_ID from PROPERTY_LISTS where LIST_CODE=@list_code

  declare @template_id int
  select @template_id=template_id from PROPERTY_TEMPLATES
  where LIST_TEMPLATE_ID=@list_template_id and TEMPLATE_CODE=@property_code

  --2. Deleting and inserting
  begin tran
    delete from PROPERTY_VALUES where LIST_ID=@list_id and TEMPLATE_ID=@template_id
    insert into PROPERTY_VALUES(LIST_ID , TEMPLATE_ID,VALUE)
    select                      @list_id, @template_id,@value
  commit
GO


CREATE PROCEDURE SP_SYNC_RESET_PROPERTIES
  @sync_folder_code varchar(256),
  @bean_code        varchar(256)
AS
  --1. Compose key
  declare @bean_class varchar(256)
  declare @list_code  varchar(1024)
  exec SP_SYNC_COMPOSE_PROPERTY_LIST_CODE @sync_folder_code,@bean_code,@list_code out
  select @bean_class = bean_class from SYNC_SERVICES where BEAN_CODE=@bean_code

  --2. Delete
  delete from PROPERTY_VALUES where LIST_ID in (select LIST_ID from PROPERTY_LISTS where LIST_CODE=@list_code)
  delete from PROPERTY_LISTS where LIST_CODE=@list_code

  --3. Insert
  declare @list_template_id int
  select @list_template_id=list_template_id from PROPERTY_LIST_TEMPLATES where LIST_TEMPLATE_CODE=@bean_class
  if @list_template_id is null return --No properties for this bean class found
  declare @list_id int
  insert into PROPERTY_LISTS(LIST_CODE ,LIST_TEMPLATE_ID)
  select                     @list_code,@list_template_id
  select @list_id=@@IDENTITY

  insert into PROPERTY_VALUES(LIST_ID , TEMPLATE_ID,VALUE)
  select                      @list_id, TEMPLATE_ID,case when IS_OPTIONAL='Y' then null else '' end
  from PROPERTY_TEMPLATES
  where LIST_TEMPLATE_ID=@list_template_id
GO

INSERT INTO [EMPLOYEE_ROLES] VALUES(1,'Administrator')
INSERT INTO [EMPLOYEES] ([EMPLOYEE_ROLE_ID],[EMPLOYEE_EMAIL], [EMPLOYEE_NAME], [leave_date], [EMPLOYEE_PASSWORD],[PASSWORD_CHANGED_DATE]) VALUES (1,'admin', 'admin', null, 'E10ADC3949BA59ABBE56E057F20F883E',getdate())
GO

if object_id('SP_SYNC_ADD_TEMPLATE') is not null DROP PROCEDURE SP_SYNC_ADD_TEMPLATE
GO
CREATE PROCEDURE SP_SYNC_ADD_TEMPLATE
   @template_code VARCHAR(128)
as
  set nocount on
  insert into PROPERTY_LIST_TEMPLATES (LIST_TEMPLATE_TYPE, LIST_TEMPLATE_CODE)
  select                               'S'               , @template_code    
GO
if object_id('SP_SYNC_ADD_TEMPLATE2') is not null DROP PROCEDURE SP_SYNC_ADD_TEMPLATE2
GO
CREATE PROCEDURE SP_SYNC_ADD_TEMPLATE2
   @id            int out,
   @template_code VARCHAR(128)
as
  set nocount on
  insert into PROPERTY_LIST_TEMPLATES (LIST_TEMPLATE_TYPE, LIST_TEMPLATE_CODE)
  select                               'S'               , @template_code    
  set @id=@@identity
GO
if object_id('SP_SYNC_ADD_TEMPLATE_PROPERTY') is not null DROP PROCEDURE SP_SYNC_ADD_TEMPLATE_PROPERTY
GO
CREATE PROCEDURE SP_SYNC_ADD_TEMPLATE_PROPERTY
   @template_code VARCHAR(128) ,
   @property_code VARCHAR(128) ,
   @property_desc VARCHAR(2048),
   @is_optional   CHAR(1)     
as
  --1. Finding template id
  set nocount on
  declare @list_template_id int
  select @list_template_id=LIST_TEMPLATE_ID from PROPERTY_LIST_TEMPLATES where LIST_TEMPLATE_CODE=@template_code

  --2. Inserting
  insert into PROPERTY_TEMPLATES(LIST_TEMPLATE_ID , TEMPLATE_CODE , TEMPLATE_DESC , IS_OPTIONAL  )
  values(                        @list_template_id, @property_code, @property_desc, @is_optional)
GO
if object_id('SP_SYNC_ADD_TEMPLATE_PROPERTY2') is not null DROP PROCEDURE SP_SYNC_ADD_TEMPLATE_PROPERTY2
GO
CREATE PROCEDURE SP_SYNC_ADD_TEMPLATE_PROPERTY2
   @list_template_id int         ,
   @property_code    VARCHAR(128),
   @is_optional      CHAR(1)     ,
   @property_desc    VARCHAR(2048)   
as
  insert into PROPERTY_TEMPLATES(LIST_TEMPLATE_ID , TEMPLATE_CODE , TEMPLATE_DESC , IS_OPTIONAL)
  values(                        @list_template_id, @property_code, @property_desc, @is_optional)
GO


if object_id('SP_SYNC_ADD_TEMPLATE') is not null DROP PROCEDURE SP_SYNC_ADD_TEMPLATE
GO
CREATE PROCEDURE SP_SYNC_ADD_TEMPLATE
   @template_code VARCHAR(128)
as
  set nocount on
  insert into PROPERTY_LIST_TEMPLATES (LIST_TEMPLATE_TYPE, LIST_TEMPLATE_CODE )
  select                               'S'               , @template_code
GO
if object_id('SP_SYNC_ADD_TEMPLATE_PROPERTY') is not null DROP PROCEDURE SP_SYNC_ADD_TEMPLATE_PROPERTY
GO
CREATE PROCEDURE SP_SYNC_ADD_TEMPLATE_PROPERTY
   @template_code VARCHAR(128) ,
   @property_code VARCHAR(128) ,
   @is_optional   CHAR(1)      
as
  --1. Finding template id
  set nocount on
  declare @list_template_id int
  select @list_template_id=LIST_TEMPLATE_ID from PROPERTY_LIST_TEMPLATES where LIST_TEMPLATE_CODE=@template_code

  --2. Inserting
  insert into PROPERTY_TEMPLATES(LIST_TEMPLATE_ID , TEMPLATE_CODE , IS_OPTIONAL )
  values(                        @list_template_id, @property_code, @is_optional)
GO


IF OBJECT_ID('READONLY_CONFIG') IS NOT NULL DROP TABLE READONLY_CONFIG
GO
CREATE TABLE READONLY_CONFIG(
   PROPERTY_KEY    VARCHAR(256),
   PROPERTY_VALUE  VARCHAR(1024)
)
GO
INSERT INTO READONLY_CONFIG VALUES('VERSION','${info.entry.revision} @ November 24 2015 1653')
GO


/****** Object:  Table [dbo].[SYNC_LOGS]    Script Date: 03/05/2014 16:14:50 ******/
if object_id('SYNC_LOGS') is not null DROP TABLE SYNC_LOGS
GO
CREATE TABLE [dbo].[SYNC_LOGS](
	[EVENT_ID]              int identity(1,1) primary key ,
	[SERVER_EVENT_ID]       [varchar](50) NOT NULL,
	[EVENT_TIME]            [datetime]      NOT NULL,
	[USER_EMAIL]            [varchar](50)       NULL,
	[EVENT_TYPE]            [varchar](50)   NOT NULL,
	[START_SERVER_EVENT_ID] int                 NULL,
	[EVENT_DESC]            [nvarchar](max)     NULL,
	[CLIENT_IP_ADDRESS]     [varchar](20)       NULL,
	[WEB_HOST_NAME]         [varchar](100)      NULL,
	[WEB_APP_NAME]          [varchar](100)      NULL,
	[DISTRIB_SERVER]        [nvarchar](200)     NULL,
	[EVENT_INFO]            [nvarchar](max)     NULL,
	[ERROR_STACK_TRACE]     [nvarchar](max)     NULL,
	[CLIENT_EVENT_ID]       [varchar](50)       NULL,
	[CLIENT_DEVICE_ID]      [varchar](100)      NULL
)
GO

if object_id('SP_SYNC_STORE_LOGMSG') is not null DROP PROCEDURE SP_SYNC_STORE_LOGMSG
go

CREATE PROCEDURE SP_SYNC_STORE_LOGMSG
  @server_event_id varchar(50) ,
  @user_email varchar(50) ,
  @client_event_id varchar(50),
  @client_device_id varchar(100),
  @event_type varchar(50),
  @start_server_event_id varchar(50),
  @event_desc nvarchar(max),
  @client_ip_address varchar(20),
  @web_host_name varchar(100),
  @web_app_name varchar(100),
  @distrib_server nvarchar(200),
  @event_info nvarchar(max),
  @error_stack_trace nvarchar(max)
as
  begin tran
    set nocount on
    INSERT INTO [dbo].[SYNC_LOGS]
           ([SERVER_EVENT_ID]
           ,[EVENT_TIME]
           ,[USER_EMAIL]
           ,[CLIENT_DEVICE_ID]
           ,[EVENT_TYPE]
           ,[START_SERVER_EVENT_ID]
           ,[EVENT_DESC]
           ,[CLIENT_IP_ADDRESS]
           ,[WEB_HOST_NAME]
           ,[WEB_APP_NAME]
           ,[DISTRIB_SERVER]
           ,[EVENT_INFO]
           ,[ERROR_STACK_TRACE]
           ,[CLIENT_EVENT_ID])
     VALUES
           (@server_event_id
           ,GETDATE()
           ,@user_email
           ,@client_device_id
           ,@event_type
           ,@start_server_event_id
           ,@event_desc
           ,@client_ip_address
           ,@web_host_name
           ,@web_app_name
           ,@distrib_server
           ,@event_info
           ,@error_stack_trace
           ,@client_event_id)
  commit
 GO

if object_id('SP_SYNC_STORE_LOGMSG_WITH_ID') is not null DROP PROCEDURE SP_SYNC_STORE_LOGMSG_WITH_ID
go

CREATE PROCEDURE SP_SYNC_STORE_LOGMSG_WITH_ID
  @server_event_id varchar(50) ,
  @user_email varchar(50) ,
  @client_event_id varchar(50),
  @client_device_id varchar(100),
  @event_type varchar(50),
  @start_server_event_id varchar(50),
  @event_desc nvarchar(max),
  @client_ip_address varchar(20),
  @web_host_name varchar(100),
  @web_app_name varchar(100),
  @distrib_server nvarchar(200),
  @event_info nvarchar(max),
  @error_stack_trace nvarchar(max)
as
  begin tran
    set nocount on
    INSERT INTO [dbo].[SYNC_LOGS]
           ([SERVER_EVENT_ID]
           ,[EVENT_TIME]
           ,[USER_EMAIL]
           ,[CLIENT_DEVICE_ID]
           ,[EVENT_TYPE]
           ,[START_SERVER_EVENT_ID]
           ,[EVENT_DESC]
           ,[CLIENT_IP_ADDRESS]
           ,[WEB_HOST_NAME]
           ,[WEB_APP_NAME]
           ,[DISTRIB_SERVER]
           ,[EVENT_INFO]
           ,[ERROR_STACK_TRACE]
           ,[CLIENT_EVENT_ID])
     VALUES
           (@server_event_id
           ,GETDATE()
           ,@user_email
           ,@client_device_id
           ,@event_type
           ,@start_server_event_id
           ,@event_desc
           ,@client_ip_address
           ,@web_host_name
           ,@web_app_name
           ,@distrib_server
           ,@event_info
           ,@error_stack_trace
           ,@client_event_id)
     select @@IDENTITY
  commit
 GO
if object_id('SYNC_AUDIT') is not null DROP TABLE SYNC_AUDIT
go

CREATE TABLE SYNC_AUDIT(
  EVENT_ID INT IDENTITY(1,1) PRIMARY KEY,
  EVENT_TIME DATETIME NOT NULL,
  IP_ADDRESS VARCHAR(50) NULL,
  USER_EMAIL VARCHAR(50) NULL,
  MODULE VARCHAR(100) NULL,
  EVENT_TYPE NVARCHAR(50) NULL,
  EVENT_DESC NVARCHAR(MAX) NULL,
  CODE INT NULL
)
GO

if object_id('SP_SYNC_AUDIT_LOGMSG') is not null DROP PROCEDURE SP_SYNC_AUDIT_LOGMSG
go

CREATE PROCEDURE SP_SYNC_AUDIT_LOGMSG
  @ip_address varchar(50) ,
  @user_email varchar(50),
  @module varchar(100),
  @event_type nvarchar(50),
  @event_desc nvarchar(max),
  @code int
as
  begin tran
    set nocount on
    INSERT INTO SYNC_AUDIT
           (EVENT_TIME
		   ,IP_ADDRESS
           ,USER_EMAIL
           ,MODULE
           ,EVENT_TYPE
           ,EVENT_DESC
           ,CODE)
     VALUES
           (GETDATE()
		   ,@ip_address
           ,@user_email
           ,@module
           ,@event_type
           ,@event_desc
		   ,@code)
  commit
 GO
--1. Deleting old, it simplifies restart
DELETE FROM SYNC_SERVICES
DELETE FROM SYNC_FOLDERS
go

--2. Filling templates for services
--2.1. For MSSQLService
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,  'ru.sberbank.syncserver2.service.sql.MSSQLService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'mssqlURL'     ,'N', 'JDBC Url to source database'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'mssqlUser'    ,'N', 'MSSQL User to source database'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'mssqlPassword','N', 'MSSQL Password to source database'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'serviceName'  ,'N', 'This parameter used for distribution request between several services according to the content of tag "service" in online request'
go

--2.2. For SQLTemplateLoader
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.sql.SQLTemplateLoader'
go

--2.3. For SQLDispatcherService
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.sql.SQLDispatcherService'
go

--2.4. For SQLService
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.sql.SQLPublicService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'conversion'  ,'N', 'This parameter used to provide compatibility of DataPower'
go

--3. Filling list of folders
set identity_insert SYNC_FOLDERS on
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(1,'proxy'   , 1,'This folder contains all services but admin services')
set identity_insert SYNC_FOLDERS off

--4. Filing list of services
set identity_insert SYNC_SERVICES on                                          
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY         ,START_ORDER,SERVLET_PATH , BEAN_DESC)
values(                   1             ,'proxy'       ,'proxyMSSQLService1'     ,'ru.sberbank.syncserver2.service.sql.MSSQLService'        ,'proxyDispatcherService' ,'subService'                 ,1          , null        ,'This service sends SQL to the database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY,START_ORDER         ,SERVLET_PATH , BEAN_DESC)
values(                   2             ,'proxy'       ,'proxySQLTemplateLoader1','ru.sberbank.syncserver2.service.sql.SQLTemplateLoader'   ,'proxyMSSQLService1'     ,'templateLoader'             ,2          , null        ,'This service loads templates from table SQL_TEMPLATES to memory')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY         ,START_ORDER,SERVLET_PATH , BEAN_DESC)
values(                   3             ,'proxy'       ,'proxyMSSQLService2'     ,'ru.sberbank.syncserver2.service.sql.MSSQLService'        ,'proxyDispatcherService' ,'subService'                 ,3          , null        ,'This service sends SQL to the database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY,START_ORDER         ,SERVLET_PATH , BEAN_DESC)
values(                   4             ,'proxy'       ,'proxySQLTemplateLoader2','ru.sberbank.syncserver2.service.sql.SQLTemplateLoader'   ,'proxyMSSQLService2'     ,'templateLoader'             ,4          , null        ,'This service loads templates from table SQL_TEMPLATES to memory')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY         ,START_ORDER,SERVLET_PATH , BEAN_DESC)
values(                   7             ,'proxy'       ,'proxyMSSQLService3'     ,'ru.sberbank.syncserver2.service.sql.MSSQLService'        ,'proxyDispatcherService' ,'subService'                 ,7          , null        ,'This service sends SQL to the database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY,START_ORDER         ,SERVLET_PATH , BEAN_DESC)
values(                   8             ,'proxy'       ,'proxySQLTemplateLoader3','ru.sberbank.syncserver2.service.sql.SQLTemplateLoader'   ,'proxyMSSQLService3'     ,'templateLoader'             ,8          , null        ,'This service loads templates from table SQL_TEMPLATES to memory')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY         ,START_ORDER,SERVLET_PATH , BEAN_DESC)
values(                   9             ,'proxy'       ,'proxyMSSQLService4'     ,'ru.sberbank.syncserver2.service.sql.MSSQLService'        ,'proxyDispatcherService' ,'subService'                 ,9          , null        ,'This service sends SQL to the database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY,START_ORDER         ,SERVLET_PATH , BEAN_DESC)
values(                   10             ,'proxy'       ,'proxySQLTemplateLoader4','ru.sberbank.syncserver2.service.sql.SQLTemplateLoader'   ,'proxyMSSQLService4'     ,'templateLoader'             ,10          , null        ,'This service loads templates from table SQL_TEMPLATES to memory')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             , BEAN_CLASS                                               ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY         ,START_ORDER,SERVLET_PATH , BEAN_DESC)
values(                   5             ,'proxy'       ,'proxyDispatcherService' ,'ru.sberbank.syncserver2.service.sql.SQLDispatcherService','proxySQLService'        ,'sqlDispatcherService'       ,5          , null        , 'This service accepts requests from proxySQLService and dispatch them to proxyMSSQLService1 and proxyMSSQLService2')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE             ,BEAN_CLASS                                                ,PARENT_BEAN_CODE         ,PARENT_BEAN_PROPERTY         ,START_ORDER,SERVLET_PATH , BEAN_DESC)
values(                   6             ,'proxy'       ,'proxySQLService'        ,'ru.sberbank.syncserver2.service.sql.SQLPublicService'          ,null                     ,null                         ,6          , 'online.sql', 'This service accepts requests from DataPower')
go
set identity_insert SYNC_SERVICES off
go


--4. Filling properties
--4.1. For misMSSQLService
exec SP_SYNC_RESET_PROPERTIES 'proxy','proxyMSSQLService1'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','mssqlURL'     ,'@MSSQL_URL1@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','mssqlUser'    ,'@MSSQL_USER1@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','mssqlPassword','@MSSQL_PASSWORD1@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService1','serviceName'  ,'@DATAPOWER_TARGET1@'
go
exec SP_SYNC_RESET_PROPERTIES 'proxy','proxyMSSQLService2'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','mssqlURL'     ,'@MSSQL_URL2@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','mssqlUser'    ,'@MSSQL_USER2@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','mssqlPassword','@MSSQL_PASSWORD2@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService2','serviceName'  ,'@DATAPOWER_TARGET2@'
go
exec SP_SYNC_RESET_PROPERTIES 'proxy','proxyMSSQLService3'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','mssqlURL'     ,'@MSSQL_URL3@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','mssqlUser'    ,'@MSSQL_USER3@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','mssqlPassword','@MSSQL_PASSWORD3@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService3','serviceName'  ,'@DATAPOWER_TARGET3@'
go

exec SP_SYNC_RESET_PROPERTIES 'proxy','proxyMSSQLService4'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','mssqlURL'     ,'@MSSQL_URL4@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','mssqlUser'    ,'@MSSQL_USER4@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','mssqlPassword','@MSSQL_PASSWORD4@'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxyMSSQLService4','serviceName'  ,'@DATAPOWER_TARGET4@'
go

exec SP_SYNC_RESET_PROPERTIES 'proxy','proxySQLService'
exec SP_SYNC_SET_PROPERTY 'proxy'    ,'proxySQLService','conversion'  ,'@DATAPOWER_CONVERSION@'
go
IF OBJECT_ID('SQL_TEMPLATES_SERVER') is not null drop table SQL_TEMPLATES_SERVER
GO
CREATE TABLE SQL_TEMPLATES_SERVER(
  TEMPLATE_CODE varchar(900) primary key,
  TEMPLATE_SQL  varchar(max)  null,
  TEMPLATE_DESC  varchar(500)  null,
)
GO 

IF OBJECT_ID('SQL_TEMPLATES_MOBILE') is null
CREATE TABLE SQL_TEMPLATES_MOBILE(
  TEMPLATE_CODE varchar(900) primary key,
  TEMPLATE_SQL  varchar(max)  null,
  APP_CODE  varchar(50)  null
)

GO

IF OBJECT_ID('SQL_TEMPLATES') is not null drop view SQL_TEMPLATES
GO

IF OBJECT_ID('SQL_TEMPLATES') is not null drop view SQL_TEMPLATES
GO

CREATE VIEW SQL_TEMPLATES AS 
select TEMPLATE_CODE,TEMPLATE_SQL from SQL_TEMPLATES_SERVER
union all
select TEMPLATE_CODE,TEMPLATE_SQL from SQL_TEMPLATES_MOBILE
GO


GO
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('SYNCSERVER.IS_ALLOWED_TO_USE_APP','exec @ALPHA_SOURCE_DB@.dbo.SP_IS_ALLOWED_TO_USE_APP ? , ?, ?','This template does not write anything. Only checks that user email has access to application.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('SYNCSERVER.IS_ALLOWED_TO_DOWNLOAD_FILE','exec @ALPHA_SOURCE_DB@.dbo.SP_IS_ALLOWED_TO_DOWNLOAD_FILE ? ,? ,?, ?','This template does not write anything. Only checks that user email has access to download file.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('SYNCSERVER.LOG_SQL','exec @ALPHA_GENERATOR_DB@.dbo.SP_SYNC_STORE_LOGMSG_WITH_ID ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?','This template write a record to SYNC_LOGS table.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('SYNCSERVER.DP_NOT_DEL_FILE_MOV_NOTIFICATION','exec @ALPHA_MONITOR_DB@..SP_DEL_FILE_MOV_NOTIFICATION ?,?,?','This template deletes record about not delivered file ( table NOTIFICATION in monitor DB) when file succesfully delivered to SIGMA.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('SYNCSERVER.DP_NOT_ADD_ERROR','exec @ALPHA_MONITOR_DB@..SP_ADD_ERROR ?,?,?','This template add record about error situation (table NOTIFICATIONS in monitor db)')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('SYNCSERVER.DP_NOT_PING','exec @ALPHA_MONITOR_DB@..SP_PING ?,?','This template updates record in (table NOTIFICATION_SERVERS in monitor DB) - sets last ping datetime from server.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('SYNCSERVER.SINGLE_FILES_REQUEST_STATUS','exec @ALPHA_GENERATOR_DB@..SP_GET_FILE_STATUSES','This template does not write anything. It selects records from SYNC_CACHE_STATIC_FILES table')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('SYNCSERVER.GENERATOR_USER_GROUP_DRAFT','exec @ALPHA_GENERATOR_DB@..SP_GET_GENERATOR_USER_GROUPS_FOR_DRAFT','This template does not write anything. It selects records from GENERATOR_GROUP_USERS table')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('SYNCSERVER.ADD_GENERATOR_STATE','exec @ALPHA_GENERATOR_DB@..SP_SYNC_STATIC_FILES_GEN_ADD_STATE ?,?,?,?,?,?','This templates adds notification signal to alpha about file is succesfully or not succesfully loaded to sigma.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('MIS_BASE2.UPDATE_USER','exec @ALPHA_SOURCE_DB@.dbo.SP_MIS_BASE2_UPDATE_USER ?,?,?,?,?,?,?,?,?,?','This template insert/update user in mis_base2 structure.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('MIS_BASE2.UPDATE_USER_ROLES','exec @ALPHA_SOURCE_DB@.dbo.SP_MIS_BASE2_UPDATE_USER_ROLES ?,?,?','This templates delete/added roles to user in mis_base2 structure.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('MIS_BASE2.GET_USER_ROLES','exec @ALPHA_SOURCE_DB@.dbo.SP_MIS_BASE2_GET_USER_ROLES ?','This templates returns user role list in mis_base2 structure.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('MIS_BASE2.GET_INAV_ROLES','exec @ALPHA_SOURCE_DB@.dbo.SP_MIS_BASE2_GET_INAV_ROLES','This template returns all navigator roles in mis_base2 structure.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('MIS_BASE2.GET_USERS_LIST','exec @ALPHA_SOURCE_DB@.dbo.SP_MIS_BASE2_GET_USERS_LIST ?,?,?,?','This templates search users in mis_base2 structure.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('MIS_BASE2.GET_USER_INFO','exec @ALPHA_SOURCE_DB@.dbo.SP_MIS_BASE2_GET_USER_INFO ?','This template select user info in mis_base2 structure.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('MIS_BASE2.GET_DICTIONARY_TERRBANKS','exec @ALPHA_SOURCE_DB@.dbo.SP_MIS_BASE2_GET_TERRBANKS','This template return dictionary terrbanks in mis_base2 structure.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('MIS_BASE2.GET_DICTIONARY_UNITS','exec @ALPHA_SOURCE_DB@.dbo.SP_MIS_BASE2_GET_UNITS','This template return dictionary units in mis_base2 structure.')
INSERT INTO SQL_TEMPLATES_SERVER (TEMPLATE_CODE,TEMPLATE_SQL,TEMPLATE_DESC) VALUES ('MIS_BASE2.GET_DICTIONARY_BLOCKS','exec @ALPHA_SOURCE_DB@.dbo.SP_MIS_BASE2_GET_BLOCKS','This template return dictionary blocks in mis_base2 structure.')
GO

IF OBJECT_ID('SUBST_DICT') is not null drop table SUBST_DICT
GO
CREATE TABLE SUBST_DICT (
  SUBST_CODE varchar(50) primary key,
  SUBST_VALUE varchar(MAX)  null,
  SUBST_DESC varchar(50) null,
  APP_CODE varchar(50)  null
)
GO 
set identity_insert SYNC_FOLDERS on
declare @id int 
select @id = max(SYNC_FOLDER_ID) from SYNC_FOLDERS
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(@id+1 ,'adminconsole', @id+1,'This folder contains services for adminconsole application')
set identity_insert SYNC_FOLDERS off
go

set identity_insert SYNC_SERVICES on
declare @id int 
select @id = max(SYNC_SERVICE_ID) from SYNC_SERVICES
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE,BEAN_CLASS,PARENT_BEAN_CODE ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(   @id+1   ,'adminconsole' ,'adminConsoleService','ru.sberbank.syncserver2.service.pub.AdminConsoleService' ,null    ,null        , @id+1   , 'request.do', 'This is xml service for adminconsole')
set identity_insert SYNC_SERVICES off
go

ALTER TABLE EMPLOYEES  ADD IS_REMOTE BIT NOT NULL  DEFAULT 0
ALTER TABLE EMPLOYEES  ADD IS_READ_ONLY BIT NOT NULL DEFAULT 0
go