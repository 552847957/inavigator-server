use ALPHAMONITOR;
go
set nocount on
go
IF OBJECT_ID('SYNC_CONFIG') IS NOT NULL DROP TABLE SYNC_CONFIG
GO
CREATE TABLE SYNC_CONFIG(
   PROPERTY_KEY    VARCHAR(256) ,
   PROPERTY_VALUE  VARCHAR(1024),
   PROPERTY_DESC   VARCHAR(1024)
)
GO
SET NOCOUNT ON
INSERT INTO SYNC_CONFIG VALUES('ALERT_TRANSPORTS'               ,'SMS'                           ,'Way of notification. Three values are supported: SMS | SMTP | SMS;SMTP')
INSERT INTO SYNC_CONFIG VALUES('ALERT_ADDRESSES'                ,''                              ,'List of email addresses separated by semicolon')
INSERT INTO SYNC_CONFIG VALUES('ALERT_PHONES'                   ,''                              ,'List of phones like 79857619675 separated by semicolon')
INSERT INTO SYNC_CONFIG VALUES('SMS_PROXY_SERVER'               ,'http://10.67.3.52:9080/DpSmsProxy/sendSms','URL of DpSmsProxy server')
INSERT INTO SYNC_CONFIG VALUES('SMTP_HOST'                      ,'127.0.0.1'                              ,'SMTP Host')
INSERT INTO SYNC_CONFIG VALUES('SMTP_PORT'                      ,'25'                              ,'SMTP Port')
INSERT INTO SYNC_CONFIG VALUES('SMTP_FROM'                      ,'navmonitor'                              ,'Default email address for From in sent notifications')
INSERT INTO SYNC_CONFIG VALUES('SMTP_USER'                      ,''                              ,'SMTP User')
INSERT INTO SYNC_CONFIG VALUES('SMTP_PASSWORD'                  ,''                              ,'SMTP Password')
INSERT INTO SYNC_CONFIG VALUES('FP_ROOT'                        ,'\\\\bronze2\\vol1\\i-navigator','UNC Address of file transporter')
INSERT INTO SYNC_CONFIG VALUES('IS_DB_LOGGING_ENABLED'          ,'true','Enables or disables logging to the database')
INSERT INTO SYNC_CONFIG VALUES('ALPHA_MONITOR_DB'               ,'MIS_IPAD_MONITOR','Database Name used by Alpha Monitor. It should be at same MSSQL Server as used by MONITOR_DB datasource in WebSphere')
INSERT INTO SYNC_CONFIG VALUES('WAITING_AUTOGEN_INTERVAL'        ,'1','If generation not started in this interval, error detected.')
INSERT INTO SYNC_CONFIG VALUES('GENERATOR_MONITORING_URLS'        ,'http://localhost:9090/generator/public/request.do','generator urls for monitoring')
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

--2. Filling list of folders
set identity_insert SYNC_FOLDERS on
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER, SYNC_FOLDER_DESC) values(1,'monitor'   , 1, 'All Monitor Services are running in this service folder')
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
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.EmailSender'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'mailHost'    ,'N','SMTP Host Name'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'mailPort'    ,'N','SMTP Port'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'mailFrom'    ,'N','SMTP Default From'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'mailUser'    ,'N','SMTP User'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'mailPassword','N','SMTP Password'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.SmsSender'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'smsProxyUrl','N','Address of DpSMSProxy'
go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.NotificationService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'alertTransports','N','Alert transports - SMS or SMTP or SMS;SMTP'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'alertAddresses' ,'N','List of emails to send notificatins by email'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'alertPhones'    ,'N','List of phones like 79161111111 to send notifications by SMS'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'databaseName','N','Alpha Monitor database name'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.GeneratorAutoGenCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'waitingIntervalMinuties','N','Waiting interval for error detected'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'urls','N','Generator urls for monitoring'

go
declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out,'ru.sberbank.syncserver2.service.monitor.check.WritePermissionCheck'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id,'includeFolders' ,'N','List of all monitored services'
go

