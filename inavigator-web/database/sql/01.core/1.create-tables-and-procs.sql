set nocount
on
if object_id('SYNC_SERVICES') is not null DROP TABLE SYNC_SERVICES
GO
if object_id('SYNC_FOLDERS') is not null DROP TABLE SYNC_FOLDERS
GO
if object_id('PROPERTY_VALUES') is not null DROP TABLE PROPERTY_VALUES
GO
if object_id('PROPERTY_LISTS') is not null DROP TABLE PROPERTY_LISTS
go
if object_id('PROPERTY_TEMPLATES') is not null DROP TABLE PROPERTY_TEMPLATES
go
if object_id('PROPERTY_LIST_TEMPLATES') is not null DROP TABLE PROPERTY_LIST_TEMPLATES
go
if object_id('EMPLOYEES') is not null DROP TABLE EMPLOYEES
GO
if object_id('EMPLOYEE_ROLES') is not null DROP TABLE EMPLOYEE_ROLES
GO
if object_id('SP_SYNC_LIST_FOLDERS') is not null DROP PROCEDURE SP_SYNC_LIST_FOLDERS
GO
if object_id('SP_SYNC_COMPOSE_PROPERTY_LIST_CODE') is not null DROP PROCEDURE SP_SYNC_COMPOSE_PROPERTY_LIST_CODE
GO
if object_id('SP_SYNC_LIST_PROPERTIES') is not null DROP PROCEDURE SP_SYNC_LIST_PROPERTIES
go
if object_id('SP_SYNC_RESET_PROPERTIES') is not null DROP PROCEDURE SP_SYNC_RESET_PROPERTIES
go
if object_id('SP_SYNC_SET_PROPERTY') is not null DROP PROCEDURE SP_SYNC_SET_PROPERTY
go
if object_id('SP_SYNC_LIST_TEMPLATES') is not null DROP PROCEDURE SP_SYNC_LIST_TEMPLATES
go
if object_id('SP_SYNC_LIST_SERVICES') is not null DROP PROCEDURE SP_SYNC_LIST_SERVICES
go
if object_id('SP_SYNC_LIST_SERVICES_FOR_DISPATCHER') is not null DROP PROCEDURE SP_SYNC_LIST_SERVICES_FOR_DISPATCHER
go

/*
 *
 */
 
CREATE TABLE EMPLOYEE_ROLES(
  EMPLOYEE_ROLE_ID     INT          PRIMARY KEY,
  EMPLOYEE_ROLE_NAME   VARCHAR(128) NOT NULL
)
go
CREATE TABLE EMPLOYEES(
  EMPLOYEE_ID       INT IDENTITY(1,1) PRIMARY KEY,
  EMPLOYEE_ROLE_ID  INT          NOT NULL REFERENCES EMPLOYEE_ROLES(EMPLOYEE_ROLE_ID),
  EMPLOYEE_EMAIL    VARCHAR(128) NOT NULL UNIQUE,
  EMPLOYEE_NAME     VARCHAR(256) NOT NULL,
  leave_date        DATETIME,
  EMPLOYEE_PASSWORD VARCHAR(256) NOT NULL,
  PASSWORD_CHANGED_DATE DATETIME,
  IS_REMOTE 		BIT 		NOT NULL  DEFAULT 0,
  IS_READ_ONLY 		BIT 		NOT NULL DEFAULT 0
)
go
CREATE TABLE PROPERTY_LIST_TEMPLATES(
  LIST_TEMPLATE_ID     INT           IDENTITY(1,1) PRIMARY KEY,
  LIST_TEMPLATE_TYPE   CHAR(1)       NOT NULL CHECK (LIST_TEMPLATE_TYPE in ('D','C','S','A')),
  LIST_TEMPLATE_CODE   VARCHAR(128)  NOT NULL
)
go
CREATE TABLE PROPERTY_TEMPLATES(
  TEMPLATE_ID         INT IDENTITY(1,1) PRIMARY KEY,
  LIST_TEMPLATE_ID    INT NOT NULL  REFERENCES PROPERTY_LIST_TEMPLATES(LIST_TEMPLATE_ID),
  TEMPLATE_CODE       VARCHAR(128)  NOT NULL,
  TEMPLATE_DESC       VARCHAR(2048) NOT NULL,
  IS_OPTIONAL         CHAR(1)       NOT NULL
)
go

