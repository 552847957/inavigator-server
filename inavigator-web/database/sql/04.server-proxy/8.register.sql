
INSERT INTO SQL_TEMPLATES_SERVER VALUES('SYNCSERVER.REGISTER_PUSH','exec SP_SYNC_REGISTER_PUSH ?,?,?')
go
IF OBJECT_ID('IPAD_USERS') is not null drop table IPAD_USERS
GO
CREATE TABLE IPAD_USERS(
  USER_ID      int identity(1,1) primary key,
  USER_EMAIL   varchar(128)      not null   ,
  DEVICE_ID    varchar(128)      not null   ,
  PUSH_TOKEN   varchar(128)      not null   ,
  INSERT_DATE  datetime          not null default (getdate()),
  UNIQUE (USER_EMAIL, DEVICE_ID)
)

IF OBJECT_ID('SP_SYNC_REGISTER_PUSH') is not null drop procedure SP_SYNC_REGISTER_PUSH
GO
CREATE PROCEDURE SP_SYNC_REGISTER_PUSH
  @user_email  varchar(128) ,
  @device_id   varchar(128) ,
  @push_token  varchar(128) 
as
  if not exists (select * from IPAD_USERS where USER_EMAIL=@user_email and DEVICE_ID=@device_id) begin
    insert into IPAD_USERS(USER_EMAIL,DEVICE_ID,PUSH_TOKEN)
    values(@user_email,@device_id,@push_token)
  end
go
