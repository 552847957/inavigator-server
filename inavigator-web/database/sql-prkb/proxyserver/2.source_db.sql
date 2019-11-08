use MIS_IPAD2
go

if object_id('SP_IPAD_GET_ACTUAL_DATE') is not null drop procedure SP_IPAD_GET_ACTUAL_DATE
go


GO
CREATE PROCEDURE [dbo].[SP_IPAD_GET_ACTUAL_DATE] (@report_code varchar(128))
as
begin
  set nocount on
  declare @actual_time datetime 
  declare @actual_time2 datetime 
  if @report_code='BALANCE' begin
    --OPU
    select @actual_time=[MIS_PL_GROUP_SB].[dbo].[f_GetActualReportDate] (101, 0, 'm')
    -- Balance
    select @actual_time2=[MIS_PL_GROUP_SB].[dbo].[f_GetActualReportDate] (102, 0, 'w')
  end else if @report_code='COMPETITORS' begin
    select @actual_time=[MIS_COMPETITORS_NT].[dbo].[f_GetActualReportDate] ('100','1','M')
  end else if @report_code='MIS_IUP_KPI' begin
    select @actual_time=value 
    from [MIS_IUP_KPI].[dbo].[default_params] 
    where param_name='last_load'
  end else if @report_code='MIS_NAVIGATOR_KPI' begin
    select @actual_time=[MIS_NAVIGATOR_DATA].[dbo].GetLastLoadDate (3)
  end else if @report_code='MIS_NAVIGATOR_KPI_W' begin
    select @actual_time=[MIS_NAVIGATOR_DATA].[dbo].GetLastLoadDate (4)
  end else if @report_code='MIS_PROGNOZ_SB_DATA' begin
    select @actual_time=max([datetime_stamp])
    FROM [MIS_PROGNOZ_SB_DATA].[dbo].[PR_DATAMART_VERSIONS]
    where dm_type='ipad'
  end else if @report_code in ('MIS_DASHBOARD_RB', 'MIS_DASHBOARD_RB2') begin
    select @actual_time=[MIS_DASHBOARD_RB_KPI].dbo.[GET_LAST_LOAD_DATE]()
  end else if @report_code in ('CIB_KPI','CIB_MARGIN','RETAIL_ATM','RETAIL_CRED','RETAIL_CRED_BAG',
                                                      'CIB_IBGM','CIB_KPKI','CIB_ASSETS','CIB_LIABILITIES','CIB_PROFIT_PASSIVE','CIB_PROFIT_PRODUCT',
                                                      'CIB_PROFIT_TOTAL','CIB_PROFIT_CONCENTRATION','CIB_PROFIT_ACTIVE') begin
           select @actual_time=[MIS_MOBILE].[dbo].f_mobile_upd_cib_lastupdate()
  end

  select @actual_time,@actual_time2
end
GO

if object_id('APP_TO_BUNDLE_NAME') IS NOT NULL
  DROP TABLE APP_TO_BUNDLE_NAME
GO

CREATE TABLE [dbo].[APP_TO_BUNDLE_NAME](
  [app] [varchar](256) NOT NULL,
  [bundle_name] [varchar](256) NULL
)

GO

/****** Object:  Index [app_bundle_index]    Script Date: 07.05.2014 18:39:58 ******/
CREATE UNIQUE INDEX [app_bundle_index] ON [dbo].[APP_TO_BUNDLE_NAME]
(
  [app] ASC,
  [bundle_name] ASC
)
GO

-- ��������� ��� ��������� ���������� iPassport
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'iPassport', N'iPassportForUser')
GO


if object_id('APP_TO_USERS') IS NOT NULL
  DROP TABLE APP_TO_USERS
GO


CREATE TABLE [dbo].[APP_TO_USERS](
  [email] [varchar](256) NOT NULL,
  [app] [varchar](256) NULL,
  [access] [char](1) NOT NULL DEFAULT ('Y')
)
-- ���������������� ��� ������ ���/����
--INSERT [dbo].[APP_TO_USERS] ([email], [app], [access]) VALUES (N'ASZunov.SBT@sberbank.ru', NULL, N'N')
--GO
--INSERT [dbo].[APP_TO_USERS] ([email], [app], [access]) VALUES (N'AVKolmakov.SBT@sberbank.ru', NULL, N'Y')
--GO
INSERT [dbo].[APP_TO_USERS] ([email], [app], [access]) VALUES (N'RONovoselov.SBT@sberbank.ru', NULL, N'Y')
GO

