use [syncserver]
go
set nocount on
go
IF OBJECT_ID('SYNC_CONFIG') IS NOT NULL DROP TABLE SYNC_CONFIG
GO
CREATE TABLE SYNC_CONFIG(
   PROPERTY_KEY    VARCHAR(256)  NOT NULL,
   PROPERTY_VALUE  VARCHAR(1024) NOT NULL,
   PROPERTY_DESC   VARCHAR(1024)     NULL
)
GO
SET NOCOUNT ON
INSERT INTO SYNC_CONFIG VALUES('ROOT_FOLDER'                           ,'D:/usr/cache','Root folder for local cache')
INSERT INTO SYNC_CONFIG VALUES('NETWORK_ROOT_FOLDER'                   ,'\\\\brass2\\box1\\i-navigator\\IN\\uat4','Root folder for file transporter')
INSERT INTO SYNC_CONFIG VALUES('NETWORK_SHARED_HOSTS_FOR_CHANGESETS'   ,'localhost','List of hosts used by iRubricator separated by semicolon. Server will transmit changeset files to this hosts only')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_URL1'                        ,'http://10.21.136.238:4004','DataPower URL #1. Server does load balancing between DataPower URL #1 and DataPower URL #2 and on failure tries to use another DataPower')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_URL2'                        ,'http://10.21.136.238:4004','DataPower URL #2. Server does load balancing between DataPower URL #1 and DataPower URL #2 and on failure tries to use another DataPower')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SQLPROXY_HOST1'                  ,'finik2-new','Address of SQL Proxy Server used by DataPower #1')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SQLPROXY_HOST2'                  ,'finik2-new','Address of SQL Proxy Server used by DataPower #2')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SOURCE_DB'                       ,'MIS_IPAD','The name of database at finik1/2 with procedures for checking permissions SP_IS_ALLOWED_TO_DOWNLOAD_FILE and SP_IS_ALLOWED_TO_USE_APP')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SOURCE_SERVICE'                  ,'finik2-new','The service name at SQL Proxy used to communicate with ALPHA_SOURCE_DB at finik1/finik2')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_DB'                      ,'MIS_IPAD_MONITOR','Alpha Monitor Database name')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_SERVICE'                 ,'finik2-new','The service name at SQL Proxy used to communicate with Alpha Monitor database')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_GENERATOR_DB'                    ,'MIS_IPAD_GENERATOR','The name of database in Alpha used by Generator')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_GENERATOR_SERVICE'               ,'finik2-new','The service name at SQL Proxy used to communicate with Sync Generator database')

INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_OVERRIDE_PROVIDER'           ,'DISPATCHER','Always should be equal to DISPATCHER')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_CONVERSION'                  ,'CONVERT_TO_NEW_SIGMA' ,'Defines DataPower compatibility mode. Temporary hack to avoid waiting until datapower is updated.')
INSERT INTO SYNC_CONFIG VALUES('DEBUG_SMS'                             ,'false','For debugging')
INSERT INTO SYNC_CONFIG VALUES('DEBUG_SINGLE_FILE_ONLY'                ,'false','For debugging')
INSERT INTO SYNC_CONFIG VALUES('LDAP_PROVIDER'                         ,'ldap://lake1.sigma.sbrf.ru:389;ldap://lake2.sigma.sbrf.ru:389','LDAP Server Addresses separated by comma')
INSERT INTO SYNC_CONFIG VALUES('LDAP_DOMAIN'                           ,'SIGMA'                   ,'Windows Domain name')
INSERT INTO SYNC_CONFIG VALUES('LDAP_USERNAME'                         ,'IncidentManagement'      ,'LDAP User')
INSERT INTO SYNC_CONFIG VALUES('LDAP_PASSWORD'                         ,'c,th,fyr2013'            ,'LDAP Password')
INSERT INTO SYNC_CONFIG VALUES('IS_DB_LOGGING_ENABLED'                 ,'true','Enables or disables logging to the database')
INSERT INTO SYNC_CONFIG VALUES('PUSH_CERTIFICATE_CONFIG_FOLDER'        ,'D:/usr/cache/push','Path to push certificates folder')
INSERT INTO SYNC_CONFIG VALUES('PUSH_CERTIFICATE_PRODUCTION_MODE'      ,'false','For production must be true, for dev/psi must be false.')
INSERT INTO SYNC_CONFIG VALUES('PUSH_NOTIFICATION_WHEN_NEW_FILE_LOADED','false','Push notification when file was loaded to sigma.')
INSERT INTO SYNC_CONFIG VALUES('LDAP_USER_GROUP_MANAGER_SETTINGS','','Java property XML fromat for ldap connection (provider, domain, username, password, iNavigatorGroupLdapDN, base_ctx)')
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
INSERT INTO READONLY_CONFIG VALUES('VERSION','12.10.2015 17:31')
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
INSERT INTO [EMPLOYEE_ROLES] VALUES(3,'OperatorMISAccess')
INSERT INTO [EMPLOYEES] ([EMPLOYEE_ROLE_ID],[EMPLOYEE_EMAIL], [EMPLOYEE_NAME], [leave_date], [EMPLOYEE_PASSWORD],[PASSWORD_CHANGED_DATE]) VALUES (3,'operator', 'operator', null, 'E10ADC3949BA59ABBE56E057F20F883E',getdate())
GO

