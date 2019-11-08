
IF OBJECT_ID('CLUSTER_STATUS') IS NOT NULL DROP TABLE CLUSTER_STATUS
GO
CREATE TABLE CLUSTER_STATUS(
  CLUSTER_STATUS_ID int primary key check(CLUSTER_STATUS_ID=1) ,
  ACTIVE_HOST_NAME  varchar(256)                       not null,
  LAST_PING_TIME    datetime                           not null,
)
GO
INSERT INTO CLUSTER_STATUS VALUES(1, 'tv-inavs-8r2-02', getdate())
GO

IF OBJECT_ID('SP_IS_HOST_ACTIVE') IS NOT NULL DROP PROCEDURE SP_IS_HOST_ACTIVE
GO
CREATE PROCEDURE SP_IS_HOST_ACTIVE
  @HOST_NAME       varchar(256) 
AS
  declare @active_host_name varchar(256)
  declare @is_host_active varchar(256)
  begin tran
    set nocount on
    UPDATE CLUSTER_STATUS SET LAST_PING_TIME=getdate() 
    WHERE ACTIVE_HOST_NAME=@HOST_NAME
    if @@rowcount>0 begin
       set @is_host_active = 'true' 
    end else begin
      UPDATE CLUSTER_STATUS SET LAST_PING_TIME=getdate(), ACTIVE_HOST_NAME = @HOST_NAME
      WHERE LAST_PING_TIME<dateadd(second,-300, getdate())
      set @is_host_active = case when @@rowcount>0 then 'true' else 'false' end
    end
    select @active_host_name=ACTIVE_HOST_NAME from CLUSTER_STATUS
    select @is_host_active is_host_active, @active_host_name active_host_name 
  commit;
GO
