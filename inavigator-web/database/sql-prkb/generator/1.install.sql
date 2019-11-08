go
use GENERATOR;
go
set nocount on
go
IF OBJECT_ID('SYNC_CONFIG') IS NOT NULL DROP TABLE SYNC_CONFIG
GO
CREATE TABLE SYNC_CONFIG(
   PROPERTY_KEY    VARCHAR(256),
   PROPERTY_VALUE  VARCHAR(1024),
   PROPERTY_DESC   VARCHAR(1024)
)
GO
SET NOCOUNT ON
INSERT INTO SYNC_CONFIG VALUES('ROOT_FOLDER'                        ,'C:/Disk_E/mis_file/cacheUAT','Root folder for local disk cache')
INSERT INTO SYNC_CONFIG VALUES('NETWORK_TEMP_FOLDER'                ,'\\\\bronze2\\vol1\\i-navigator\\IN\\uat4\\temp','Root folder at file transporter for temporary files')
INSERT INTO SYNC_CONFIG VALUES('NETWORK_ROOT_FOLDER'                ,'\\\\bronze2\\vol1\\i-navigator\\OUT\\uat4','Root folder at file transporter for files to be transferred to Sigma')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_URL1'                         ,'jdbc:sqlserver://finik1:1433;databaseName=MIS_IPAD','Data Source for generator #1')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_USER1'                        ,'mis_user','JDBC User')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_PASSWORD1'                    ,'mis','JDBC Password')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_URL2'                         ,'jdbc:sqlserver://finik2:1433;databaseName=mis_rubricator','Data Source for generator #1')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_USER2'                        ,'mis_user','JDBC User')
INSERT INTO SYNC_CONFIG VALUES('MSSQL_PASSWORD2'                    ,'mis','JDBC Password')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_DB'                   ,'MIS_IPAD_MONITOR2','Database Name used by Alpha Monitor. It should be at same MSSQL Server as used by MONITOR_DB datasource in WebSphere')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SOURCE_DB'                    ,'MIS_IPAD2','Source Database Name at finik1 or finik2. Usually it should be equal MIS_IPAD or MIS_IPAD2')
INSERT INTO SYNC_CONFIG VALUES('LOGGING_SERVICE'                    ,'SynchronousDbLogService','SynchronousDbLogService means synchronousn logging. DbLogService means asynchronous logging. Other values are not supported')
INSERT INTO SYNC_CONFIG VALUES('FILE_FRAGMENT_SIZE_MB'              ,'1','Max file fragment size for offline files.')
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
INSERT INTO READONLY_CONFIG VALUES('VERSION','${info.entry.revision} @ November 24 2015 1652')
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
---0. Addomg additional role to employee
INSERT INTO [EMPLOYEE_ROLES] VALUES(2,'Operator')
INSERT INTO [EMPLOYEES] ([EMPLOYEE_ROLE_ID],[EMPLOYEE_EMAIL], [EMPLOYEE_NAME], [leave_date], [EMPLOYEE_PASSWORD],[PASSWORD_CHANGED_DATE]) VALUES (2,'operator', 'operator', null, 'E10ADC3949BA59ABBE56E057F20F883E',getdate())
GO

--1. Deleting old, it simplifies restart
DELETE FROM SYNC_SERVICES
DELETE FROM SYNC_FOLDERS
go

