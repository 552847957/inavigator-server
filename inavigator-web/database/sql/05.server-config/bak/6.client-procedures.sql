/*
TODO: check it


������,

� ������� ����� ��������� ��� ������ � ����������� �����������.

��� ��� ����� ConfigurationService, ����������� �  config.do

�� ������ ��������� ��� ���������: config.do?appBundle=balance&appVersion=1.0

� ���������� xml ����, ����������� �� ����������� ���������� exec SP_SYNC_LIST_CLIENT_PROPERTIES @appBundle, @appVersion

<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<properties>
<property><name>Server1</name><value>https://10.21.138.60:9443/mis_syncserver_mirrow</value></property>
<property><name>Server2</name><value>https://10.21.138.60:9443/mis_syncserver_mirrow</value></property>
</properties>

*/

if object_id('SP_SYNC_LIST_CLIENT_APPS') is not null DROP PROCEDURE SP_SYNC_LIST_CLIENT_APPS
go
if object_id('SP_SYNC_LIST_CLIENT_VERSIONS') is not null DROP PROCEDURE SP_SYNC_LIST_CLIENT_VERSIONS
go
if object_id('SP_SYNC_LIST_CLIENT_PROPERTIES') is not null DROP PROCEDURE SP_SYNC_LIST_CLIENT_PROPERTIES
go
if object_id('SP_SYNC_RESET_CLIENT_PROPERTIES') is not null DROP PROCEDURE SP_SYNC_RESET_CLIENT_PROPERTIES
go
if object_id('SP_SYNC_SET_CLIENT_PROPERTY') is not null DROP PROCEDURE SP_SYNC_SET_CLIENT_PROPERTY
go
if object_id('SP_SYNC_ADD_CLIENT_VERSION') is not null DROP PROCEDURE SP_SYNC_ADD_CLIENT_VERSION
go
if object_id('SP_SYNC_DELETE_CLIENT_VERSION') is not null DROP PROCEDURE SP_SYNC_DELETE_CLIENT_VERSION
go
CREATE PROCEDURE SP_SYNC_LIST_CLIENT_APPS
as
  select APP_BUNDLE from CONFSERVER_APPS ORDER BY 1
go
CREATE PROCEDURE SP_SYNC_LIST_CLIENT_VERSIONS
  @app_bundle       varchar(256)
as
  select APP_VERSION
  from CONFSERVER_VERSIONS
  where app_id in (select app_id from CONFSERVER_APPS where app_bundle=@app_bundle)
go
CREATE PROCEDURE SP_SYNC_LIST_CLIENT_PROPERTIES
  @app_bundle       varchar(256),
  @app_version      varchar(256)
as
  select property_code property, property_value value
  from CONFSERVER_PROPERTY_VALUES
  where app_id in (select app_id from CONFSERVER_APPS where app_bundle=@app_bundle)
    and app_version=@app_version
GO

CREATE PROCEDURE SP_SYNC_RESET_CLIENT_PROPERTIES
  @app_bundle       varchar(256),
  @app_version      varchar(256)
AS
  --1. Identify @app_id
  set nocount on
  declare @app_id int
  select @app_id = app_id from CONFSERVER_APPS where app_bundle=@app_bundle
  if @app_id is null return

  --2. Deleting old property values
  delete from CONFSERVER_PROPERTY_VALUES
  where app_id=@app_id
    and app_version=@app_version

  --3. Add version if required
  delete from CONFSERVER_VERSIONS where app_id=@app_id and app_version=@app_version 
  insert into CONFSERVER_VERSIONS(APP_ID, APP_VERSION) values(@app_id,@app_version)


  --4. Insert new property values
  insert into CONFSERVER_PROPERTY_VALUES(APP_ID, PROPERTY_CODE, APP_VERSION , PROPERTY_VALUE)
  select                                @app_id, PROPERTY_CODE, @app_version, ' '
  from CONFSERVER_PROPERTY_TEMPLATES
  where app_id=@app_id
GO

CREATE PROCEDURE SP_SYNC_SET_CLIENT_PROPERTY
  @app_bundle       varchar(256) ,
  @app_version      varchar(256) ,
  @property_code    varchar(256) ,
  @value            varchar(1024)
as
  --1. Identify @app_id
  set nocount on
  declare @app_id int
  select @app_id = app_id from CONFSERVER_APPS where app_bundle=@app_bundle
  if @app_id is null return

  --2. Updating
  update CONFSERVER_PROPERTY_VALUES set property_value=@value
  where app_id=@app_id and app_version=@app_version and property_code=@property_code
GO

CREATE PROCEDURE SP_SYNC_ADD_CLIENT_VERSION
  @app_bundle       varchar(256),
  @app_version      varchar(256)
