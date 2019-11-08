use [SIGMAMONITOR1]
go
set nocount on
go
IF OBJECT_ID('SYNC_CONFIG') IS NOT NULL DROP TABLE SYNC_CONFIG
GO
CREATE TABLE SYNC_CONFIG(
   PROPERTY_KEY    VARCHAR(256)  NOT NULL,
   PROPERTY_VALUE  VARCHAR(max)     NULL,
   PROPERTY_DESC   VARCHAR(1024)     NULL,
)
GO
SET NOCOUNT ON
INSERT INTO SYNC_CONFIG VALUES('ALERT_TRANSPORTS'               ,'SMTP','Way of notification. Three values are supported: SMS | SMTP | SMS;SMTP')
INSERT INTO SYNC_CONFIG VALUES('ALERT_ADDRESSES'                ,'LBKozhinsky.SBT@sberbank.ru','List of email addresses separated by semicolon')
INSERT INTO SYNC_CONFIG VALUES('ALERT_PHONES'                   ,'','List of phones like 79857619675 separated by semicolon')
INSERT INTO SYNC_CONFIG VALUES('SMS_PROXY_SERVER'               ,'','URL of DpSmsProxy server')
INSERT INTO SYNC_CONFIG VALUES('SMTP_HOST'                      ,'mail.sberbank.ru','SMTP Host')
INSERT INTO SYNC_CONFIG VALUES('SMTP_PORT'                      ,'587','SMTP port')
INSERT INTO SYNC_CONFIG VALUES('SMTP_FROM'                      ,'IncidentManagement@sberbank.ru','Default email address for From in sent notifications')
INSERT INTO SYNC_CONFIG VALUES('SMTP_USER'                      ,'IncidentManagement','SMTP User')
INSERT INTO SYNC_CONFIG VALUES('SMTP_PASSWORD'                  ,'c,th,fyr2013','SMTP Password')
INSERT INTO SYNC_CONFIG VALUES('FP_ROOT'                        ,'\\\\brass2\\box1\\i-navigator','UNC Address of file transporter')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_URL1'                 ,'http://10.21.136.238:4004','DataPower URL #1. Server does load balancing between DataPower URL #1 and DataPower URL #2 and on failure tries to use another DataPower')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_URL2'                 ,'http://10.21.136.238:4004','DataPower URL #2. Server does load balancing between DataPower URL #1 and DataPower URL #2 and on failure tries to use another DataPower')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SQLPROXY_HOST1'           ,'finik2-new','Address of SQL Proxy Server used by DataPower #1')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_SQLPROXY_HOST2'           ,'finik2-new','Address of SQL Proxy Server used by DataPower #2')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_DB'               ,'MIS_IPAD_MONITOR2','Alpha Monitor Database name')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_SERVICE'          ,'finik2-new','The service name at SQL Proxy used to communicate with Alpha Monitor database')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_CONVERSION'           ,'CONVERT_TO_NEW_SIGMA' ,'Defaines DataPower compatibility mode. Temporary hack to avoid waiting until datapower is updated.')
INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_OVERRIDE_PROVIDER'    ,'DISPATCHER','Always should be equal to DISPATCHER')
INSERT INTO SYNC_CONFIG VALUES('IS_DB_LOGGING_ENABLED'          ,'true','Enables or disables logging to the database')
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

--1. Deleting old, it simplifies restart
DELETE FROM SYNC_SERVICES
DELETE FROM SYNC_FOLDERS
go

--2. Filling list of folders
set identity_insert SYNC_FOLDERS on
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(1,'monitor'   , 1, 'All Monitor Services are running in this service folder')
set identity_insert SYNC_FOLDERS off

exec SP_SYNC_ADD_TEMPLATE 'ru.sberbank.syncserver2.service.monitor.MonitorService'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.SpaceAndLastModifiedCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'includeFolders' ,'N','List of all monitored services, it should include UNC-address of file transporter'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'includeMaxMB'   ,'N','If space used by files at file transporter in includeFolders become bigger than this value then notification will be sent'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'includeMaxHours','N','If any file in includeFolders at file transporter becomes older by more hours than this value then notification will be sent'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'excludeFolders' ,'N','These folders will be ignored during the space and date check'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'tempFolders'    ,'N','Old files in temp folders are deleted automatically'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'tempMaxHours'   ,'N','Any files in tempFolders older than this value will be deleted'
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
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'dataPowerBeanCode','N','Link to dataPowerService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'databaseName'     ,'N','The name of database used by Alpha Monitor'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id ,'provider'         ,'N','SQLService provider, should be DISPATCHER'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id ,'service'          ,'N','SQL Service at SQL Proxy Server for writing to generator database'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.WritePermissionCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'includeFolders' ,'N','List of all monitored services'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.DatapowerAvailabilityChecker'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'connectionService' ,'N','SQL Service at SQL Proxy Server for getting simple request'
go