--2. Filling list of folders
set identity_insert SYNC_FOLDERS on
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(1,'common'   , 1,'This folder contains common services used by other folders')
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(2,'single'   , 2,'This folder contains services required to generate single files')
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(4,'public', 4,'This folder contains services for public access form other applications')
set identity_insert SYNC_FOLDERS off

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.transport.LocalDeflater'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localSourceFolder','N','After file appears in localSourceFolder the service moves it to localTempFolder1. '
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder1' ,'N','After file appears in localTempFolder1 the service starts deflating file to a file with same name in localTempFolder2'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder2' ,'N','localTempFolder2 used to store deflated files while the deflation. After deflation finished the file is copied to localDestFolder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localDestFolder'  ,'N','The service moves to localDestFolder completed inflated file'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'maxFragmentFileSizeMb'  ,'N','Max fragment file size from offline files.'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.generator.single.SingleGeneratorService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'threadCount'         ,'N', 'Maximum number of files generated at same time'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'configHome'          ,'N', 'Path to folder with ETL files'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localFileHome'       ,'N', 'This folder contains subfolders with temporary and generated files'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localArchiveFolder'  ,'N', 'The link to network mover''s localArchiveFolder. Used to  repeat file copy.'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'networkTempFolder'   ,'N', 'The link to network mover''s networkTempFolder. Used to repeat file copy.'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'networkTargetFolder'  ,'N', 'The link to network mover''s networkTargetFolder. Used to repeat file copy.'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'logsStorageTimes'  ,'N', 'The maximum number of log generation to store.'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'databaseName','N','Alpha Monitor database name'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.file.cache.list.DatabaseFileLister'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.file.transport.AlphaNetworkFileMover'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localSourceFolder'    ,'N','The source folder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localTempFolder'      ,'N','When file appears in source folder it is copied to localTempFolder and only then it is copied to a file transporter'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'networkTempFolder'    ,'N','Service moves files to the folder at file transporter defined by networkTempFolder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'networkTargetFolder'  ,'N','Service renames files to networkTargetFolder from networkTempFolder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localArchiveFolder'   ,'N','After file is ready for move to file transporter we keep the copy for repetition'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'staticSharedHosts'    ,'N','Static list of hosts the file should be delivered to'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'sharedHostsListerCode','N','The link to dynamic lister that provides list of hosts the file should be delivered to'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.core.event.impl.AlphaEventHandler'
go


--3. Filing list of services
set identity_insert SYNC_SERVICES on
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE               ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   1              ,'common'        , 'databaseLogger'       ,'ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger'            ,null            ,null                ,1          , 'This service logs notifications to Alpha Monitor database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE               ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   2              ,'common'        , 'clusterManager'       ,'ru.sberbank.syncserver2.service.generator.ClusterManager'            ,null            ,null                ,2          , 'This service helps other services to understand whether this node of cluster is active node or passive node')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE               ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   3              ,'single'        , 'singleGenerator'      ,'ru.sberbank.syncserver2.service.generator.single.SingleGeneratorService'       ,null            ,null                ,3          , 'This service generates single files')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE, BEAN_CODE              ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   4              ,'single'        , 'singleLocalDeflater'  ,'ru.sberbank.syncserver2.service.file.transport.LocalDeflater'                  ,null            ,null                ,4          , 'This service deflates generated files')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE, BEAN_CODE              ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   5              ,'single'        , 'singleFileLister'     ,'ru.sberbank.syncserver2.service.file.cache.list.DatabaseFileLister'            ,null            ,null                ,5          , 'This service used to prepare notifications for not delivered files. It loads list of hosts the files should be delivered to')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE, BEAN_CODE              ,BEAN_CLASS                                                                      ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   6              ,'single'        , 'singleNetworkMover'   ,'ru.sberbank.syncserver2.service.file.transport.AlphaNetworkFileMover'          ,null            ,null                ,6          , 'This service moves deflated files to file transporter')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   9             ,'common'        ,'misEventHandler','ru.sberbank.syncserver2.service.core.event.impl.AlphaEventHandler'               ,null                  ,null                 ,9         , null, 'This is system event handler for alpha')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   10             ,'public'        ,'genStatusRequester','ru.sberbank.syncserver2.service.generator.single.pub.GeneratorAutogenStatusPublicService'               ,null                  ,null                 ,10         , 'request.do', 'This is system event handler for alpha')

go
set identity_insert SYNC_SERVICES off
go