--3. Filing list of services
set identity_insert SYNC_SERVICES on
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE         ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   1              ,'monitor'     , 'monitor'          ,'ru.sberbank.syncserver2.service.monitor.MonitorService'                   ,null            ,null                ,1          , 'This service starts spaceAndTimeCheck and use databaseLogger to save notification to the database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE         ,BEAN_CLASS                                                                 ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   2              ,'monitor'     , 'notifier'          ,'ru.sberbank.syncserver2.service.monitor.NotificationService'             ,null            ,null                ,2          ,'This service reads notifications from database and distributes them. Notifications could be generated by other servers or this server')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   3              ,'monitor'     , 'spaceAndTimeCheck','ru.sberbank.syncserver2.service.monitor.check.SpaceAndLastModifiedCheck'  ,'monitor'       ,'checkAction'       ,3          ,'This service used by "monitor" to check disk space and too old files at file transporter')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   4              ,'monitor'     , 'emailSender'      ,'ru.sberbank.syncserver2.service.monitor.EmailSender'                      ,null            ,null                ,4          ,'This service used by "notifier" to sent notifications by email')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   5              ,'monitor'     , 'smsSender'        ,'ru.sberbank.syncserver2.service.monitor.SmsSender'                        ,null            ,null                ,5          ,'This service used by "notifier" to sent notifications by SMS')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   6              ,'monitor'     , 'databaseLogger'   ,'ru.sberbank.syncserver2.service.monitor.DatabaseNotificationLogger'       ,'monitor'       ,'databaseLogger'    ,6          ,'This service used by "monitor" to save notifications to the database')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   7              ,'monitor'     , 'permissionCheck'  ,'ru.sberbank.syncserver2.service.monitor.check.WritePermissionCheck'  	 ,'monitor'       ,'checkAction'       ,7          ,'This service used by "monitor" to check write permission')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                            ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, BEAN_DESC)
values(                   8              ,'monitor'     , 'autogenCheck'  	,'ru.sberbank.syncserver2.service.monitor.check.GeneratorAutoGenCheck'  	 ,'monitor'       ,'checkAction'       ,8          ,'This service used by "monitor" to check write permission')
go
set identity_insert SYNC_SERVICES off
go

--4. Filling properties
--4.1 For MonitorServuice
exec SP_SYNC_RESET_PROPERTIES 'monitor','monitor'

--4.2. For notification service
exec SP_SYNC_RESET_PROPERTIES 'monitor','notifier'
exec SP_SYNC_SET_PROPERTY     'monitor','notifier'     ,'alertTransports','@ALERT_TRANSPORTS@'
exec SP_SYNC_SET_PROPERTY     'monitor','notifier'     ,'alertAddresses' ,'@ALERT_ADDRESSES@'
exec SP_SYNC_SET_PROPERTY     'monitor','notifier'     ,'alertPhones'    ,'@ALERT_PHONES@'

exec SP_SYNC_RESET_PROPERTIES 'monitor','spaceAndTimeCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','includeFolders' ,'@FP_ROOT@'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','includeMaxMB'   ,'8192'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','includeMaxHours','24'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','excludeFolders' ,''
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','tempFolders'    ,'@FP_ROOT@\\OUT\\temp;@FP_ROOT@\\IN\\temp'
--exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','excludeFolders' ,'@FP_ROOT@\\IN\\prod\\mbr\\mbr_december_for_mis;@FP_ROOT@\\OUT\\temp;@FP_ROOT@\\IN\\temp'
--exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','tempFolders'    ,'@FP_ROOT@\\OUT\\temp;@FP_ROOT@\\IN\\temp'
exec SP_SYNC_SET_PROPERTY     'monitor','spaceAndTimeCheck','tempMaxHours'   ,'24'

exec SP_SYNC_RESET_PROPERTIES 'monitor','emailSender'
exec SP_SYNC_SET_PROPERTY 'monitor','emailSender'    ,'mailHost'         ,'@SMTP_HOST@'
exec SP_SYNC_SET_PROPERTY 'monitor','emailSender'    ,'mailPort'         ,'@SMTP_PORT@'
exec SP_SYNC_SET_PROPERTY 'monitor','emailSender'    ,'mailFrom'         ,'@SMTP_FROM@'
exec SP_SYNC_SET_PROPERTY 'monitor','emailSender'    ,'mailUser'         ,'@SMTP_USER@'
exec SP_SYNC_SET_PROPERTY 'monitor','emailSender'    ,'mailPassword'     ,'@SMTP_PASSWORD@'

