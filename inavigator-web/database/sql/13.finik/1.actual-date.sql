if object_id('SP_IPAD_GET_ACTUAL_DATE') is not null drop procedure SP_IPAD_GET_ACTUAL_DATE
go

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
  end else if @report_code='MIS_NAVIGATOR_KPI_D' begin
    select @actual_time=[MIS_NAVIGATOR_DATA].[dbo].GetLastLoadDate (5)
  end else if @report_code='MIS_NAVIGATOR_KPI_LIQ' begin
    select @actual_time=[MIS_NAVIGATOR_DATA].[dbo].GetLastLoadDate (3)
  end else if @report_code='MIS_NAVIGATOR_KPI_LIQ_W' begin
    select @actual_time=[MIS_NAVIGATOR_DATA].[dbo].GetLastLoadDate (4)
  end else if @report_code='MIS_NAVIGATOR_KPI_LIQ_D' begin
    select @actual_time=[MIS_NAVIGATOR_DATA].[dbo].GetLastLoadDate (5)
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
