
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