--4. Filling properties
--4.0.0. For database notification logger
exec SP_SYNC_RESET_PROPERTIES 'common','databaseLogger'
exec SP_SYNC_SET_PROPERTY     'common','databaseLogger','databaseName','@ALPHA_MONITOR_DB@'

--4.1.1 For singleGenerator
exec SP_SYNC_RESET_PROPERTIES 'single','singleGenerator'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'threadCount' ,'8'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'configHome'         ,'@ROOT_FOLDER@/generator/config'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'localFileHome'      ,'@ROOT_FOLDER@/generator/single/generator'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'localArchiveFolder' ,'@ROOT_FOLDER@/generator/single/network/archive'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'networkTempFolder'  ,'@NETWORK_TEMP_FOLDER@/single/files'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'networkTargetFolder','@NETWORK_ROOT_FOLDER@/single/files'
exec SP_SYNC_SET_PROPERTY 'single','singleGenerator' ,'logsStorageTimes'   ,'5'
go

--4.1.2. For local deflater
exec SP_SYNC_RESET_PROPERTIES 'single','singleLocalDeflater'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','localSourceFolder'  ,'@ROOT_FOLDER@/generator/single/generator/generated'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','localTempFolder1'   ,'@ROOT_FOLDER@/generator/single/deflater/temp1'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','localTempFolder2'   ,'@ROOT_FOLDER@/generator/single/deflater/temp2'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','localDestFolder'    ,'@ROOT_FOLDER@/generator/single/deflater/outbox'
exec SP_SYNC_SET_PROPERTY 'single','singleLocalDeflater','maxFragmentFileSizeMb'    ,'@FILE_FRAGMENT_SIZE_MB@'
go

--4.1.3. For alpha network mover
exec SP_SYNC_RESET_PROPERTIES 'single','singleFileLister'
go
exec SP_SYNC_RESET_PROPERTIES 'single','singleNetworkMover'
exec SP_SYNC_SET_PROPERTY 'single','singleNetworkMover','localSourceFolder'    , '@ROOT_FOLDER@/generator/single/deflater/outbox'
exec SP_SYNC_SET_PROPERTY 'single','singleNetworkMover','localTempFolder'      , '@ROOT_FOLDER@/generator/single/network/temp'
exec SP_SYNC_SET_PROPERTY 'single','singleNetworkMover','networkTempFolder'    , '@NETWORK_TEMP_FOLDER@/single/files'
exec SP_SYNC_SET_PROPERTY 'single','singleNetworkMover','networkTargetFolder'  , '@NETWORK_ROOT_FOLDER@/single/files'
exec SP_SYNC_SET_PROPERTY 'single','singleNetworkMover','localArchiveFolder'   , '@ROOT_FOLDER@/generator/single/network/archive'
exec SP_SYNC_SET_PROPERTY 'single','singleNetworkMover','sharedHostsListerCode', 'singleFileLister'
go

IF OBJECT_ID('IPAD_JOBS') is not null DROP TABLE IPAD_JOBS
GO
IF OBJECT_ID('IPAD_JOB_LOGS') is not null DROP TABLE IPAD_JOB_LOGS
GO
IF OBJECT_ID('SP_IPAD_HOUSEKEEPING') is not null DROP PROCEDURE SP_IPAD_HOUSEKEEPING
GO
IF OBJECT_ID('SP_IPAD_GET_LAST_REFRESH_TIME') is not null DROP PROCEDURE SP_IPAD_GET_LAST_REFRESH_TIME
GO
IF OBJECT_ID('SP_IPAD_SET_LAST_REFRESH_TIME') is not null DROP PROCEDURE SP_IPAD_SET_LAST_REFRESH_TIME
GO
CREATE TABLE [dbo].[IPAD_JOB_LOGS](
	[ID] [int] IDENTITY(1,1) NOT NULL PRIMARY KEY,
	[REPORT_CODE] [varchar](64) NOT NULL,
	[EVENT_TYPE] [char](1) NOT NULL CHECK(EVENT_TYPE IN ('C','G')), --C for check, G for generation
	[EVENT_TIME] [datetime] NOT NULL
) ON [PRIMARY]
GO
CREATE TABLE [dbo].[IPAD_JOBS](
	[REPORT_ID] [int] IDENTITY(1,1) NOT NULL PRIMARY KEY,
	[REPORT_CODE] [varchar](64) NOT NULL,
	[LAST_GENERATION_TIME] [datetime] NOT NULL,
	[LAST_ACTUAL_TIME] [datetime] NOT NULL,
	[LAST_ACTUAL_TIME2] [datetime] NULL,
)
GO
CREATE PROCEDURE [dbo].[SP_IPAD_HOUSEKEEPING]
AS
  delete from ipad_job_logs where event_time<DATEADD(day,-30,getdate())
