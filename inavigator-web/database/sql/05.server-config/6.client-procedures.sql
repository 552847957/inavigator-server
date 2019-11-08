/*
TODO: check it


������,

� ������� ����� ��������� ��� ������ � ����������� �����������.

��� ��� ����� ConfigurationService, ����������� �  config.do

�� ������ ��������� ��� ���������: config.do?appBundle=balance&appVersion=02.000.00

� ���������� xml ����, ����������� �� ����������� ���������� exec SP_SYNC_LIST_CLIENT_PROPERTIES @appBundle, @appVersion

<?xml version="02.000.00" encoding="UTF-8" standalone="yes"?>
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
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'iNavigator', '02.000.00'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator', '02.000.00', 'offlineServer1','https://inavigator1.mobile.sbrf.ru:9911/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator', '02.000.00', 'offlineServer2','https://inavigator1.mobile.sbrf.ru:9912/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator', '02.000.00', 'onlineServer1' ,'https://inavigator1.mobile.sbrf.ru:9921/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator', '02.000.00', 'onlineServer2' ,'https://inavigator1.mobile.sbrf.ru:9922/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator', '02.000.00', 'onlineService' ,'finik1-new'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator', '02.000.00', 'onlineProvider','DATAPOWER'
go

--2. Options for Balance
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'balance', '02.000.00'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'balance', '02.000.00', 'offlineServer1','https://inavigator1.mobile.sbrf.ru:9913/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'balance', '02.000.00', 'offlineServer2','https://inavigator1.mobile.sbrf.ru:9914/syncserver/single'
go

--3. Options for competitors
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'competitors', '02.000.00'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'competitors', '02.000.00', 'offlineServer1','https://inavigator1.mobile.sbrf.ru:9913/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'competitors', '02.000.00', 'offlineServer2','https://inavigator1.mobile.sbrf.ru:9914/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'competitors', '02.000.00', 'loggingServer','https://inavigator1.mobile.sbrf.ru:9914/confserver/public/request.do'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'competitors', '02.000.00', 'statisticsKey','b0c7cc5279a410bfcd2ffc9cd1025b29ca33c4e2'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'competitors', '02.000.00', 'statisticsServer','https://inavigator1.mobile.sbrf.ru:9914/ss'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'competitors', '02.000.00', 'pushServer1','https://inavigator1.mobile.sbrf.ru:9914/syncserver/public/request.do'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'competitors', '02.000.00', 'pushServer2','https://inavigator1.mobile.sbrf.ru:9914/syncserver/public/request.do'
go

--4. Options for iRubricator
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'iRubricator', '02.000.00'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iRubricator', '02.000.00', 'offlineServer1','https://inavigator1.mobile.sbrf.ru:9913/syncserver/changeset'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iRubricator', '02.000.00', 'offlineServer2','https://inavigator1.mobile.sbrf.ru:9914/syncserver/changeset'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iRubricator', '02.000.00', 'onlineServer1' ,'https://inavigator1.mobile.sbrf.ru:9921/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iRubricator', '02.000.00', 'onlineServer2' ,'https://inavigator1.mobile.sbrf.ru:9922/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iRubricator', '02.000.00', 'onlineService','finik2-new'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iRubricator', '02.000.00', 'onlineProvider','DATAPOWER'
go

--5. Options for Dynamic Model
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'DynamicModel', '02.000.00'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'DynamicModel', '02.000.00', 'onlineServer1','https://inavigator1.mobile.sbrf.ru:9921/syncserver/online'
go                                    
exec SP_SYNC_SET_CLIENT_PROPERTY 'DynamicModel', '02.000.00', 'onlineServer2','https://inavigator1.mobile.sbrf.ru:9922/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'DynamicModel', '02.000.00', 'onlineService','finik1-new'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'DynamicModel', '02.000.00', 'onlineProvider','DATAPOWER'
go

--6. Options for iSalesManagementHead
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'iSalesManagementHead', '02.000.00'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iSalesManagementHead', '02.000.00', 'offlineServer1','https://inavigator1.mobile.sbrf.ru:9915/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iSalesManagementHead', '02.000.00', 'offlineServer2','https://inavigator1.mobile.sbrf.ru:9916/syncserver/single'
go

--7. Options for iSalesManagementReg
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'iSalesManagementReg', '02.000.00'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iSalesManagementReg', '02.000.00', 'offlineServer1','https://inavigator1.mobile.sbrf.ru:9915/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iSalesManagementReg', '02.000.00', 'offlineServer2','https://inavigator1.mobile.sbrf.ru:9916/syncserver/single'
go

--8. Options for iup_rb
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'iup_rb', '02.000.00'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iup_rb', '02.000.00', 'offlineServer1','https://inavigator1.mobile.sbrf.ru:9915/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iup_rb', '02.000.00', 'offlineServer2','https://inavigator1.mobile.sbrf.ru:9916/syncserver/single'
go


--9. Options for MISMobile
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'MISMobile', '02.000.00'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '02.000.00', 'offlineServer1','https://inavigator1.mobile.sbrf.ru:9917/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '02.000.00', 'offlineServer2','https://inavigator1.mobile.sbrf.ru:9918/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '02.000.00', 'offlineServer3','https://inavigator1.mobile.sbrf.ru:9919/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '02.000.00', 'offlineServer4','https://inavigator1.mobile.sbrf.ru:9920/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '02.000.00', 'onlineServer1' ,'https://inavigator1.mobile.sbrf.ru:9921/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '02.000.00', 'onlineServer2' ,'https://inavigator1.mobile.sbrf.ru:9922/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '02.000.00', 'onlineService' ,'finik1-new'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'MISMobile', '02.000.00', 'onlineProvider','DATAPOWER'
go


--9. Options for KPIPPR
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'KPIPPR', '02.000.00'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'KPIPPR'    , '02.000.00', 'offlineServer1','https://inavigator1.mobile.sbrf.ru:9917/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'KPIPPR'    , '02.000.00', 'offlineServer2','https://inavigator1.mobile.sbrf.ru:9918/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'KPIPPR'    , '02.000.00', 'offlineServer3','https://inavigator1.mobile.sbrf.ru:9919/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'KPIPPR'    , '02.000.00', 'offlineServer4','https://inavigator1.mobile.sbrf.ru:9920/syncserver/single'
go

--10. Options for iNavigator2
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'iNavigator2', '03.001.01'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator2', '03.001.01', 'onlineServer1' ,'https://inavigator1.mobile.sbrf.ru:9921/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator2', '03.001.01', 'onlineServer2' ,'https://inavigator1.mobile.sbrf.ru:9922/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator2', '03.001.01', 'onlineService' ,'finik1-new'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator2', '03.001.01', 'onlineProvider','DATAPOWER'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator2', '03.001.01', 'pushServer1','https://inavigator1.mobile.sbrf.ru:9922/syncserver/public/request.do'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator2', '03.001.01', 'pushServer2','https://inavigator1.mobile.sbrf.ru:9922/syncserver/public/request.do'
go

--11. Options for iNavigator2Phone
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'iNavigator2Phone', '03.001.01'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator2Phone', '03.001.01', 'onlineServer1' ,'https://inavigator1.mobile.sbrf.ru:9921/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator2Phone', '03.001.01', 'onlineServer2' ,'https://inavigator1.mobile.sbrf.ru:9922/syncserver/online'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator2Phone', '03.001.01', 'onlineService' ,'finik1-new'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator2Phone', '03.001.01', 'onlineProvider','DATAPOWER'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator2Phone', '03.001.01', 'pushServer1','https://inavigator1.mobile.sbrf.ru:9922/syncserver/public/request.do'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iNavigator2Phone', '03.001.01', 'pushServer2','https://inavigator1.mobile.sbrf.ru:9922/syncserver/public/request.do'
go
