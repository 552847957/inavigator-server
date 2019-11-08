DECLARE @BASE VARCHAR(50)
SET @BASE = 'MIS_NAVIGATOR_DATA'
IF DB_ID(@BASE) is not null BEGIN
  exec ('
  USE ' + @BASE + '
  IF NOT EXISTS ( select 1 from sys.database_principals where name = ''ipad_proxy_user'') BEGIN
    CREATE USER ipad_proxy_user FOR LOGIN ipad_proxy_user;
    EXEC sp_addrolemember db_datareader, ipad_proxy_user
    PRINT ''' + @BASE + ': user created''
  END
  ELSE
    PRINT ''' +@BASE + ' : user already exists'' 
  ')
END
ELSE
	PRINT @BASE + ' does not exists'
GO

DECLARE @BASE VARCHAR(50)
SET @BASE = 'MIS_PCA_CIB_DYNAMIC_MODEL'
IF DB_ID(@BASE) is not null BEGIN
  exec ('
  USE ' + @BASE + '
  IF NOT EXISTS ( select 1 from sys.database_principals where name = ''ipad_proxy_user'') BEGIN
    CREATE USER ipad_proxy_user FOR LOGIN ipad_proxy_user;
    EXEC sp_addrolemember db_datareader, ipad_proxy_user
    PRINT ''' + @BASE + ': user created''
  END
  ELSE
    PRINT ''' +@BASE + ' : user already exists'' 
  ')
END
ELSE
	PRINT @BASE + ' does not exists'
GO

DECLARE @BASE VARCHAR(50)
SET @BASE = 'MIS_PCA_DKK_DATA'
IF DB_ID(@BASE) is not null BEGIN
  exec ('
  USE ' + @BASE + '
  IF NOT EXISTS ( select 1 from sys.database_principals where name = ''ipad_proxy_user'') BEGIN
    CREATE USER ipad_proxy_user FOR LOGIN ipad_proxy_user;
    EXEC sp_addrolemember db_datareader, ipad_proxy_user
    PRINT ''' + @BASE + ': user created''
  END
  ELSE
    PRINT ''' +@BASE + ' : user already exists'' 
  ')
END
ELSE
	PRINT @BASE + ' does not exists'
GO


DECLARE @BASE VARCHAR(50)
SET @BASE = 'MIS_BASE2'
IF DB_ID(@BASE) is not null BEGIN
  exec ('
  USE ' + @BASE + '
  IF NOT EXISTS ( select 1 from sys.database_principals where name = ''ipad_proxy_user'') BEGIN
    CREATE USER ipad_proxy_user FOR LOGIN ipad_proxy_user;
    EXEC sp_addrolemember db_datareader, ipad_proxy_user
    PRINT ''' + @BASE + ': user created''
  END
  ELSE
    PRINT ''' +@BASE + ' : user already exists'' 
  ')
END
ELSE
	PRINT @BASE + ' does not exists'
GO


DECLARE @BASE VARCHAR(50)
SET @BASE = 'MIS_RETAIL_VSP_MOBILE'
IF DB_ID(@BASE) is not null BEGIN
  exec ('
  USE ' + @BASE + '
  IF NOT EXISTS ( select 1 from sys.database_principals where name = ''ipad_proxy_user'') BEGIN
    CREATE USER ipad_proxy_user FOR LOGIN ipad_proxy_user;
    EXEC sp_addrolemember db_datareader, ipad_proxy_user
    PRINT ''' + @BASE + ': user created''
  END
  ELSE
    PRINT ''' +@BASE + ' : user already exists'' 
  ')
END
ELSE
	PRINT @BASE + ' does not exists'
GO

DECLARE @BASE VARCHAR(50)
SET @BASE = 'MIS_RUBRICATOR'
IF DB_ID(@BASE) is not null BEGIN
  exec ('
  USE ' + @BASE + '
  IF NOT EXISTS ( select 1 from sys.database_principals where name = ''ipad_proxy_user'') BEGIN
    CREATE USER ipad_proxy_user FOR LOGIN ipad_proxy_user;
    EXEC sp_addrolemember db_datareader, ipad_proxy_user
    PRINT ''' + @BASE + ': user created''
  END
  ELSE
    PRINT ''' +@BASE + ' : user already exists'' 
  ')
END
ELSE
	PRINT @BASE + ' does not exists'
GO