GO
CREATE PROCEDURE [dbo].[SP_IPAD_GET_LAST_REFRESH_TIME](@report_code varchar(64))
AS
begin
  set nocount on
  declare @actual_time datetime 
  declare @actual_time2 datetime 
  select @actual_time=last_actual_time,@actual_time2=last_actual_time2  from IPAD_JOBS where report_code=@report_code
  if @actual_time is null begin select @actual_time=convert(datetime,'01.01.2000',104) end
  select @actual_time,@actual_time2
end
GO
CREATE PROCEDURE [dbo].[SP_IPAD_SET_LAST_REFRESH_TIME](@report_code varchar(64), @actual_time datetime, @actual_time2 datetime = null)
AS
begin
  begin tran
    --1. Change actual time
    set nocount on
    update ipad_jobs set last_actual_time=@actual_time, last_actual_time2=@actual_time2,last_generation_time=GETDATE() 
    where report_code=@report_code
    
    if @@rowcount=0 begin
        insert into ipad_jobs(report_code, last_actual_time, last_actual_time2, last_generation_time) values(@report_code, @actual_time, @actual_time2, GETDATE())
    end
  
    --2. Track actual time change
    insert into IPAD_JOB_LOGS(REPORT_CODE,EVENT_TYPE,EVENT_TIME) VALUES(@report_code, 'G',GETDATE()) 
  commit
end
GO


IF OBJECT_ID('CLUSTER_STATUS') IS NOT NULL DROP TABLE CLUSTER_STATUS
GO
CREATE TABLE CLUSTER_STATUS(
  CLUSTER_STATUS_ID int primary key check(CLUSTER_STATUS_ID=1) ,
  ACTIVE_HOST_NAME  varchar(256)                       not null,
  LAST_PING_TIME    datetime                           not null,
)
GO
INSERT INTO CLUSTER_STATUS VALUES(1, 'tv-inavs-8r2-02', getdate())
GO

IF OBJECT_ID('SP_IS_HOST_ACTIVE') IS NOT NULL DROP PROCEDURE SP_IS_HOST_ACTIVE
GO
CREATE PROCEDURE SP_IS_HOST_ACTIVE
  @HOST_NAME       varchar(256) 
AS
  declare @active_host_name varchar(256)
  declare @is_host_active varchar(256)
  begin tran
    set nocount on
    UPDATE CLUSTER_STATUS SET LAST_PING_TIME=getdate() 
    WHERE ACTIVE_HOST_NAME=@HOST_NAME
    if @@rowcount>0 begin
       set @is_host_active = 'true' 
    end else begin
      UPDATE CLUSTER_STATUS SET LAST_PING_TIME=getdate(), ACTIVE_HOST_NAME = @HOST_NAME
      WHERE LAST_PING_TIME<dateadd(second,-300, getdate())
      set @is_host_active = case when @@rowcount>0 then 'true' else 'false' end
    end
    select @active_host_name=ACTIVE_HOST_NAME from CLUSTER_STATUS
    select @is_host_active is_host_active, @active_host_name active_host_name 
  commit;
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