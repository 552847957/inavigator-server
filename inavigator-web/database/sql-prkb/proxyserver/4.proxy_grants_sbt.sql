USE MIS_IPAD2
GO
if not exists ( select 'ipad_proxy_user' from sys.database_principals where name = 'ipad_proxy_user')
  CREATE USER ipad_proxy_user FOR LOGIN ipad_proxy_user
  exec sp_addrolemember db_datareader, ipad_proxy_user
GO
USE MIS_IPAD_MONITOR2
if not exists ( select 'ipad_proxy_user' from sys.database_principals where name = 'ipad_proxy_user')
  CREATE USER ipad_proxy_user FOR LOGIN ipad_proxy_user
  exec sp_addrolemember db_datareader, ipad_proxy_user
GO
USE MIS_IPAD_GENERATOR2
if not exists ( select 'ipad_proxy_user' from sys.database_principals where name = 'ipad_proxy_user')
  CREATE USER ipad_proxy_user FOR LOGIN ipad_proxy_user
  exec sp_addrolemember db_datareader, ipad_proxy_user
GO
USE MIS_IPAD2
GO
GRANT EXECUTE ON MIS_IPAD2.dbo.SP_IS_ALLOWED_TO_USE_APP TO [ipad_proxy_user]
GO
GRANT EXECUTE ON MIS_IPAD2.dbo.SP_IS_ALLOWED_TO_DOWNLOAD_FILE TO [ipad_proxy_user]
GO
USE MIS_IPAD_GENERATOR2
GO
GRANT EXECUTE ON MIS_IPAD_GENERATOR2.dbo.SP_SYNC_STORE_LOGMSG to ipad_proxy_user
GRANT EXECUTE ON MIS_IPAD_GENERATOR2.dbo.SP_SYNC_STORE_LOGMSG_WITH_ID to ipad_proxy_user
GRANT EXECUTE ON MIS_IPAD_GENERATOR2.dbo.SP_GET_FILE_STATUSES to ipad_proxy_user
GRANT EXECUTE ON MIS_IPAD_GENERATOR2.dbo.SP_GET_GENERATOR_USER_GROUPS_FOR_DRAFT to ipad_proxy_user
GRANT EXECUTE ON MIS_IPAD_GENERATOR2.dbo.SP_SYNC_STATIC_FILES_GEN_ADD_STATE to ipad_proxy_user
GO
USE MIS_IPAD_MONITOR2
GO
GRANT EXECUTE ON MIS_IPAD_MONITOR2.dbo.SP_PING to ipad_proxy_user
GRANT EXECUTE ON MIS_IPAD_MONITOR2.dbo.SP_ADD_FILE_NOTIFICATION to ipad_proxy_user
GRANT EXECUTE ON MIS_IPAD_MONITOR2.dbo.SP_DEL_FILE_NOTIFICATION to ipad_proxy_user
GRANT EXECUTE ON MIS_IPAD_MONITOR2.dbo.SP_ADD_ERROR to ipad_proxy_user
GRANT EXECUTE ON MIS_IPAD_MONITOR2.dbo.SP_ADD_FILE_GEN_NOTIFICATION to ipad_proxy_user
GRANT EXECUTE ON MIS_IPAD_MONITOR2.dbo.SP_DEL_FILE_GEN_NOTIFICATION to ipad_proxy_user
GRANT EXECUTE ON MIS_IPAD_MONITOR2.dbo.SP_ADD_FILE_MOV_NOTIFICATION to ipad_proxy_user
GRANT EXECUTE ON MIS_IPAD_MONITOR2.dbo.SP_DEL_FILE_MOV_NOTIFICATION to ipad_proxy_user
GRANT EXECUTE ON MIS_IPAD_MONITOR2.dbo.SP_LIST_NOTIFICATIONS to ipad_proxy_user
GO
