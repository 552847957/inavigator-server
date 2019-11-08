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
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'Passport', '1.0'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '1.0', 'offlineServer1','https://prkb1.mobile.sbrf.ru:9443/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '1.0', 'offlineServer2','https://prkb2.mobile.sbrf.ru:9443/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '1.0', 'loggingServer' ,''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '1.0', 'pushServer1' ,''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '1.0', 'pushServer2' ,''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '1.0', 'statisticsKey',''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '1.0', 'statisticsServer',''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '1.0', 'versionStatus',''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '1.0', 'versionMessage',''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '1.0', 'distribServer',''
go

--1. Options for iNavigator
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'Passport', '02.000.00'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '02.000.00', 'offlineServer1','https://prkb1.mobile.sbrf.ru:9443/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '02.000.00', 'offlineServer2','https://prkb2.mobile.sbrf.ru:9443/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '02.000.00', 'loggingServer' ,''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '02.000.00', 'pushServer1' ,''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '02.000.00', 'pushServer2' ,''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '02.000.00', 'statisticsKey',''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '02.000.00', 'statisticsServer',''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '02.000.00', 'versionStatus',''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '02.000.00', 'versionMessage',''
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'Passport', '02.000.00', 'distribServer',''
go

