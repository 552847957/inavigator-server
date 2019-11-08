if object_id('CONFSERVER_APPS') is not null drop table CONFSERVER_APPS
go
CREATE TABLE CONFSERVER_APPS(
   APP_ID      int                   primary key     ,
   APP_BUNDLE  varchar(128) not null unique
)
go

if object_id('CONFSERVER_VERSIONS') is not null drop table CONFSERVER_VERSIONS
go
CREATE TABLE CONFSERVER_VERSIONS(
   APP_ID        int          not null,
   APP_VERSION   varchar(128) not null
   unique (APP_ID, APP_VERSION)
)
go
if object_id('CONFSERVER_PROPERTY_TEMPLATES') is not null drop table CONFSERVER_PROPERTY_TEMPLATES
go
CREATE TABLE CONFSERVER_PROPERTY_TEMPLATES(
   APP_ID          int          not null,
   PROPERTY_CODE   varchar(128) not null,
   unique (APP_ID, PROPERTY_CODE)
)
go
if object_id('CONFSERVER_PROPERTY_VALUES') is not null drop table CONFSERVER_PROPERTY_VALUES
go
CREATE TABLE CONFSERVER_PROPERTY_VALUES(
   APP_ID          int          not null,
   APP_VERSION     varchar(128) not null,
   PROPERTY_CODE   varchar(128) not null,
   PROPERTY_VALUE  varchar(128)     null,
   unique (APP_ID, APP_VERSION, PROPERTY_CODE)
)
go
 
--1. Inserting client apps
INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     100   ,'Passport')

--2. Inserting client app properties
insert into CONFSERVER_PROPERTY_TEMPLATES(APP_ID, PROPERTY_CODE)
select                                    100  , 'offlineServer1'
union
select                                    100  , 'offlineServer2'
union
select                                    100  , 'loggingServer'
union
select                                    100  , 'pushServer1'
union
select                                    100  , 'pushServer2'
union
select                                    100  , 'statisticsKey'
union
select                                    100  , 'statisticsServer'
union
select                                    100  , 'versionStatus'
union
select                                    100  , 'versionMessage'
union
select                                    100  , 'distribServer'
go