CREATE UNIQUE INDEX [email_app_access_index] ON [dbo].[APP_TO_USERS]
(
  [email] ASC,
  [app] ASC,
  [access] ASC
)
GO
/****** Object:  Index [email_app_index]    Script Date: 12.05.2014 11:48:58 ******/
CREATE UNIQUE INDEX [email_app_index] ON [dbo].[APP_TO_USERS]
(
  [email] ASC,
  [app] ASC
)
GO

if object_id('SP_IS_ALLOWED_TO_DOWNLOAD_FILE') IS NOT NULL
  DROP PROCEDURE SP_IS_ALLOWED_TO_DOWNLOAD_FILE
GO
if object_id('SP_IS_ALLOWED_TO_USE_APP') IS NOT NULL
  DROP PROCEDURE SP_IS_ALLOWED_TO_USE_APP
GO

CREATE PROCEDURE [dbo].[SP_IS_ALLOWED_TO_DOWNLOAD_FILE]
    @app      varchar(256),
    @filename varchar(256),
    @email    varchar(256),
    @device   varchar(256)
as
  if not exists (select 1 from APP_TO_USERS c where lower(c.email) = lower(@email) and (lower(c.app) = lower(@app) or c.app is NULL))
    begin
      select case
             when count(*) = 0 then 'false'
             else 'true'
             end as status
      from MIS_RUBRICATOR.dbo.reportList a with (nolock)
        left join MIS_RUBRICATOR.dbo.resource res with (nolock) on res.id = a.resourceId
        join MIS_BASE2.dbo._tAccessObject ao with (nolock) on (ao.AccessObjectName = a.id or ao.AccessObjectName = a.ownerId)
        join MIS_BASE2.dbo._tAccessObject2Role ao2r with (nolock) on ao2r.AccessObjectId = ao.AccessObjectId
        join MIS_BASE2.dbo._tRole r with (nolock) on r.RoleId = ao2r.RoleId
        join MIS_BASE2.dbo._tRole2User r2u with (nolock) on r.RoleId = r2u.RoleId
        join MIS_BASE2.dbo._tUser u with (nolock) on u.UserId = r2u.UserId
      where lower(u.Email)=lower(@email)
            and a.content_type = 'app'
            and exists (select 1 from APP_TO_BUNDLE_NAME b where lower(b.app) = lower(@app) and (lower(a.bundle_name)=lower(b.bundle_name) or b.bundle_name is NULL))
    end else
    begin
      select case
             when count(*) = 0 then 'false'
             else 'true'
             end as status
      from APP_TO_USERS c where
        lower(c.email) = lower(@email) and (lower(c.app) = lower(@app) or c.app is NULL) and c.access = 'Y'
    end

GO




CREATE PROCEDURE [dbo].[SP_IS_ALLOWED_TO_USE_APP]
    @app   varchar(256),
    @email varchar(256),
    @device   varchar(256)
as
  if not exists (select 1 from APP_TO_USERS c where lower(c.email) = lower(@email) and (lower(c.app) = lower(@app) or c.app is NULL))
    begin
      select case
             when count(*) = 0 then 'false'
             else 'true'
             end as status
      from MIS_RUBRICATOR.dbo.reportList a with (nolock)
        left join MIS_RUBRICATOR.dbo.resource res with (nolock) on res.id = a.resourceId
        join MIS_BASE2.dbo._tAccessObject ao with (nolock) on (ao.AccessObjectName = a.id or ao.AccessObjectName = a.ownerId)
        join MIS_BASE2.dbo._tAccessObject2Role ao2r with (nolock) on ao2r.AccessObjectId = ao.AccessObjectId
        join MIS_BASE2.dbo._tRole r with (nolock) on r.RoleId = ao2r.RoleId
        join MIS_BASE2.dbo._tRole2User r2u with (nolock) on r.RoleId = r2u.RoleId
        join MIS_BASE2.dbo._tUser u with (nolock) on u.UserId = r2u.UserId
      where lower(u.Email)=lower(@email)
            and a.content_type = 'app'
            and exists (select 1 from APP_TO_BUNDLE_NAME b where lower(b.app) = lower(@app) and (lower(a.bundle_name)=lower(b.bundle_name) or b.bundle_name is NULL))
    end else
    begin
      select case
             when count(*) = 0 then 'false'
             else 'true'
             end as status
      from APP_TO_USERS c where
        lower(c.email) = lower(@email) and (lower(c.app) = lower(@app) or c.app is NULL) and c.access = 'Y'
    end

GO



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