CREATE TABLE PROPERTY_LISTS(
  LIST_ID                  INT IDENTITY(1,1) PRIMARY KEY,
  LIST_TEMPLATE_ID         INT NOT NULL      REFERENCES PROPERTY_LIST_TEMPLATES(LIST_TEMPLATE_ID),
  LIST_CODE                VARCHAR(512) NOT NULL UNIQUE
)
go

CREATE TABLE PROPERTY_VALUES(
  VALUE_ID          INT IDENTITY(1,1) PRIMARY KEY,
  LIST_ID           INT NOT NULL REFERENCES PROPERTY_LISTS(LIST_ID),
  TEMPLATE_ID       INT NOT NULL REFERENCES PROPERTY_TEMPLATES(TEMPLATE_ID),
  VALUE             VARCHAR(1024) NULL,
  UNIQUE(LIST_ID,TEMPLATE_ID)
)
go

CREATE TABLE SYNC_FOLDERS(
  SYNC_FOLDER_ID   INT IDENTITY(1,1)           ,
  SYNC_FOLDER_CODE VARCHAR(128)  NOT NULL UNIQUE,
  SYNC_FOLDER_DESC VARCHAR(2048) NOT NULL,
  START_ORDER      INT           NOT NULL
)
go
CREATE TABLE SYNC_SERVICES(
  SYNC_SERVICE_ID           INT IDENTITY(1,1) PRIMARY KEY                                  ,
  SYNC_FOLDER_CODE          VARCHAR(128) NOT NULL REFERENCES SYNC_FOLDERS(SYNC_FOLDER_CODE),
  SERVLET_PATH              VARCHAR(256)     NULL                                          ,
  BEAN_CODE                 VARCHAR(256) NOT NULL UNIQUE                                   ,
  BEAN_CLASS                VARCHAR(256)                                                   ,
  PARENT_BEAN_CODE          VARCHAR(256)     NULL                                          ,
  PARENT_BEAN_PROPERTY      VARCHAR(256)     NULL                                          ,
  START_ORDER               INT          NOT NULL                                          ,
  BEAN_DESC                 VARCHAR(2048)    NULL					   ,
  STOPPED		    BIT		     NULL
)
go

CREATE PROCEDURE SP_SYNC_LIST_FOLDERS
AS
  SELECT SYNC_FOLDER_CODE, START_ORDER, SYNC_FOLDER_DESC
  FROM SYNC_FOLDERS
  ORDER BY START_ORDER
GO

CREATE PROCEDURE SP_SYNC_LIST_SERVICES
   @sync_folder_code varchar(256)
AS
  SELECT SYNC_SERVICE_ID,BEAN_CODE,BEAN_CLASS,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY,START_ORDER, SERVLET_PATH as PUBLIC_SERVLET_PATH, BEAN_DESC, STOPPED
  FROM SYNC_SERVICES
  WHERE SYNC_FOLDER_CODE=@sync_folder_code
  ORDER BY START_ORDER
GO
--CREATE PROCEDURE SP_SYNC_LIST_SERVICES_FOR_DISPATCHER
--   @sync_folder_code varchar(256)
--AS
--  SELECT s.SYNC_SERVICE_ID,s.BEAN_CODE,SERVLET_PATH, f.SYNC_FOLDER_CODE
--  FROM SYNC_SERVICES s join SYNC_FOLDERS f on f.SYNC_FOLDER_CODE=s.SYNC_FOLDER_CODE
--  WHERE SERVLET_PATH IS NOT NULL and s.SYNC_FOLDER_CODE=@sync_folder_code
--O

--CREATE PROCEDURE SP_SYNC_LIST_TEMPLATES
--AS
--  SELECT LIST_TEMPLATE_ID,
--       case when LIST_TEMPLATE_TYPE='C' then 'For Client'
--            when LIST_TEMPLATE_TYPE='S' then 'For Service'
--            when LIST_TEMPLATE_TYPE='A' then 'For Admin GUI'
--       end LIST_TEMPLATE_TYPE,LIST_TEMPLATE_CODE
--  FROM PROPERTY_LIST_TEMPLATES
--  ORDER BY 3
--O

