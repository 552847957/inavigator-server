INSERT INTO CONFSERVER_APPS(APP_ID, APP_BUNDLE)
values(                     110   ,'iup_rb')
go
insert into CONFSERVER_PROPERTY_TEMPLATES(APP_ID, PROPERTY_CODE)
select                                    110  , 'offlineServer1'
union
select                                    110  , 'offlineServer2'
union
select                                    110  , 'versionStatus'
union
select                                    110  , 'versionMessage'
union
select                                    110  , 'distribServer'
go
exec SP_SYNC_RESET_CLIENT_PROPERTIES 'iup_rb', '02.000.00'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iup_rb', '02.000.00', 'offlineServer1','https://inavigator1.mobile.sbrf.ru:9915/syncserver/single'
go
exec SP_SYNC_SET_CLIENT_PROPERTY 'iup_rb', '02.000.00', 'offlineServer2','https://inavigator1.mobile.sbrf.ru:9916/syncserver/single'
go