--1. Deleting old, it simplifies restart
DELETE FROM SYNC_SERVICES
DELETE FROM SYNC_FOLDERS
go

--2. Filling templates of services and templates of properties
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.transport.SharedSigmaNetworkFileMover'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'networkSourceFolder','N','The folder at file transporter with files copied from Sigma'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder','N','The folder at local disk used for temporary storage while moving file from file transporter to local disk'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localDestFolder','N','After file is fully copied and MD5 is verified, file is copied to this folder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'networkSharedFolder','N','Before moving file to a local disk, file is moved to this folder at file transporter. It is required to prevent overwriting of file by next update from Alpha'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'staticSharedHosts','Y','The list of hosts the file should be delivered to. It is possible to use this param of FileLister defined in sharedHostsListerCode'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'sharedHostsListerCode','Y','The link to file lister service. It is required to identify the hosts the file should be moved to. This param could be undefined and staticSharedHosts could be defined'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'debugModeWithSMSOnDelivery','Y','This parameter could be used for debugging only'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.transport.LocalInflater'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localSourceFolder','N','After file appears in localSourceFolder the service moves it to localTempFolder1'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder1' ,'N','After file appears in this folder the service starts inflating file to a filer with same name in localTempFolder2'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder2' ,'N','This folder is used to store inflated files while process of inflation. After inflation finished the file is copied to localDestFolder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localDestFolder'  ,'N','The service moves to this folder completed inflated file'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.transport.LocalDeflater'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localSourceFolder','N','After file appears in localSourceFolder the service moves it to localTempFolder1'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder1' ,'N','After file appears in this folder the service starts deflating file to a filer with same name in localTempFolder2'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localTempFolder2' ,'N','This folder is used to store deflated files while the deflation. After deflation finished the file is copied to localDestFolder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'localDestFolder'  ,'N','The service moves to this folder completed inflated file'
go



declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.transport.LocalFileMover'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'srcFolder','N','Source folder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dstFolder','N','Destination folder'
go


declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,  'ru.sberbank.syncserver2.service.file.transport.LocalFileCopier'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'srcFolder' ,'N','Source folder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dstFolder1','N','Destination folder #1. Should be defined'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dstFolder2','Y','Destination folder #1. Could be undefined'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dstFolder3','Y','Destination folder #1. Could be undefined'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dstFolder4','Y','Destination folder #1. Could be undefined'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dstFolder5','Y','Destination folder #1. Could be undefined'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.cache.SingleFileLoader'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'inboxFolder'  ,'Y','The folder for incoming files'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'tempFolder'   ,'N','When file appears in incomingFolder, they are moved to tempFolder to prevent overwriting during splitting and loading'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'archiveFolder','N','After file is splitted'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'cacheFolder'  ,'N','This folder contains subfolders with content and files with information about loaded files'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'chunkSize'    ,'Y','The chunk size'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.cache.FileCache'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'debugModeWithoutLoadToMemory','Y','This option could be used for debugging purposes only'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.cache.FileCacheDraftSupported'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'debugModeWithoutLoadToMemory','Y','This option could be used for debugging purposes only'
go


declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.file.FileService'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.sql.SQLiteService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localIncomingFile','N','The folder the file should arrive to'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'localWorkFolder'  ,'N','The file will be moved and used in this folder'
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
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.security.DataPowerSecurityService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dataPowerServiceBeanCode','N','Link to DataPower service'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'isAllowedToUseApp'       ,'N','Template for checking permission to use application'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'isAllowedToDownloadFile' ,'N','Template for checking permission to download file'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id ,'provider'                ,'N','SQLService provider, should be DISPATCHER'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id ,'service'                 ,'N','Database is same for both proxy services so there is no difference'

go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.security.SessionCachedSecurity'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'sessionTimeoutSeconds','Y','We do not send request to Alpha to recheck permissions during this time and use cached result'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.log.LogService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'logDbURL'     ,'N','JDBC Url to the database used for logging'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'logDbUser'    ,'N','User for database used for logging'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'logDbPassword','N','Password for database used for logging'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.log.DataPowerLogService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'dataPowerBeanCode','N','Link to datapowerService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'logSQL'           ,'N','SQL for saving logs to Alpha Generator database'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'provider'         ,'N','SQLService provider, should be DISPATCHER'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'service'          ,'N','Database is same for both proxy services so there is no difference'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.security.LdapUserCheckerServiceGroup'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'provider','N','URL to ldap server'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'domain'  ,'N','Windows Domain'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'username','N','User name for connection to LDAP server'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'password','N','Password for connection to LDAP server'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dataPowerBeanCode'		,'N','Link to dataPowerService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'databaseName'     		,'N','The name of database used by Alpha Monitor'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'databaseGeneratorName'	,'N','The name of database used by Alpha Generator'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id ,'provider'         		,'N','SQLService provider, should be DISPATCHER'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id ,'service'          		,'N','SQL Service at SQL Proxy Server for writing to generator database'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.pushnotifications.PushNotificationService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'configFolder'	, 'N','Path to config folder'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'productionMode' ,'N','Is Production Mode'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.file.cache.SingleFileStatusCacheService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'fileStatusRequestTemplateName','N','File status template name'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'userGroupRequestTemplateName'    ,'N','User group template name'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'fileCacheBeanCode'    ,'N','Bean code of filecache'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'datapowerServiceBeanCode'    ,'N','Bean code of datapower service'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'generatorServiceName'    ,'N','generator Target service name'
go


declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.core.event.impl.SigmaEventHandler'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'sendPushNotificationWhenFileLoaded'    ,'N','send push notification when new file loaded or not'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.ldap.LdapGroupManagementService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'provider'    ,'N','provider for ldap connection'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'domain'    ,'N','domain for ldap connection'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'settings'    ,'N','other settings'
go


--3. Filling list of folders
set identity_insert SYNC_FOLDERS on
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(1,'passport'   , 1,'Passport services')
set identity_insert SYNC_FOLDERS off
go


