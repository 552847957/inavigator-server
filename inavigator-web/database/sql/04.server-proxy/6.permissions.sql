
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

INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'balance', N'iSalesManagenentHeadForUser')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'balance', N'iSalesManagenentRegForUser')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'competitors', N'CompetitorsForBoard')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'default', N'CompetitorsForBoard')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'iNavigator', N'iNavigatorForUser')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'iRubricator', NULL)
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'iup', N'FinanceReportForBoard')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'MISMobile', N'MISMobileForTestUser')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'iup', N'iSalesManagementForBoard')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'iup', N'iSalesManagementHeadForUser')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'iNavigator', N'iNavigatorForBoard')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'iNavigator', N'iNavigatorForTestUser')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'MISMobile', N'KPIPPRForUser')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'MISMobile', N'KPIPPRForTestUser')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'KPIPPR', N'KPIPPRForUser')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'KPIPPR', N'KPIPPRForTestUser')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'MISMobile', N'MISMobileForUser')
GO
INSERT [dbo].[APP_TO_BUNDLE_NAME] ([app], [bundle_name]) VALUES (N'iup_rb'   , N'iSalesManagementPoletaevForUser') 
GO

-- Добавлено для поддержки приложений iPassport
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
-- Закомментировано для версии ПСИ/ПРОМ
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


