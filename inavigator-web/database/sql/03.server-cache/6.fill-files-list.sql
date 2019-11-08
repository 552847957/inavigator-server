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
select 'iNavigator' ,'MIS_NAVIGATOR_KPI_LIQ','MIS_NAVIGATOR_KPI_LIQ.sqlite','localhost',900
union all
select 'iNavigator' ,'MIS_NAVIGATOR_KPI_LIQ_W','MIS_NAVIGATOR_KPI_LIQ_W.sqlite','localhost',900
union all
select 'iNavigator' ,'MIS_NAVIGATOR_KPI_LIQ_D','MIS_NAVIGATOR_KPI_LIQ_D.sqlite','localhost',900
union all
select 'iNavigator' ,'mis_navigator_kpi'   ,'MIS_NAVIGATOR_KPI.sqlite'   ,'localhost',11095
union all
select 'iNavigator' ,'mis_navigator_kpi2'  ,'MIS_NAVIGATOR_KPI2.sqlite'  ,'localhost',11095
union all
select 'iNavigator' ,'mis_navigator_kpi_w' ,'MIS_NAVIGATOR_KPI_W.sqlite' ,'localhost',0
union all
select 'iNavigator' ,'mis_navigator_kpi_w2','MIS_NAVIGATOR_KPI_W2.sqlite','localhost',0
union all
select 'iNavigator' ,'mis_navigator_kpi_d','MIS_NAVIGATOR_KPI_D.sqlite','localhost',11095
union all
select 'iup'        ,'mis_iup_kpi','mis_iup_kpi.sqlite','localhost',301
union all
select 'competitors','mis_competitors','competitors.sqlite','localhost',94
union all
select 'balance'    ,'mis_balance','balance.sqlite','localhost',9301
union all
select 'MISMobile'  ,'RETAIL_CRED','RETAIL_CRED.sqlite','localhost',55059
union all
select 'MISMobile'  ,'RETAIL_CRED_BAG','RETAIL_CRED_BAG.sqlite','localhost',50164
union all
select 'MISMobile'  ,'RETAIL_CRED_ATM','mis_retail_atm_01.db','localhost',0
union all
select 'MISMobile'  ,'DASHBOARD_PPR_MOBILE','DASHBOARD_PPR_MOBILE.sqlite','localhost',30
union all
select 'MISMobile'  ,'CIB_KPI','CIB_KPI.sqlite','localhost',15
union all
select 'MISMobile'  ,'CIB_MARGIN','CIB_MARGIN.sqlite','localhost',15
union all
select 'MISMobile'  ,'RETAIL_ATM','RETAIL_ATM.sqlite','localhost',8810
union all
select 'MISMobile'  ,'CIB_ASSETS','CIB_ASSETS.sqlite','localhost',135
union all
select 'MISMobile'  ,'CIB_LIABILITIES','CIB_LIABILITIES.sqlite','localhost',24168
union all
select 'MISMobile'  ,'CIB_IBGM','CIB_IBGM.sqlite','localhost',15
union all
select 'MISMobile'  ,'CIB_KPKI','CIB_KPKI.sqlite','localhost',35636
union all
select 'MISMobile'  ,'CIB_PROFIT_ACTIVE','CIB_PROFIT_ACTIVE.sqlite','localhost',49812
union all
select 'MISMobile'  ,'CIB_PROFIT_PASSIVE','CIB_PROFIT_PASSIVE.sqlite','localhost',25329
union all
select 'MISMobile'  ,'CIB_PROFIT_PRODUCT','CIB_PROFIT_PRODUCT.sqlite','localhost',1318
union all
select 'MISMobile'  ,'CIB_PROFIT_CONCENTRATION','CIB_PROFIT_CONCENTRATION.sqlite','localhost',4051
union all
select 'MISMobile'  ,'CIB_PROFIT_TOTAL','CIB_PROFIT_TOTAL.sqlite','localhost',766
union all
select 'iup_rb'     ,'MIS_DASHBOARD_RB','mis_dashboard_rb2.sqlite','localhost',0
union all
select 'MISMobile'     ,'CIB_WEEK','CIB_WEEK.sqlite','localhost',0
union all
select 'MISMobile'     ,'CIB_PROFIT_PRODOTR','CIB_PROFIT_PRODOTR.sqlite','localhost',0
union all
select 'MISMobile'     ,'CIB_UTFIKO','CIB_UTFIKO.sqlite','localhost',0
union all
select 'MISMobile'     ,'CIB_KPKI_PROD','CIB_KPKI_PROD.sqlite','localhost',0
go

-- активируем автогенерацию для всех заданий по умолчанию
update SYNC_CACHE_STATIC_FILES set IS_AUTO_GEN_ENABLED = 1;
go
-- отключаем автогенерацию для баланса по умолчанию
update SYNC_CACHE_STATIC_FILES set IS_AUTO_GEN_ENABLED = 0 WHERE FILE_NAME = 'balance.sqlite';
go

-- set up default values (published and no md5)
update SYNC_CACHE_STATIC_FILES set 
	PUBLISHED_FILE_MD5 = NULL,
	DRAFT_FILE_MD5 = NULL,
	GENERATION_MODE = 0
GO 