--4. Filing list of services
--4.1. Filling online and security services
set identity_insert SYNC_SERVICES on
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE    ,PARENT_BEAN_PROPERTY, START_ORDER,SERVLET_PATH    , BEAN_DESC)
values(                   1              ,'passport'        ,'passportNetworkMover','ru.sberbank.syncserver2.service.file.transport.SharedSigmaNetworkFileMover'   ,null                ,null                , 1          , null           ,'This service moves SQLite file for online queries from file transporter to local disk. Not in use at 01.07.2014' )
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   2             ,'passport'        ,'passportInflater'    ,'ru.sberbank.syncserver2.service.file.transport.LocalInflater'                  ,null            ,null                    ,2         ,null        , 'This service inflates file at local disk')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   3             ,'passport'        ,'passportFileLister'        ,'ru.sberbank.syncserver2.service.file.cache.list.DatabaseFileLister'         ,'passportFileLoader'       ,'fileLister'      ,3         ,null           , 'This service queries database to get hostnames the file should be moved to')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY    ,START_ORDER,SERVLET_PATH   , BEAN_DESC)
values(                   4             ,'passport'        ,'passportFileLoader'        ,'ru.sberbank.syncserver2.service.file.cache.SingleFileLoader'                  ,'passportFileCache'  ,'loader'                ,4         ,null           , 'This service split files to chunks and loads them to memory (to fileCache service)' )
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY    ,START_ORDER,SERVLET_PATH   , BEAN_DESC)
values(                   5             ,'passport'        ,'passportFileCache'        ,'ru.sberbank.syncserver2.service.file.cache.FileCacheDraftSupported'            ,'passportFileService','fileCache'             ,5         ,null           , 'This service stores data in memory and reply to requests from misFileService')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY    ,START_ORDER,SERVLET_PATH   , BEAN_DESC)
values(                   6             ,'passport'        ,'passportFileService'       ,'ru.sberbank.syncserver2.service.file.FileService'                             ,null           ,null                    ,6         , 'file.do'      , 'This service accepts requests from iPad')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE    ,PARENT_BEAN_PROPERTY, START_ORDER,SERVLET_PATH    , BEAN_DESC)
values(                   7              ,'passport'        ,'passportDataPowerService' ,'ru.sberbank.syncserver2.service.sql.DataPowerService'                          ,null     ,null , 7          , null            ,'This service sends requests to DataPower')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE    ,PARENT_BEAN_PROPERTY, START_ORDER,SERVLET_PATH    , BEAN_DESC)
values(                   8              ,'passport'        ,'dpLogger'             ,'ru.sberbank.syncserver2.service.log.DataPowerLogService'                       ,null               ,null                , 8          , null           ,'This service used for logging events to Alpha databases and it uses datapower to send logging information to Alpha')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY        ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   9             ,'passport'        ,'passportDataPowerSecurity' ,'ru.sberbank.syncserver2.service.security.DataPowerSecurityService'            ,'passportCachedSecurity','originalSecurityService',9         ,null        , 'This service used for checking permissions. It sends requests to datapower service to check permissions')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY        ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   10             ,'passport'        ,'passportCachedSecurity'    ,'ru.sberbank.syncserver2.service.security.SessionCachedSecurity'               ,'passportFileService','securityService'           ,10         ,null        , 'This service caches the result of permission check')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY        ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   11             ,'passport'        ,'ldapUserCheckService','ru.sberbank.syncserver2.service.security.LdapUserCheckerServiceGroup',null            ,null                        ,11         , null       , 'This service requests LDAP to check user certificate')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY        ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   12             ,'passport'        ,'passportNotificationLogger','ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger'          ,null            ,null                        ,12         ,null        , 'This service sends notifications to Alpha using datapower')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   13             ,'passport'        ,'passportFileStatusCacheServer','ru.sberbank.syncserver2.service.file.cache.SingleFileStatusCacheService','passportFileLoader'                  ,'fileStatusCacheService'                 ,13         , null, 'Single file statis cache server')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   14             ,'passport'        ,'pushNotificationService','ru.sberbank.syncserver2.service.pushnotifications.PushNotificationService'               ,null                  ,null                 ,14         , null, 'This service sends push notifications to out.')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE                  ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   15             ,'passport'        ,'xmlPublicService','ru.sberbank.syncserver2.service.pub.SyncserverPublicServices'               ,null                  ,null                 ,15         , 'request.do', 'This is public xml service')

go
set identity_insert SYNC_SERVICES off
go


--4. Filling properties
--4.1. Filling online services
--4.1.1. Filling datapower service
exec SP_SYNC_RESET_PROPERTIES 'passport','passportDataPowerService'
exec SP_SYNC_SET_PROPERTY     'passport','passportDataPowerService','dataPowerURL1'   ,'@DATAPOWER_URL1@'
exec SP_SYNC_SET_PROPERTY     'passport','passportDataPowerService','dataPowerURL2'   ,'@DATAPOWER_URL2@'
exec SP_SYNC_SET_PROPERTY     'passport','passportDataPowerService','overrideProvider','@DATAPOWER_OVERRIDE_PROVIDER@'
exec SP_SYNC_SET_PROPERTY     'passport','passportDataPowerService','overrideService1','@ALPHA_SQLPROXY_HOST1@'
exec SP_SYNC_SET_PROPERTY     'passport','passportDataPowerService','overrideService2','@ALPHA_SQLPROXY_HOST2@'
exec SP_SYNC_SET_PROPERTY     'passport','passportDataPowerService','conversion'      ,'@DATAPOWER_CONVERSION@'

