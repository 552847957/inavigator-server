USE [MIS_BALANCE]
GO
if not exists ( select 'ipad_generator_user' from sys.database_principals where name = 'ipad_generator_user') begin
  CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
  exec sp_addrolemember db_datareader, ipad_generator_user
end
GO
IF DB_ID('MIS_RETAIL_ATM') IS NOT NULL
USE [MIS_PCA_DKK_DATA]
GO
if not exists ( select 'ipad_generator_user' from sys.database_principals where name = 'ipad_generator_user') begin
  CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
  exec sp_addrolemember db_datareader, ipad_generator_user
end
GO
USE [MIS_RETAIL_ATM]
GO
if not exists ( select 'ipad_generator_user' from sys.database_principals where name = 'ipad_generator_user') begin
  CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
  exec sp_addrolemember db_datareader, ipad_generator_user
end
GO
USE [MIS_RETAIL_CRED]
GO
if not exists ( select 'ipad_generator_user' from sys.database_principals where name = 'ipad_generator_user') begin
  CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
  exec sp_addrolemember db_datareader, ipad_generator_user
end
GO
USE [mis_competitors]
GO
if not exists ( select 'ipad_generator_user' from sys.database_principals where name = 'ipad_generator_user') begin
  CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
  exec sp_addrolemember db_datareader, ipad_generator_user
end
GO
USE [dashboard_ppr_mobile]
GO
if not exists ( select 'ipad_generator_user' from sys.database_principals where name = 'ipad_generator_user') begin
  CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
  exec sp_addrolemember db_datareader, ipad_generator_user
end
GO
USE [mis_navigator_kpi]
GO
if not exists ( select 'ipad_generator_user' from sys.database_principals where name = 'ipad_generator_user') begin
  CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
  exec sp_addrolemember db_datareader, ipad_generator_user
end
GO
USE [MIS_NAVIGATOR_DATA]
GO
if not exists ( select 'ipad_generator_user' from sys.database_principals where name = 'ipad_generator_user') begin
  CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
  exec sp_addrolemember db_datareader, ipad_generator_user
end 
GO
USE [mis_iup_kpi]
GO
if not exists ( select 'ipad_generator_user' from sys.database_principals where name = 'ipad_generator_user') begin
  CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
  exec sp_addrolemember db_datareader, ipad_generator_user
end
GO
USE [mis_iup_data]
GO
if not exists ( select 'ipad_generator_user' from sys.database_principals where name = 'ipad_generator_user') begin
  CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
  exec sp_addrolemember db_datareader, ipad_generator_user
end
GO
if DB_ID('mis_prognoz_sb_data') is not null begin
exec('  USE [mis_prognoz_sb_data]
        if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'') begin
            CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
             exec sp_addrolemember db_datareader, ipad_generator_user
         end')
end
GO
USE [MIS_PL_GROUP_SB]
GO
if not exists ( select 'ipad_generator_user' from sys.database_principals where name = 'ipad_generator_user') begin
  CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
  exec sp_addrolemember db_datareader, ipad_generator_user
end
GO
if DB_ID('MIS_DASHBOARD_RB_KPI') is not null begin
  USE MIS_DASHBOARD_RB_KPI
  if not exists ( select 'ipad_generator_user' from sys.database_principals where name = 'ipad_generator_user') begin
    CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
    exec sp_addrolemember db_datareader, ipad_generator_user
  end
end
GO
if DB_ID('MIS_RUBRICATOR') is not null begin
  USE MIS_RUBRICATOR
  if not exists ( select 'ipad_generator_user' from sys.database_principals where name = 'ipad_generator_user') begin
    CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
    exec sp_addrolemember db_datareader, ipad_generator_user
    GRANT EXECUTE ON getChangeAfterId TO ipad_generator_user    
  end
end
GO
