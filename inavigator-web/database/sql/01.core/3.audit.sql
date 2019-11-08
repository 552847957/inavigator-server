

if object_id('SYNC_AUDIT') is not null DROP TABLE SYNC_AUDIT
go

CREATE TABLE SYNC_AUDIT(
  EVENT_ID INT IDENTITY(1,1) PRIMARY KEY,
  EVENT_TIME DATETIME NOT NULL,
  HOST VARCHAR(50) NULL,
  IP_ADDRESS VARCHAR(50) NULL,
  USER_EMAIL VARCHAR(50) NULL,
  MODULE VARCHAR(50) NULL,
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
  @module varchar(50),
  @event_type nvarchar(50),
  @event_desc nvarchar(max),
  @code int,
  @host varchar(50)
as
    set nocount on
    INSERT INTO SYNC_AUDIT
           (EVENT_TIME
		   ,HOST
		   ,IP_ADDRESS
           ,USER_EMAIL
           ,MODULE
           ,EVENT_TYPE
           ,EVENT_DESC
           ,CODE)
     VALUES
           (GETDATE()
		   ,@host
		   ,@ip_address
           ,@user_email
           ,@module
           ,@event_type
           ,@event_desc
		   ,@code)
	SELECT @@IDENTITY;
 GO
 
