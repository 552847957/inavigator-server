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
  end
GO

if object_id('SP_GET_HOSTS_DO_NOT_PING') is not null drop procedure SP_GET_HOSTS_DO_NOT_PING
go
CREATE PROCEDURE SP_GET_HOSTS_DO_NOT_PING
  @time_interval int
AS  
  set nocount on
  SELECT DISTINCT SOURCE_HOST FROM NOTIFICATION_SERVERS WHERE LAST_PING_TIME < dateadd(second, -@time_interval, getdate()) 
GO