exec SP_SYNC_RESET_PROPERTIES 'passport','passportDataPowerSecurity'
exec SP_SYNC_SET_PROPERTY     'passport','passportDataPowerSecurity', 'dataPowerServiceBeanCode','passportDataPowerService'
exec SP_SYNC_SET_PROPERTY     'passport','passportDataPowerSecurity', 'isAllowedToUseApp','SYNCSERVER.IS_ALLOWED_TO_USE_APP'
exec SP_SYNC_SET_PROPERTY     'passport','passportDataPowerSecurity', 'isAllowedToDownloadFile','SYNCSERVER.IS_ALLOWED_TO_DOWNLOAD_FILE'
exec SP_SYNC_SET_PROPERTY     'passport','passportDataPowerSecurity',  'provider'         ,'@DATAPOWER_OVERRIDE_PROVIDER@'
exec SP_SYNC_SET_PROPERTY     'passport','passportDataPowerSecurity',  'service'          ,'@ALPHA_SOURCE_SERVICE@'

--4.1.2. Filling sqlite services
exec SP_SYNC_RESET_PROPERTIES 'passport','passportNetworkMover'
exec SP_SYNC_SET_PROPERTY     'passport','passportNetworkMover','networkSourceFolder','@NETWORK_ROOT_FOLDER@/single/files'
exec SP_SYNC_SET_PROPERTY     'passport','passportNetworkMover','localTempFolder'    ,'@ROOT_FOLDER@/@DB_NAME@/single/network/temp'
exec SP_SYNC_SET_PROPERTY     'passport','passportNetworkMover','localDestFolder'    ,'@ROOT_FOLDER@/@DB_NAME@/single/local/inflater/inbox'
exec SP_SYNC_SET_PROPERTY     'passport','passportNetworkMover','networkSharedFolder','@NETWORK_ROOT_FOLDER@/single/shared'
exec SP_SYNC_SET_PROPERTY     'passport','passportNetworkMover','staticSharedHosts'  ,'localhost'
exec SP_SYNC_SET_PROPERTY     'passport','passportNetworkMover','debugModeWithSMSOnDelivery','@DEBUG_SMS@'
go

--exec SP_SYNC_RESET_PROPERTIES 'passport','misSQLiteService'
--exec SP_SYNC_SET_PROPERTY     'passport','misSQLiteService','localIncomingFile','@ROOT_FOLDER@/@DB_NAME@/online/local/inbox/online.sqlite'
--exec SP_SYNC_SET_PROPERTY     'passport','misSQLiteService','localWorkFolder'  ,'@ROOT_FOLDER@/@DB_NAME@/online/local/work'
--go

--4.9 For datapower logger
exec SP_SYNC_RESET_PROPERTIES 'passport','dpLogger'
exec SP_SYNC_SET_PROPERTY     'passport','dpLogger','dataPowerBeanCode','passportDataPowerService'
exec SP_SYNC_SET_PROPERTY     'passport','dpLogger','logSQL'           ,'SYNCSERVER.LOG_SQL'
exec SP_SYNC_SET_PROPERTY     'passport','dpLogger','provider'         ,'@DATAPOWER_OVERRIDE_PROVIDER@'
exec SP_SYNC_SET_PROPERTY     'passport','dpLogger','service'          ,'@ALPHA_GENERATOR_SERVICE@'

--4.9 For notification logger
exec SP_SYNC_RESET_PROPERTIES 'passport','passportNotificationLogger'
exec SP_SYNC_SET_PROPERTY     'passport','passportNotificationLogger','dataPowerBeanCode','passportDataPowerService'
exec SP_SYNC_SET_PROPERTY     'passport','passportNotificationLogger','databaseName','@ALPHA_MONITOR_DB@'
exec SP_SYNC_SET_PROPERTY     'passport','passportNotificationLogger','databaseGeneratorName','@ALPHA_GENERATOR_DB@'
exec SP_SYNC_SET_PROPERTY     'passport','passportNotificationLogger','provider'    ,'@DATAPOWER_OVERRIDE_PROVIDER@'
exec SP_SYNC_SET_PROPERTY     'passport','passportNotificationLogger','service'     ,'@ALPHA_MONITOR_SERVICE@'
go

