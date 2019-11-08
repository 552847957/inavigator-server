if object_id('CONFSERVER_PROPERTY_VALUES') is not null drop table CONFSERVER_PROPERTY_VALUES
go
if object_id('CONFSERVER_PROPERTY_TEMPLATES') is not null drop table CONFSERVER_PROPERTY_TEMPLATES
go
if object_id('CONFSERVER_VERSIONS') is not null drop table CONFSERVER_VERSIONS
go
if object_id('CONFSERVER_APPS') is not null drop table CONFSERVER_APPS
go

CREATE TABLE CONFSERVER_APPS(
   APP_ID      int                   primary key     ,
   APP_BUNDLE  varchar(128) not null unique
)
go

CREATE TABLE CONFSERVER_VERSIONS(
   APP_ID        int          not null REFERENCES CONFSERVER_APPS(APP_ID),
   APP_VERSION   varchar(128) not null
   unique (APP_ID, APP_VERSION)
)
go

CREATE TABLE CONFSERVER_PROPERTY_TEMPLATES(
   APP_ID          int          not null REFERENCES CONFSERVER_APPS(APP_ID),
   PROPERTY_CODE   varchar(128) not null,
   unique (APP_ID, PROPERTY_CODE)
)
go

CREATE TABLE CONFSERVER_PROPERTY_VALUES(
   APP_ID          int          not null REFERENCES CONFSERVER_APPS(APP_ID),
   APP_VERSION     varchar(128) not null,
   PROPERTY_CODE   varchar(128) not null,
   PROPERTY_VALUE  varchar(128)     null,
   unique (APP_ID, APP_VERSION, PROPERTY_CODE)
)
go
 
--1. Inserting client apps
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     10    ,'iNavigator')
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     20    ,'balance'    )
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     30    ,'competitors')
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     40    ,'iRubricator')
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     50    ,'DynamicModel')
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     60    ,'iSalesManagementHead')
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     70    ,'iSalesManagementReg')
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     80    ,'MISMobile')
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     90    ,'KPIPPR')
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     100   ,'Passport')
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     110   ,'iup_rb')
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     120    ,'iNavigator2')
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     130    ,'iNavigator2Phone')

--2. Inserting client app properties
declare @id int
set @id=10
while @id<=130 begin
  --1. Adding only file server properties for offline apps
  if @id in (10,20,30,40, 50,60,70,80,90,100,110,120,130) begin  --(10) for iNavigator , (20) for balance, 
                                                         --(30) for competitors, (40) for iRubricator
                                                         --(60,70) for  iSalesManagement 
                                                         --(80) for mis mobile
                                                         --(90) for KPIPPR
                                                        --(100) for Passport
                                                        --(110) for iup_rb
    insert into CONFSERVER_PROPERTY_TEMPLATES(APP_ID, PROPERTY_CODE)
    select                                    @id  , 'offlineServer1'
    union
    select                                    @id  , 'offlineServer2'
  end 

  if @id in (30,120,130) begin  --(30) for competitors
	  insert into CONFSERVER_PROPERTY_TEMPLATES(APP_ID, PROPERTY_CODE)
	  select                                    @id  , 'loggingServer'
	  insert into CONFSERVER_PROPERTY_TEMPLATES(APP_ID, PROPERTY_CODE)
	  select                                    @id  , 'pushServer1'
	  insert into CONFSERVER_PROPERTY_TEMPLATES(APP_ID, PROPERTY_CODE)
	  select                                    @id  , 'pushServer2'
	  insert into CONFSERVER_PROPERTY_TEMPLATES(APP_ID, PROPERTY_CODE)
	  select                                    @id  , 'statisticsKey'
	  insert into CONFSERVER_PROPERTY_TEMPLATES(APP_ID, PROPERTY_CODE)
	  select                                    @id  , 'statisticsServer'
  end 


  if @id in (80,90) begin  --(80) for mis mobile, (90) for KPIPPR
    insert into CONFSERVER_PROPERTY_TEMPLATES(APP_ID, PROPERTY_CODE)
    select                                    @id  , 'offlineServer3'
    union
    select                                    @id  , 'offlineServer4'
  end 

  --2. Adding also sql properties for online apps
  if @id in (10,40,50,80,120,130) begin  --(10) for iNavigator  , (40)for iRubricator, 
                                 --(50)for Dynamic Model, (80) for mis mobile
    insert into CONFSERVER_PROPERTY_TEMPLATES(APP_ID, PROPERTY_CODE)
    select                                    @id  , 'onlineServer1'
    union
    select                                    @id  , 'onlineServer2'
    union
    select                                    @id  , 'onlineService'
    union
    select                                    @id  , 'onlineProvider'
  end

  --3. Adding properties for version tracking
  insert into CONFSERVER_PROPERTY_TEMPLATES(APP_ID, PROPERTY_CODE)
  select                                    @id  , 'versionStatus'
  union
  select                                    @id  , 'versionMessage'

  --4. Adding property for distrib server
  insert into CONFSERVER_PROPERTY_TEMPLATES(APP_ID, PROPERTY_CODE)
  select                                    @id  , 'distribServer'
  set @id=@id+10
end
go