exec SP_SYNC_RESET_PROPERTIES 'monitor','smsSender'
exec SP_SYNC_SET_PROPERTY     'monitor','smsSender'  ,'smsProxyUrl'      ,'@SMS_PROXY_SERVER@'

exec SP_SYNC_RESET_PROPERTIES 'monitor','permissionCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','permissionCheck','includeFolders' ,'@FP_ROOT@\\IN;@FP_ROOT@\\OUT'

exec SP_SYNC_RESET_PROPERTIES 'monitor','databaseLogger'
exec SP_SYNC_SET_PROPERTY     'monitor','databaseLogger','databaseName','@ALPHA_MONITOR_DB@'

exec SP_SYNC_RESET_PROPERTIES 'monitor','autogenCheck'
exec SP_SYNC_SET_PROPERTY     'monitor','autogenCheck','waitingIntervalMinuties','@WAITING_AUTOGEN_INTERVAL@'
exec SP_SYNC_SET_PROPERTY     'monitor','autogenCheck','urls','@GENERATOR_MONITORING_URLS@'


go
if object_id('NOTIFICATION_SERVERS') is not null drop table NOTIFICATION_SERVERS
go
CREATE TABLE NOTIFICATION_SERVERS(
  SOURCE_HOST     varchar(256) not null,
  SOURCE_APP      varchar(256) not null,  
  LAST_PING_TIME  datetime     not null 
  unique(SOURCE_HOST, SOURCE_APP)
)
go
INSERT INTO NOTIFICATION_SERVERS VALUES('sbtrpu004','monitor',getdate())
INSERT INTO NOTIFICATION_SERVERS VALUES('imr00sbtiks0090','monitor',getdate())
go

if object_id('NOTIFICATIONS') is not null drop table NOTIFICATIONS
go
CREATE TABLE NOTIFICATIONS(
  NOTIFICATION_ID   int            identity(1,1),
  NOTIFICATION_TYPE char(4)        not null     ,
  NOTIFICATION_TEXT varchar(2048)  not null     ,
  VALID_FROM        datetime       not null     ,
  SOURCE_HOST       varchar(256)   not null     ,
  SOURCE_APP        varchar(256)   not null     ,
  FILE_NAME         varchar(256)       null     ,  
  FILE_MD5          varchar(256)       null     ,  
  TARGET_HOST       varchar(256)       null     ,
  NOTIFY_TIME       datetime           null     ,
  LOCK_TIME         datetime           null     ,
  IS_CANCELLED      char(1)        not null default ('N'),
  DESCRIPTION       varchar(256)       null     ,
)
go
if object_id('NOTIFICATIONS_ARCHIVE') is not null drop table NOTIFICATIONS_ARCHIVE
go
CREATE TABLE NOTIFICATIONS_ARCHIVE(
  NOTIFICATION_ID   int            not null              ,
  NOTIFICATION_TYPE char(4)        not null              ,
  NOTIFICATION_TEXT varchar(2048)  not null              ,
  VALID_FROM        datetime       not null              ,
  SOURCE_HOST       varchar(256)   not null              ,
  SOURCE_APP        varchar(256)   not null              ,
  FILE_NAME         varchar(256)       null              ,           
  FILE_MD5          varchar(256)       null              ,  
  TARGET_HOST       varchar(256)       null              ,
  NOTIFY_TIME       datetime           null              ,
  IS_CANCELLED      char(1)        not null              ,
  DESCRIPTION       varchar(256)       null              ,
)
go
if object_id('SP_PING') is not null drop procedure SP_PING
go
CREATE PROCEDURE SP_PING
  @source_host       varchar(256) , 
  @source_app        varchar(256) 
AS
BEGIN
  SET NOCOUNT ON
  update NOTIFICATION_SERVERS set LAST_PING_TIME=getdate()
  where SOURCE_HOST=@source_host and SOURCE_APP=@source_app
  select @@servername
END
GO

if object_id('SP_ADD_FILE_NOTIFICATION') is not null drop procedure SP_ADD_FILE_NOTIFICATION
go
CREATE PROCEDURE SP_ADD_FILE_NOTIFICATION
  @source_host        varchar(256) , 
  @source_app         varchar(256) ,
  @file_name          varchar(256) ,
  @file_md5           varchar(256) ,
  @target_host        varchar(256) ,
  @notification_text  varchar(2048),
  @valid_from         datetime
