
if OBJECT_ID('SP_SYNC_GET_GENERATION_LOGS') is not null drop procedure SP_SYNC_GET_GENERATION_LOGS
go
create procedure SP_SYNC_GET_GENERATION_LOGS(
	@fileName			varchar(100),
	@date				dateTime,
	@debug				bit,
	@startIndex			int,
	@numberOfRecords	int
)
as
begin
	set nocount on
  if object_id('tempdb..#skip') is not null drop table #skip
  create table #skip(
	EVENT_TYPE varchar(50)
  )
  insert into #skip values ('OTHER')
  if @debug=0 insert into #skip 
						select 'DEBUG' union 
						select 'GEN_DEBUG' union 
						select 'GEN_GENERATION_PROGRESS'

  declare @start_id int
  declare @second_id int

  select @start_id = EVENT_ID from (
		select ROW_NUMBER() OVER (order by EVENT_ID asc) as rownumber, EVENT_ID from SYNC_LOGS WHERE EVENT_TIME>@date AND EVENT_INFO=@fileName AND EVENT_TYPE='GEN_QUEUED'
  ) as foo where rownumber = 1

  select @second_id = EVENT_ID from (
		select ROW_NUMBER() OVER (order by EVENT_ID asc) as rownumber, EVENT_ID from SYNC_LOGS WHERE EVENT_TIME>@date AND EVENT_INFO=@fileName AND EVENT_TYPE='GEN_QUEUED'
  ) as foo where rownumber = 2

  if (@second_id is null) 
	select top 1 @second_id = EVENT_ID from SYNC_LOGS order by EVENT_ID desc
  else
	set @second_id = @second_id - 1

  if object_id('tempdb..#records') is not null drop table #records
  select * into #records from SYNC_LOGS WHERE EVENT_INFO=@fileName AND 
											EVENT_TYPE not in (select EVENT_TYPE from #skip) AND 
											EVENT_ID >= @start_id AND 
											EVENT_ID <= @second_id
  if (@debug=0) 
	insert into #records select top 1 
	   [SERVER_EVENT_ID]
      ,[EVENT_TIME]
      ,[USER_EMAIL]
      ,[EVENT_TYPE]
      ,[START_SERVER_EVENT_ID]
      ,[EVENT_DESC]
      ,[CLIENT_IP_ADDRESS]
      ,[WEB_HOST_NAME]
      ,[WEB_APP_NAME]
      ,[DISTRIB_SERVER]
      ,[EVENT_INFO]
      ,[ERROR_STACK_TRACE]
      ,[CLIENT_EVENT_ID]
      ,[CLIENT_DEVICE_ID] from SYNC_LOGS WHERE EVENT_INFO=@fileName AND 
												EVENT_TYPE='GEN_GENERATION_PROGRESS' AND 
												EVENT_ID >= @start_id AND 
												EVENT_ID <= @second_id 
										order by EVENT_ID desc
  if (@numberOfRecords < 0)
		select * from #records order by EVENT_TIME asc
  else begin
		select * from (select ROW_NUMBER() over(order by EVENT_TIME asc) rownumber,* from #records) foo 
				where rownumber between (@startIndex+1) and (@startIndex+@numberOfRecords) order by EVENT_TIME asc
		select count(*) as count from #records
  end
end
go