exec SP_SYNC_RESET_PROPERTIES 'passport','ldapUserCheckService'
exec SP_SYNC_SET_PROPERTY     'passport','ldapUserCheckService','provider','@LDAP_PROVIDER@'
exec SP_SYNC_SET_PROPERTY     'passport','ldapUserCheckService','domain','@LDAP_DOMAIN@'
exec SP_SYNC_SET_PROPERTY     'passport','ldapUserCheckService','username','@LDAP_USERNAME@'
exec SP_SYNC_SET_PROPERTY     'passport','ldapUserCheckService','password' ,'@LDAP_PASSWORD@'
go

--4.2. Filling single file services
--4.2.1 For misNetworkMover
--exec SP_SYNC_RESET_PROPERTIES 'single','misNetworkMover'
--exec SP_SYNC_SET_PROPERTY     'single','misNetworkMover','networkSourceFolder'  ,'@NETWORK_ROOT_FOLDER@/single/files'
--exec SP_SYNC_SET_PROPERTY     'single','misNetworkMover','networkSharedFolder'  ,'@NETWORK_ROOT_FOLDER@/single/shared'
--exec SP_SYNC_SET_PROPERTY     'single','misNetworkMover','localTempFolder'      ,'@ROOT_FOLDER@/@DB_NAME@/single/network/temp'
--exec SP_SYNC_SET_PROPERTY     'single','misNetworkMover','localDestFolder'      ,'@ROOT_FOLDER@/@DB_NAME@/single/local/inflater/inbox'
--exec SP_SYNC_SET_PROPERTY     'single','misNetworkMover','sharedHostsListerCode','misDbFileLister'
--exec SP_SYNC_SET_PROPERTY     'single','misNetworkMover','debugModeWithSMSOnDelivery','@DEBUG_SMS@'
--go

--4.2.2 For misLocalInflater
exec SP_SYNC_RESET_PROPERTIES 'passport','passportInflater'
exec SP_SYNC_SET_PROPERTY     'passport','passportInflater','localSourceFolder'  ,'@ROOT_FOLDER@/@DB_NAME@/single/local/inflater/inbox'
exec SP_SYNC_SET_PROPERTY     'passport','passportInflater','localTempFolder1'   ,'@ROOT_FOLDER@/@DB_NAME@/single/local/inflater/temp1'
exec SP_SYNC_SET_PROPERTY     'passport','passportInflater','localTempFolder2'   ,'@ROOT_FOLDER@/@DB_NAME@/single/local/inflater/temp2'
exec SP_SYNC_SET_PROPERTY     'passport','passportInflater','localDestFolder'    ,'@ROOT_FOLDER@/@DB_NAME@/single/local/inflater/inflated'
go

--4.2.3. For misFileLoader
exec SP_SYNC_RESET_PROPERTIES 'passport','passportFileLoader'
exec SP_SYNC_SET_PROPERTY     'passport','passportFileLoader','chunkSize','1048576'
exec SP_SYNC_SET_PROPERTY     'passport','passportFileLoader','inboxFolder','@ROOT_FOLDER@/@DB_NAME@/single/local/inflater/inflated'
exec SP_SYNC_SET_PROPERTY     'passport','passportFileLoader','tempFolder','@ROOT_FOLDER@/@DB_NAME@/single/local/loader/temp'
exec SP_SYNC_SET_PROPERTY     'passport','passportFileLoader','archiveFolder','@ROOT_FOLDER@/@DB_NAME@/single/local/loader/archive'
exec SP_SYNC_SET_PROPERTY     'passport','passportFileLoader','cacheFolder','@ROOT_FOLDER@/@DB_NAME@/single/local/fileCache'
go
--4.2.4. For misFileCache
exec SP_SYNC_RESET_PROPERTIES 'passport','passportFileCache'
exec SP_SYNC_SET_PROPERTY     'passport','passportFileCache','debugModeWithoutLoadToMemory','@DEBUG_SINGLE_FILE_ONLY@'
go


