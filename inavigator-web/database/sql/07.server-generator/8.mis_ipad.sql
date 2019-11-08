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

