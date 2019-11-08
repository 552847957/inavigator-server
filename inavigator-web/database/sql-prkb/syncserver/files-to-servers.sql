if object_id('SYNC_CACHE_STATIC_FILES') is not null DROP TABLE SYNC_CACHE_STATIC_FILES
go
CREATE TABLE SYNC_CACHE_STATIC_FILES(
  SYNC_CACHE_FILE_ID          INT IDENTITY(1,1) PRIMARY KEY,
  APP_CODE                    VARCHAR(256) NOT NULL,   
  FILE_ID                     VARCHAR(256) NOT NULL,
  FILE_NAME                   VARCHAR(256) NOT NULL,
  HOSTS                       VARCHAR(256)     NULL,
  GENERATION_SECONDS          INT          NOT NULL,
  IS_AUTO_GEN_ENABLED 		  INT,
  GENERATION_MODE	INT , -- 0 - public, 1- draft
  PUBLISHED_FILE_MD5	VARCHAR(256) ,
  DRAFT_FILE_MD5	VARCHAR(256)
)
go
insert into SYNC_CACHE_STATIC_FILES(APP_CODE,FILE_ID,FILE_NAME,HOSTS,GENERATION_SECONDS)
select 'iPassport','mis_prognoz_sb_data','MIS_PROGNOZ_SB_DATA.sqlite','localhost', 600
go

-- ���������� ������������� ��� ���� ������� �� ���������
update SYNC_CACHE_STATIC_FILES set IS_AUTO_GEN_ENABLED = 1;
go

-- set up default values (published and no md5)
update SYNC_CACHE_STATIC_FILES set 
	PUBLISHED_FILE_MD5 = NULL,
	DRAFT_FILE_MD5 = NULL,
	GENERATION_MODE = 0
GO 
