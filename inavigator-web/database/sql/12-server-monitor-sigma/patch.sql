INSERT INTO SYNC_CONFIG VALUES('DATAPOWER_CONVERSION'                  ,'CONVERT_TO_NEW_SIGMA' ,'Defines DataPower compatibility mode. Temporary hack to avoid waiting until datapower is updated.')
go
declare @id int
select @id=list_template_id from PROPERTY_LIST_TEMPLATES where LIST_TEMPLATE_CODE='ru.sberbank.syncserver2.service.sql.DataPowerService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'conversion'     ,'N','This parameter used to provide compatibility of DataPower'
go
exec SP_SYNC_SET_PROPERTY     'monitor','datapowerService','conversion'      ,'@DATAPOWER_CONVERSION@'
go
