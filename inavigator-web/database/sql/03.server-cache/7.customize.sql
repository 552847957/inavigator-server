
if object_id('SP_CONFIGURE_DEL_PUSH_SERVICES') is not null drop procedure SP_CONFIGURE_DEL_PUSH_SERVICES
go
if object_id('SP_CONFIGURE_PUSH_SERVICES_ONLY') is not null drop procedure SP_CONFIGURE_PUSH_SERVICES_ONLY
go
if object_id('SP_CONFIGURE_PUSH_SENDERS_ONLY') is not null drop procedure SP_CONFIGURE_PUSH_SENDERS_ONLY
go
if object_id('SP_CONFIGURE_DEL_PUSH_SENDERS') is not null drop procedure SP_CONFIGURE_DEL_PUSH_SENDERS
go
if object_id('SP_CONFIGURE_SERVICES') is not null drop procedure SP_CONFIGURE_SERVICES
go
if type_id('primitivList') is not null drop type primitivList
go
CREATE TYPE primitivList AS TABLE   
(
	value VARCHAR(256)
)  
GO
CREATE PROCEDURE SP_CONFIGURE_SERVICES
	@list_codes primitivList readonly,
	@not bit
as
	set nocount on
	declare @sync_folder_code varchar(256)
	declare @bean_code varchar(256)
	declare cur CURSOR LOCAL for select SYNC_FOLDER_CODE, BEAN_CODE from SYNC_SERVICES
	open cur
	fetch next from cur into @sync_folder_code, @bean_code

	declare @list_code  varchar(512)
	while @@FETCH_STATUS = 0 BEGIN
		if (
			((@not=0) and (@bean_code in (select value from @list_codes)))
			OR
			((@not=1) and (@bean_code not in (select value from @list_codes)))
			)
		begin		
			exec SP_SYNC_COMPOSE_PROPERTY_LIST_CODE @sync_folder_code,@bean_code,@list_code out

			--Delete
			delete from PROPERTY_VALUES where LIST_ID in (select LIST_ID from PROPERTY_LISTS where LIST_CODE=@list_code)
			delete from PROPERTY_LISTS where LIST_CODE=@list_code
			delete from SYNC_SERVICES where BEAN_CODE=@bean_code
		end
	    fetch next from cur into @sync_folder_code, @bean_code
	END
	close cur
	deallocate cur
	delete from SYNC_FOLDERS where SYNC_FOLDER_CODE not in (select distinct SYNC_FOLDER_CODE from SYNC_SERVICES)
GO

CREATE PROCEDURE SP_CONFIGURE_PUSH_SERVICES_ONLY
as
	declare @services primitivList
	insert into @services values ('pushNotificationUploader'),('pushNotificationService'),('xmlPublicService'),('AppleSender'), ('misDataPowerService'), ('clusterManager')
	exec SP_CONFIGURE_SERVICES @services, true
GO

CREATE PROCEDURE SP_CONFIGURE_DEL_PUSH_SERVICES
as
	declare @services primitivList
	insert into @services values ('pushNotificationUploader'),('pushNotificationService'),('xmlPublicService'),('AppleSender')
	exec SP_CONFIGURE_SERVICES @services, false
GO

CREATE PROCEDURE SP_CONFIGURE_PUSH_SENDERS_ONLY
as
	declare @services primitivList
	insert into @services values ('pushNotificationService'),('AppleSender'),('clusterManager')
	exec SP_CONFIGURE_SERVICES @services, true
GO

CREATE PROCEDURE SP_CONFIGURE_DEL_PUSH_SENDERS
as
	declare @services primitivList
	insert into @services values ('pushNotificationService'),('AppleSender')
	exec SP_CONFIGURE_SERVICES @services, false
GO