AS
  --1. Identify @app_id
  set nocount on
  declare @app_id int
  select @app_id = app_id from CONFSERVER_APPS where app_bundle=@app_bundle
  if @app_id is null return

  --2. Deleting old property values
  if not exists (select * from CONFSERVER_VERSIONS where app_id=@app_id and app_version=@app_version) begin
    --2.1. Insert version
    insert into CONFSERVER_VERSIONS(APP_ID, APP_VERSION) values(@app_id,@app_version)

    --2.2. Insert properties
    delete from CONFSERVER_PROPERTY_VALUES where app_id=@app_id and app_version=@app_version 
    insert into CONFSERVER_PROPERTY_VALUES(APP_ID, PROPERTY_CODE, APP_VERSION , PROPERTY_VALUE)
    select                                @app_id, PROPERTY_CODE, @app_version, ' '
    from CONFSERVER_PROPERTY_TEMPLATES
    where app_id=@app_id
  end
GO

CREATE PROCEDURE SP_SYNC_DELETE_CLIENT_VERSION
  @app_bundle       varchar(256),
  @app_version      varchar(256)
AS
  --1. Identify @app_id
  set nocount on
  declare @app_id int
  select @app_id = app_id from CONFSERVER_APPS where app_bundle=@app_bundle
  if @app_id is null return

  --2. Deleting old property values
  delete from CONFSERVER_PROPERTY_VALUES
  where app_id=@app_id
    and app_version=@app_version

  --3. Add version if required
  delete from CONFSERVER_VERSIONS where app_id=@app_id and app_version=@app_version 
GO


--1. Options for iNavigator
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'iNavigator', '1.0'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator', '1.0', 'offlineServer1','https://i-navigator.sbrf.ru/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator', '1.0', 'offlineServer2','https://i-navigator.sbrf.ru/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator', '1.0', 'onlineServer1' ,'https://i-navigator.sbrf.ru/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator', '1.0', 'onlineServer2' ,'https://i-navigator.sbrf.ru/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator', '1.0', 'onlineService','finik2-new'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator', '1.0', 'onlineProvider','DATAPOWER'
go

--2. Options for Balance
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'balance', '1.0'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'balance', '1.0', 'offlineServer1','https://i-navigator.sbrf.ru/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'balance', '1.0', 'offlineServer2','https://i-navigator.sbrf.ru/syncserver/single'
go

--3. Options for competitors
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'competitors', '1.0'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'competitors', '1.0', 'offlineServer1','https://i-navigator.sbrf.ru/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'competitors', '1.0', 'offlineServer2','https://i-navigator.sbrf.ru/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'competitors', '1.0', 'versionStatus' ,'DEPRECATED'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'competitors', '1.0', 'versionMessage','This version is deprecated, please go to Afaria and update'
go


--4. Options for iRubricator
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'iRubricator', '1.0'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iRubricator', '1.0', 'offlineServer1','https://i-navigator.sbrf.ru/syncserver/changeset'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iRubricator', '1.0', 'offlineServer2','https://i-navigator.sbrf.ru/syncserver/changeset'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iRubricator', '1.0', 'onlineServer1' ,'https://i-navigator.sbrf.ru/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iRubricator', '1.0', 'onlineServer2' ,'https://i-navigator.sbrf.ru/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iRubricator', '1.0', 'onlineService','finik2-new'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iRubricator', '1.0', 'onlineProvider','DATAPOWER'
go

--5. Options for Dynamic Model
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'DynamicModel', '1.0'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'DynamicModel', '1.0', 'onlineServer1','https://i-navigator.sbrf.ru/syncserver/online'
go                                    
exec SP_SYNC_SET_CLIENT_PROPERTY 'DynamicModel', '1.0', 'onlineServer2','https://i-navigator.sbrf.ru/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'DynamicModel', '1.0', 'onlineService','finik1-new'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'DynamicModel', '1.0', 'onlineProvider','DATAPOWER'
go

--6. Options for iSalesManagementHead
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'iSalesManagementHead', '1.0'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iSalesManagementHead', '1.0', 'offlineServer1','https://i-navigator.sbrf.ru/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iSalesManagementHead', '1.0', 'offlineServer2','https://i-navigator.sbrf.ru/syncserver/single'
go

--7. Options for iSalesManagementReg
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'iSalesManagementReg', '1.0'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iSalesManagementReg', '1.0', 'offlineServer1','https://i-navigator.sbrf.ru/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iSalesManagementReg', '1.0', 'offlineServer2','https://i-navigator.sbrf.ru/syncserver/single'
go


--8. Options for MISMobile
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'MISMobile', '1.0'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '1.0', 'offlineServer1','https://i-navigator.sbrf.ru/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '1.0', 'offlineServer2','https://i-navigator.sbrf.ru/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '1.0', 'onlineServer1' ,'https://i-navigator.sbrf.ru/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '1.0', 'onlineServer2' ,'https://i-navigator.sbrf.ru/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '1.0', 'onlineService','finik2-new'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '1.0', 'onlineProvider','DATAPOWER'
go


--9. Options for KPIPPR
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'KPIPPR', '1.0'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'KPIPPR'    , '1.0', 'offlineServer1','https://i-navigator.sbrf.ru/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'KPIPPR'    , '1.0', 'offlineServer2','https://i-navigator.sbrf.ru/syncserver/single'
go


--10. Options for Passport
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'Passport', '1.0'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '1.0', 'offlineServer1','https://i-navigator.sbrf.ru/psyncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '1.0', 'offlineServer2','https://i-navigator.sbrf.ru/psyncserver/single'
go