AS
BEGIN
  INSERT INTO NOTIFICATIONS(NOTIFICATION_TYPE,  NOTIFICATION_TEXT,  VALID_FROM,  SOURCE_HOST, SOURCE_APP , TARGET_HOST,FILE_NAME ,FILE_MD5)
  VALUES(                   'file'           , @notification_text, @valid_from, @source_host, @source_app,@target_host,@file_name,@file_md5)
END
GO  

if object_id('SP_DEL_FILE_NOTIFICATION') is not null drop procedure SP_DEL_FILE_NOTIFICATION
go
CREATE PROCEDURE SP_DEL_FILE_NOTIFICATION
  @source_host        varchar(256) , 
  @source_app         varchar(256) ,
  @file_name          varchar(256) ,
  @file_md5           varchar(256) ,
  @target_host        varchar(256)   
AS
BEGIN
  SET NOCOUNT ON
  UPDATE NOTIFICATIONS SET IS_CANCELLED='Y'
  WHERE NOTIFICATION_TYPE='file' 
    and (SOURCE_HOST='localhost' or SOURCE_HOST=isnull(@source_host,SOURCE_HOST))
    and SOURCE_APP=isnull(@source_app,SOURCE_APP)
    and FILE_NAME=@file_name and file_md5=@file_md5 
    and (TARGET_HOST='localhost' or target_host=isnull(@target_host,target_host))
END
GO  

if object_id('SP_ADD_ERROR') is not null drop procedure SP_ADD_ERROR
go
CREATE PROCEDURE SP_ADD_ERROR
  @source_host        varchar(256) , 
  @source_app         varchar(256) ,
  @notification_text  varchar(2048)
AS
  set nocount on
  declare @valid_from datetime
  select @valid_from=getdate()
  exec SP_ADD_FILE_NOTIFICATION @source_host, @source_app, null, 'error', null, @notification_text, @valid_from
  select 'true'
GO

if object_id('SP_ADD_FILE_GEN_NOTIFICATION') is not null drop procedure SP_ADD_FILE_GEN_NOTIFICATION
go
CREATE PROCEDURE SP_ADD_FILE_GEN_NOTIFICATION
  @source_host        varchar(256) , 
  @source_app         varchar(256) ,
  @file_name          varchar(256) 
AS
BEGIN
  --1. Composing notification message and valid_from time
  set nocount on
  declare @notification_text varchar(2048)
  set @notification_text='File '+@file_name+' is not generated in time by '+@source_app+' at '+@source_host
  declare @valid_from datetime
  select @valid_from=dateadd(second,generation_seconds,getdate()) 
  from SYNC_CACHE_STATIC_FILES
  where lower(file_name)=lower(@file_name)
  if @valid_from is null begin
    set @notification_text='File '+@file_name+' is not found in table SYNC_CACHE_STATIC_FILES in database '+db_name()+' at server '+@@servername
    set @valid_from=getdate()
  end

  --2. Adding future notification
  exec SP_ADD_FILE_NOTIFICATION @source_host, @source_app, @file_name, 'generation-md5', 'generator-host', @notification_text, @valid_from
END 
GO  

if object_id('SP_DEL_FILE_GEN_NOTIFICATION') is not null drop procedure SP_DEL_FILE_GEN_NOTIFICATION
go
CREATE PROCEDURE SP_DEL_FILE_GEN_NOTIFICATION
  @source_host        varchar(256) , 
  @source_app         varchar(256) ,
  @file_name          varchar(256) 
AS
  exec SP_DEL_FILE_NOTIFICATION @source_host, @source_app, @file_name, 'generation-md5', 'generator-host'
GO

if object_id('SP_ADD_FILE_MOV_NOTIFICATION') is not null drop procedure SP_ADD_FILE_MOV_NOTIFICATION
go
CREATE PROCEDURE SP_ADD_FILE_MOV_NOTIFICATION
  @source_host        varchar(256) , 
  @source_app         varchar(256) ,
  @file_name          varchar(256) ,
  @file_md5           varchar(256) ,
  @target_host        varchar(256) 
AS
BEGIN
  --1. Composing notification message and valid_from time
  set nocount on
  declare @notification_text varchar(2048)
  set @notification_text='File '+@file_name+' is not delivered to  in 30 minutes '+' to server '+@target_host
  declare @valid_from datetime
  select @valid_from=dateadd(second,30*60,getdate()) 

  --2. Adding future notification
  exec SP_ADD_FILE_NOTIFICATION @source_host, @source_app, @file_name, @file_md5, @target_host, @notification_text, @valid_from