CREATE PROCEDURE SP_SYNC_COMPOSE_PROPERTY_LIST_CODE
  @sync_folder_code varchar(256),
  @bean_code        varchar(256),
  @list_code              varchar(256) out
as
  set @list_code=@sync_folder_code+'/'+@bean_code
GO

CREATE PROCEDURE SP_SYNC_LIST_PROPERTIES
  @sync_folder_code varchar(256),
  @bean_code        varchar(256)
as
  declare @list_code varchar(1024)
  exec SP_SYNC_COMPOSE_PROPERTY_LIST_CODE @sync_folder_code,@bean_code,@list_code out
  declare @list_id int
  select @list_id=list_id from PROPERTY_LISTS where LIST_CODE=@list_code

  select v.VALUE_ID, t.TEMPLATE_CODE, v.VALUE, t.TEMPLATE_DESC
  from PROPERTY_VALUES v join PROPERTY_TEMPLATES t on t.TEMPLATE_ID=v.TEMPLATE_ID
  where LIST_ID=@list_id
  order by 1
GO


CREATE PROCEDURE SP_SYNC_SET_PROPERTY
  @sync_folder_code varchar(256) ,
  @bean_code        varchar(256) ,
  @property_code    varchar(256) ,
  @value            varchar(1024)
as
  --1. Compose key, list id and template id
  declare @list_code varchar(1024)
  exec SP_SYNC_COMPOSE_PROPERTY_LIST_CODE @sync_folder_code,@bean_code,@list_code out

  declare @list_id int
  declare @list_template_id int
  select @list_id=list_id, @list_template_id = LIST_TEMPLATE_ID from PROPERTY_LISTS where LIST_CODE=@list_code

  declare @template_id int
  select @template_id=template_id from PROPERTY_TEMPLATES
  where LIST_TEMPLATE_ID=@list_template_id and TEMPLATE_CODE=@property_code

  --2. Deleting and inserting
  begin tran
    delete from PROPERTY_VALUES where LIST_ID=@list_id and TEMPLATE_ID=@template_id
    insert into PROPERTY_VALUES(LIST_ID , TEMPLATE_ID,VALUE)
    select                      @list_id, @template_id,@value
  commit
GO


CREATE PROCEDURE SP_SYNC_RESET_PROPERTIES
  @sync_folder_code varchar(256),
  @bean_code        varchar(256)
AS
  --1. Compose key
  declare @bean_class varchar(256)
  declare @list_code  varchar(1024)
  exec SP_SYNC_COMPOSE_PROPERTY_LIST_CODE @sync_folder_code,@bean_code,@list_code out
  select @bean_class = bean_class from SYNC_SERVICES where BEAN_CODE=@bean_code

  --2. Delete
  delete from PROPERTY_VALUES where LIST_ID in (select LIST_ID from PROPERTY_LISTS where LIST_CODE=@list_code)
  delete from PROPERTY_LISTS where LIST_CODE=@list_code

  --3. Insert
  declare @list_template_id int
  select @list_template_id=list_template_id from PROPERTY_LIST_TEMPLATES where LIST_TEMPLATE_CODE=@bean_class
  if @list_template_id is null return --No properties for this bean class found
  declare @list_id int
  insert into PROPERTY_LISTS(LIST_CODE ,LIST_TEMPLATE_ID)
  select                     @list_code,@list_template_id
  select @list_id=@@IDENTITY

  insert into PROPERTY_VALUES(LIST_ID , TEMPLATE_ID,VALUE)
  select                      @list_id, TEMPLATE_ID,case when IS_OPTIONAL='Y' then null else '' end
  from PROPERTY_TEMPLATES
  where LIST_TEMPLATE_ID=@list_template_id
GO

INSERT INTO [EMPLOYEE_ROLES] VALUES(1,'Administrator')
INSERT INTO [EMPLOYEES] ([EMPLOYEE_ROLE_ID],[EMPLOYEE_EMAIL], [EMPLOYEE_NAME], [leave_date], [EMPLOYEE_PASSWORD],[PASSWORD_CHANGED_DATE]) VALUES (1,'admin', 'admin', null, 'E10ADC3949BA59ABBE56E057F20F883E',getdate())
GO

if object_id('SP_SYNC_ADD_TEMPLATE') is not null DROP PROCEDURE SP_SYNC_ADD_TEMPLATE
GO
CREATE PROCEDURE SP_SYNC_ADD_TEMPLATE
   @template_code VARCHAR(128)