exec SP_SYNC_RESET_PROPERTIES 'passport','pushNotificationService'
exec SP_SYNC_SET_PROPERTY     'passport','pushNotificationService','configFolder'   ,'@PUSH_CERTIFICATE_CONFIG_FOLDER@'
exec SP_SYNC_SET_PROPERTY     'passport','pushNotificationService','productionMode' ,'@PUSH_CERTIFICATE_PRODUCTION_MODE@'
go

exec SP_SYNC_RESET_PROPERTIES 'passport','passportFileStatusCacheServer'
exec SP_SYNC_SET_PROPERTY     'passport','passportFileStatusCacheServer','fileStatusRequestTemplateName'    ,'SYNCSERVER.SINGLE_FILES_REQUEST_STATUS'
exec SP_SYNC_SET_PROPERTY     'passport','passportFileStatusCacheServer','userGroupRequestTemplateName','SYNCSERVER.GENERATOR_USER_GROUP_DRAFT'
exec SP_SYNC_SET_PROPERTY     'passport','passportFileStatusCacheServer','fileCacheBeanCode','passportFileCache'
exec SP_SYNC_SET_PROPERTY     'passport','passportFileStatusCacheServer','datapowerServiceBeanCode','passportDataPowerService'
exec SP_SYNC_SET_PROPERTY     'passport','passportFileStatusCacheServer','generatorServiceName','@ALPHA_GENERATOR_SERVICE@'
go

--exec SP_SYNC_RESET_PROPERTIES 'common','misEventHandler'
--exec SP_SYNC_SET_PROPERTY     'common','misEventHandler','sendPushNotificationWhenFileLoaded'    ,'@PUSH_NOTIFICATION_WHEN_NEW_FILE_LOADED@'
--go


--5. Special procedure for removing rubricator from list of services
--5.1. Deletion of all but online services
if object_id('SP_SERVICES_CONFIGURE_ONLINE') is not null drop procedure SP_SERVICES_CONFIGURE_ONLINE
go
CREATE PROCEDURE SP_SERVICES_CONFIGURE_ONLINE
as
   DELETE FROM SYNC_SERVICES WHERE  BEAN_CODE not in ('misSQLService','misSQLiteService','misDataPowerService','dpLogger','misSQLiteNetworkMover')
   DELETE FROM SYNC_CACHE_STATIC_FILES
GO

--5.2. For removing all but single files and leave one app - iNavigator
if object_id('SP_SERVICES_CONFIGURE_OFFLINE1') is not null drop procedure SP_SERVICES_CONFIGURE_OFFLINE1
go
CREATE PROCEDURE SP_SERVICES_CONFIGURE_OFFLINE1
as
  DELETE FROM SYNC_SERVICES WHERE BEAN_CODE not in ('misNetworkMover','misLocalInflater','misDbFileLister','misFileLoader','misFileCache','misFileService','misDataPowerSecurity','misCachedSecurity','dpLogger','misDataPowerService')
   DELETE FROM SYNC_CACHE_STATIC_FILES WHERE APP_CODE!='iNavigator'
GO

--5.3. Leaving sincle files and rubricator and remove one apps - IUP
if object_id('SP_SERVICES_CONFIGURE_OFFLINE2') is not null drop procedure SP_SERVICES_CONFIGURE_OFFLINE2
go
CREATE PROCEDURE SP_SERVICES_CONFIGURE_OFFLINE2
as
  DELETE FROM SYNC_SERVICES WHERE BEAN_CODE not in ('misNetworkMover','misLocalInflater','misDbFileLister','misFileLoader','misFileCache','misFileService','misDataPowerSecurity','misCachedSecurity','dpLogger','misDataPowerService')
   DELETE FROM SYNC_CACHE_STATIC_FILES WHERE APP_CODE!='iup'
GO

