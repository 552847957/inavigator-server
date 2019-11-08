if object_id('SYNC_MOBILE_LOG') is not null DROP TABLE SYNC_MOBILE_LOG
go

CREATE TABLE SYNC_MOBILE_LOG(
  DEVICE_INFO_SOURCE_ID VARCHAR(512) NULL,
  DEVICE_MODEL VARCHAR(512) NULL,
  IOS_VERSION VARCHAR(512) NULL,
  APP_VERSION VARCHAR(512) NULL,
  BUNDLE_ID VARCHAR(512) NULL,
  UPDATE_TIME VARCHAR(512) NULL,

  EVENT_SOURCE_ID VARCHAR(512) NULL,
  EVENT_TIME datetime NULL,
  TIME_ZONE VARCHAR(512) NULL,
  USER_EMAIL VARCHAR(512) NULL,
  EVENT_TYPE VARCHAR(512) NULL,
  EVENT_DESC VARCHAR(512) NULL,
  IP_ADDRESS VARCHAR(512) NULL,
  DATA_SERVER VARCHAR(512) NULL,
  DISTRIB_SERVER VARCHAR(512) NULL,
  EVENT_INFO VARCHAR(512) NULL,
  ERROR_STACK_TRACE VARCHAR(512) NULL,
  CONFIGURATION_SERVER VARCHAR(512) NULL
)
GO

if object_id('SYNC_MOBILE_MODE') is not null DROP TABLE SYNC_MOBILE_MODE
go

CREATE TABLE SYNC_MOBILE_MODE(
  USER_EMAIL VARCHAR(50) NOT NULL,
  DEVICE_MODEL VARCHAR(100) NULL,
  MODE BIT NOT NULL
)
GO

if object_id('SP_SYNC_MOBILE_SET_MODE') is not null DROP PROCEDURE SP_SYNC_MOBILE_SET_MODE
go

CREATE PROCEDURE SP_SYNC_MOBILE_SET_MODE
	@user_email varchar(100),
	@device varchar(100),
	@mode BIT
as
  begin tran
    set nocount on
	if exists(select * from SYNC_MOBILE_MODE where lower(USER_EMAIL)=lower(@user_email))
	begin
		update SYNC_MOBILE_MODE set MODE=@mode where lower(USER_EMAIL)=lower(@user_email)
	end
	else
	begin		
    INSERT INTO SYNC_MOBILE_MODE
           (USER_EMAIL,
			DEVICE_MODEL,
			MODE)
     VALUES
           (@user_email,
			@device,
			@mode)
	end
  commit
 GO

if object_id('SP_SYNC_MOBILE_LOG') is not null DROP PROCEDURE SP_SYNC_MOBILE_LOG
go

CREATE PROCEDURE SP_SYNC_MOBILE_LOG
	@device_info_source_id varchar(512),
	@device_model varchar(512),
	@ios_version varchar(512),
	@app_version varchar(512),
	@bundle_id varchar(512),
	@update_time varchar(512),

	@event_source_id varchar(512),
	@event_time datetime,
	@time_zone varchar(512),
	@user_email varchar(512),
	@event_type varchar(512),
	@event_desc varchar(512),
	@ip_address varchar(512),
	@data_server varchar(512),
	@distrib_server varchar(512),
	@event_info varchar(512),
	@error_stack_trace varchar(512),
	@configuration_server varchar(512)
as
  begin tran
    set nocount on
    INSERT INTO SYNC_MOBILE_LOG
           (DEVICE_INFO_SOURCE_ID,
			DEVICE_MODEL,
			IOS_VERSION,
			APP_VERSION,
			BUNDLE_ID,
			UPDATE_TIME,

			EVENT_SOURCE_ID,
			EVENT_TIME,
			TIME_ZONE,
			USER_EMAIL,
			EVENT_TYPE,
			EVENT_DESC,
			IP_ADDRESS,
			DATA_SERVER,
			DISTRIB_SERVER,
			EVENT_INFO,
			ERROR_STACK_TRACE,
			CONFIGURATION_SERVER)
     VALUES
           (@device_info_source_id,
			@device_model,
			@ios_version,
			@app_version,
			@bundle_id,
			@update_time,

			@event_source_id,
			@event_time,
			@time_zone,
			@user_email,
			@event_type,
			@event_desc,
			@ip_address,
			@data_server,
			@distrib_server,
			@event_info,
			@error_stack_trace,
			@configuration_server)
  commit
 GO

