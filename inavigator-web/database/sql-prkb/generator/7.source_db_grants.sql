USE MIS_IPAD2
GO
if not exists ( select * from sys.database_principals where name = 'ipad_generator_user')  begin
  CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user
  exec sp_addrolemember db_owner, ipad_generator_user
end
GO
GRANT EXECUTE ON MIS_IPAD2..SP_IPAD_GET_ACTUAL_DATE TO [ipad_generator_user]
GO