--5.4. Leaving sincle files and rubricator and remove one apps - IUP
if object_id('SP_SERVICES_CONFIGURE_OFFLINE2') is not null drop procedure SP_SERVICES_CONFIGURE_OFFLINE2
go
CREATE PROCEDURE SP_SERVICES_CONFIGURE_OFFLINE2
as
  DELETE FROM SYNC_SERVICES WHERE BEAN_CODE not in ('misNetworkMover','misLocalInflater','misDbFileLister','misFileLoader','misFileCache','misFileService','misDataPowerSecurity','misCachedSecurity','dpLogger','misDataPowerService')
   DELETE FROM SYNC_CACHE_STATIC_FILES WHERE APP_CODE!='iup'
GO

--5.5. Leaving sincle files for balance and competitors and rubricator
if object_id('SP_SERVICES_CONFIGURE_OFFLINE3') is not null drop procedure SP_SERVICES_CONFIGURE_OFFLINE3
go
CREATE PROCEDURE SP_SERVICES_CONFIGURE_OFFLINE3
as
  DELETE FROM SYNC_SERVICES WHERE BEAN_CODE in ('misSQLService','misSQLiteNetworkMover','misSQLiteService')
  UPDATE SYNC_SERVICES SET PARENT_BEAN_CODE=null, PARENT_BEAN_PROPERTY=null where BEAN_CODE = 'misDataPowerService'
  DELETE FROM SYNC_CACHE_STATIC_FILES WHERE APP_CODE!='balance' and APP_CODE!='competitors'
GO

--5.5. Leaving sincle files and one apps - MIS MOBILE
if object_id('SP_SERVICES_CONFIGURE_OFFLINE4') is not null drop procedure SP_SERVICES_CONFIGURE_OFFLINE4
go
CREATE PROCEDURE SP_SERVICES_CONFIGURE_OFFLINE4
as
  DELETE FROM SYNC_SERVICES WHERE BEAN_CODE not in ('misNetworkMover','misLocalInflater','misDbFileLister','misFileLoader','misFileCache','misFileService','misDataPowerSecurity','misCachedSecurity','dpLogger','misDataPowerService')
  DELETE FROM SYNC_CACHE_STATIC_FILES WHERE APP_CODE!='iup'
GO


if object_id('SP_SERVICES_REMOVE_ALL_BUT_SINGLES') is not null drop procedure SP_SERVICES_REMOVE_ALL_BUT_SINGLES
go
CREATE PROCEDURE SP_SERVICES_REMOVE_ALL_BUT_SINGLES
as
  DELETE FROM SYNC_SERVICES WHERE BEAN_CODE not in ('misNetworkMover','misLocalInflater','misDbFileLister','misFileLoader','misFileCache','misFileService','misDataPowerSecurity','misCachedSecurity','dpLogger','misDataPowerService')
GO

--5.3. For removing all but changesets
if object_id('SP_SERVICES_REMOVE_ALL_BUT_CHANGESETS') is not null drop procedure SP_SERVICES_REMOVE_ALL_BUT_CHANGESETS
go
CREATE PROCEDURE SP_SERVICES_REMOVE_ALL_BUT_CHANGESETS
as
  DELETE FROM SYNC_SERVICES from SYNC_SERVICES WHERE BEAN_CODE not in ('misChangesetNetworkMover','misDynFileLister','misMbrLoader' ,'misFileCache','misFileService','misDataPowerSecurity','misCachedSecurity','dpLogger','misDataPowerService')
GO

--5.4. For removing all but onlines
if object_id('SP_SERVICES_REMOVE_ONLINES') is not null drop procedure SP_SERVICES_REMOVE_ONLINES
go
CREATE PROCEDURE SP_SERVICES_REMOVE_ONLINES
as
  DELETE FROM SYNC_SERVICES WHERE BEAN_CODE in ('misSQLService','misSQLiteNetworkMover','misSQLiteService')
  UPDATE SYNC_SERVICES SET PARENT_BEAN_CODE=null, PARENT_BEAN_PROPERTY=null where BEAN_CODE = 'misDataPowerService'
GO

--5.4. For removing all but onlines
if object_id('SP_FILES_CONFIGURE_HOST_NAMES') is not null drop procedure SP_FILES_CONFIGURE_HOST_NAMES
go
CREATE PROCEDURE SP_FILES_CONFIGURE_HOST_NAMES 
  @hosts varchar(256)
as
  UPDATE SYNC_CACHE_STATIC_FILES SET HOSTS=@hosts
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