--3. Filing list of services
set identity_insert SYNC_SERVICES on
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   1              ,'monitor'     , 'spaceAndTimeCheck','ru.sberbank.syncserver2.service.monitor.check.SpaceAndLastModifiedCheck'  ,'monitor'       ,'checkAction'       ,1          ,'This service used by "monitor" to check disk space and too old files at file transporter')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   2              ,'monitor'     , 'datapowerLogger'   ,'ru.sberbank.syncserver2.service.monitor.DataPowerNotificationLogger'     ,'monitor'       ,'datapowerLogger'   ,2          ,'This server used by monitor to send notifications and pings to Alpha. It sends requests to SQL Proxy Server and it executes in Alpha Monitor database. This sevices uses dataPowerService for sending requests')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   3              ,'monitor'      ,'datapowerService' ,'ru.sberbank.syncserver2.service.sql.DataPowerService'                     ,null            ,null                ,3          ,'This service sends requests to DataPower and further to SQL Proxy Server in Alpha')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE         ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   4              ,'monitor'     , 'monitor'          ,'ru.sberbank.syncserver2.service.monitor.MonitorService'                   ,null            ,null                ,4          ,'This service starts spaceAndTimeCheck and use datapowerLogger to send notification to the database used by Alpha Monitor. It also sends pings to Alpha Monitor')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   5              ,'monitor'     , 'permissionCheck','ru.sberbank.syncserver2.service.monitor.check.WritePermissionCheck'  ,'monitor'       ,'checkAction'       ,5          ,'This service used by "monitor" to check write permission')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   6              ,'monitor'     , 'datapowerAvailabilityCheck','ru.sberbank.syncserver2.service.monitor.check.DatapowerAvailabilityChecker'  ,'monitor'       ,'checkAction'       ,6          ,'This service used by "monitor" to check Datapower availability')
go
set identity_insert SYNC_SERVICES off
go

--4. Filling properties
--4.1 For MonitorServuice
exec SP_SYNC_RESET_PROPERTIES 'monitor','monitor'
go
exec SP_SYNC_RESET_PROPERTIES 'monitor','spaceAndTimeCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','includeFolders' ,'@FP_ROOT@'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','includeMaxMB'   ,'8192'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','includeMaxHours','24'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','excludeFolders' ,''
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','tempFolders'    ,'@FP_ROOT@\\OUT\\temp;@FP_ROOT@\\IN\\temp'

--exec SP_SYNC_SET_PROPERTY   'monitor','spaceAndTimeCheck','excludeFolders' ,'@FP_ROOT@\\IN\\prod\\mbr\\mbr_december_for_mis;@FP_ROOT@\\OUT\\temp;@FP_ROOT@\\IN\\temp'
--exec SP_SYNC_SET_PROPERTY   'monitor','spaceAndTimeCheck','tempFolders'    ,'@FP_ROOT@\\OUT\\temp;@FP_ROOT@\\IN\\temp'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','tempMaxHours'   ,'24'
go
exec SP_SYNC_RESET_PROPERTIES 'monitor','datapowerService'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerService','dataPowerURL1'   ,'@DATAPOWER_URL1@'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerService','dataPowerURL2'   ,'@DATAPOWER_URL2@'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerService','overrideProvider','@DATAPOWER_OVERRIDE_PROVIDER@'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerService','overrideService1','@ALPHA_SQLPROXY_HOST1@'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerService','overrideService2','@ALPHA_SQLPROXY_HOST2@'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerService','conversion'      ,'@DATAPOWER_CONVERSION@'
go
exec SP_SYNC_RESET_PROPERTIES 'monitor','datapowerLogger'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerLogger','dataPowerBeanCode','datapowerService'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerLogger','databaseName','@ALPHA_MONITOR_DB@'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerLogger','provider'    ,'@DATAPOWER_OVERRIDE_PROVIDER@'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerLogger','service'     ,'@ALPHA_MONITOR_SERVICE@'
go
exec SP_SYNC_RESET_PROPERTIES 'monitor','permissionCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','permissionCheck','includeFolders' ,'@FP_ROOT@\\IN;@FP_ROOT@\\OUT'
go
exec SP_SYNC_RESET_PROPERTIES 'monitor','datapowerAvailabilityCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerAvailabilityCheck','connectionService' ,'@ALPHA_MONITOR_SERVICE@'
go

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