END
GO

if object_id('SP_DEL_FILE_MOV_NOTIFICATION') is not null drop procedure SP_DEL_FILE_MOV_NOTIFICATION
go
CREATE PROCEDURE SP_DEL_FILE_MOV_NOTIFICATION
  @file_name          varchar(256) ,
  @file_md5           varchar(256) ,
  @target_host        varchar(256) 
AS
  set nocount on
  exec SP_DEL_FILE_NOTIFICATION null, null, @file_name, @file_md5, @target_host
  select @@servername -- we should return something for datapower
GO


if object_id('SP_LIST_NOTIFICATIONS') is not null drop procedure SP_LIST_NOTIFICATIONS
go
CREATE PROCEDURE SP_LIST_NOTIFICATIONS
  @client_host varchar(256)
AS
  --1. Creating temp table
  set nocount on
  if object_id('tempdb..#list') is not null drop table #list
  create table #list(
    notification_id   int          , 
    notification_text varchar(2048),
    source_host       varchar(256) , 
    source_app        varchar(256)
  )

  --2. Collecting ping notificatio
  insert into #list(notification_id, notification_text, source_host, source_app)
  select distinct null, 'Monitor at '+@client_host+' did not find a ping from '+source_host+' (see database '+db_name()+' at server '+@@servername+')',source_host,source_app
  from NOTIFICATION_SERVERS
  where LAST_PING_TIME<dateadd(second,-900, getdate())
  
  --3. Collecting file notifications and marking them in LOCK_TIME as to be sent by current process
    insert into #list(notification_id, notification_text, source_host, source_app)
    select            notification_id, notification_text, source_host, source_app
    from notifications 
    where valid_from<dateadd(second,-60, getdate()) and (lock_time is null or lock_time<dateadd(second,-300, getdate()))

    update notifications set lock_time=getdate() where notification_id in (select notification_id from #list)

  --4. Archiving cancelled notifications
    INSERT INTO NOTIFICATIONS_ARCHIVE
             ([NOTIFICATION_ID]
             ,[NOTIFICATION_TYPE]
             ,[NOTIFICATION_TEXT]
             ,[VALID_FROM]
             ,[SOURCE_HOST]
             ,[SOURCE_APP]
             ,[FILE_NAME]
             ,[FILE_MD5]
             ,[TARGET_HOST]
             ,[NOTIFY_TIME]
             ,[DESCRIPTION]
             ,IS_CANCELLED
             )
    SELECT [NOTIFICATION_ID]
             ,[NOTIFICATION_TYPE]
             ,[NOTIFICATION_TEXT]
             ,[VALID_FROM]
             ,[SOURCE_HOST]
             ,[SOURCE_APP]
             ,[FILE_NAME]
             ,[FILE_MD5]
             ,[TARGET_HOST]
             ,[NOTIFY_TIME]
             ,[DESCRIPTION]
             ,IS_CANCELLED
    FROM NOTIFICATIONS
    WHERE IS_CANCELLED='Y' or NOTIFY_TIME is not null

    DELETE FROM NOTIFICATIONS WHERE NOTIFICATION_ID in (SELECT NOTIFICATION_ID FROM NOTIFICATIONS_ARCHIVE)
   
  --5. Return list of all notifications
  SELECT NOTIFICATION_ID, NOTIFICATION_TEXT,SOURCE_HOST,SOURCE_APP FROM #list
GO 

if object_id('SP_SET_NOFIFY_TIME') is not null drop procedure SP_SET_NOFIFY_TIME
go
CREATE PROCEDURE SP_SET_NOFIFY_TIME
  @notification_id int         ,
  @source_host     varchar(256),
  @source_app      varchar(256)
AS  
  set nocount on
  if @notification_id is not null and @notification_id>0 begin
    UPDATE NOTIFICATIONS SET NOTIFY_TIME = GETDATE() WHERE NOTIFICATION_ID=@notification_id
  end else begin
    UPDATE NOTIFICATION_SERVERS SET LAST_PING_TIME = GETDATE() WHERE SOURCE_HOST=@source_host and SOURCE_APP=@source_app
  end
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