as
  set nocount on
  insert into PROPERTY_LIST_TEMPLATES (LIST_TEMPLATE_TYPE, LIST_TEMPLATE_CODE)
  select                               'S'               , @template_code    
GO
if object_id('SP_SYNC_ADD_TEMPLATE2') is not null DROP PROCEDURE SP_SYNC_ADD_TEMPLATE2
GO
CREATE PROCEDURE SP_SYNC_ADD_TEMPLATE2
   @id            int out,
   @template_code VARCHAR(128)
as
  set nocount on
  insert into PROPERTY_LIST_TEMPLATES (LIST_TEMPLATE_TYPE, LIST_TEMPLATE_CODE)
  select                               'S'               , @template_code    
  set @id=@@identity
GO
if object_id('SP_SYNC_ADD_TEMPLATE_PROPERTY') is not null DROP PROCEDURE SP_SYNC_ADD_TEMPLATE_PROPERTY
GO
CREATE PROCEDURE SP_SYNC_ADD_TEMPLATE_PROPERTY
   @template_code VARCHAR(128) ,
   @property_code VARCHAR(128) ,
   @property_desc VARCHAR(2048),
   @is_optional   CHAR(1)     
as
  --1. Finding template id
  set nocount on
  declare @list_template_id int
  select @list_template_id=LIST_TEMPLATE_ID from PROPERTY_LIST_TEMPLATES where LIST_TEMPLATE_CODE=@template_code

  --2. Inserting
  insert into PROPERTY_TEMPLATES(LIST_TEMPLATE_ID , TEMPLATE_CODE , TEMPLATE_DESC , IS_OPTIONAL  )
  values(                        @list_template_id, @property_code, @property_desc, @is_optional)
GO
if object_id('SP_SYNC_ADD_TEMPLATE_PROPERTY2') is not null DROP PROCEDURE SP_SYNC_ADD_TEMPLATE_PROPERTY2
GO
CREATE PROCEDURE SP_SYNC_ADD_TEMPLATE_PROPERTY2
   @list_template_id int         ,
   @property_code    VARCHAR(128),
   @is_optional      CHAR(1)     ,
   @property_desc    VARCHAR(2048)   
as
  insert into PROPERTY_TEMPLATES(LIST_TEMPLATE_ID , TEMPLATE_CODE , TEMPLATE_DESC , IS_OPTIONAL)
  values(                        @list_template_id, @property_code, @property_desc, @is_optional)
GO


if object_id('SP_SYNC_ADD_TEMPLATE') is not null DROP PROCEDURE SP_SYNC_ADD_TEMPLATE
GO
CREATE PROCEDURE SP_SYNC_ADD_TEMPLATE
   @template_code VARCHAR(128)
as
  set nocount on
  insert into PROPERTY_LIST_TEMPLATES (LIST_TEMPLATE_TYPE, LIST_TEMPLATE_CODE )
  select                               'S'               , @template_code
GO
if object_id('SP_SYNC_ADD_TEMPLATE_PROPERTY') is not null DROP PROCEDURE SP_SYNC_ADD_TEMPLATE_PROPERTY
GO
CREATE PROCEDURE SP_SYNC_ADD_TEMPLATE_PROPERTY
   @template_code VARCHAR(128) ,
   @property_code VARCHAR(128) ,
   @is_optional   CHAR(1)      
as
  --1. Finding template id
  set nocount on
  declare @list_template_id int
  select @list_template_id=LIST_TEMPLATE_ID from PROPERTY_LIST_TEMPLATES where LIST_TEMPLATE_CODE=@template_code

  --2. Inserting
  insert into PROPERTY_TEMPLATES(LIST_TEMPLATE_ID , TEMPLATE_CODE , IS_OPTIONAL )
  values(                        @list_template_id, @property_code, @is_optional)
GO


IF OBJECT_ID('READONLY_CONFIG') IS NOT NULL DROP TABLE READONLY_CONFIG
GO
CREATE TABLE READONLY_CONFIG(
   PROPERTY_KEY    VARCHAR(256),
   PROPERTY_VALUE  VARCHAR(1024)
)
GO
INSERT INTO READONLY_CONFIG VALUES('VERSION','@SVN_REVISION@')
GO

