IF DB_ID('MIS_RETAIL_ATM') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [MIS_RETAIL_ATM];
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT SELECT ON [MIS_RETAIL_ATM].dbo.[t 000 010 report dates] TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE MIS_RETAIL_ATM GRANTED'
END	
ELSE
	PRINT 'BASE MIS_RETAIL_ATM DOES NOT EXIST ON ' + @@SERVERNAME
GO	

IF DB_ID('MIS_BALANCE') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [MIS_BALANCE]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT EXECUTE ON [MIS_BALANCE].[dbo].[SP_IPAD_BALANCE_GENERATE_BALANCE_VALUES] TO [ipad_generator_user]
	GRANT EXECUTE ON [MIS_BALANCE].[dbo].[SP_IPAD_BALANCE_GENERATE_OPU_VALUES] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_BALANCE].[dbo].[t 000 Balance PNL Dept Mapping] TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE MIS_BALANCE GRANTED'
END	
ELSE
	PRINT 'BASE MIS_BALANCE DOES NOT EXIST ON ' + @@SERVERNAME
GO

IF DB_ID('MIS_RETAIL_CRED') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [MIS_RETAIL_CRED]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT SELECT ON [MIS_RETAIL_CRED].dbo.[t 000 030 ChanelType] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_RETAIL_CRED].dbo.[t 000 040 ClientType] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_RETAIL_CRED].dbo.[t 000 080 Volume Requests] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_RETAIL_CRED].dbo.[t 000 090 IndType] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_RETAIL_CRED].dbo.[t 000 200 report dates] TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE MIS_RETAIL_CRED GRANTED'
END	
ELSE
	PRINT 'BASE MIS_RETAIL_CRED DOES NOT EXIST ON ' + @@SERVERNAME
GO

IF DB_ID('MIS_PCA_DKK_DATA') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [MIS_PCA_DKK_DATA]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_002_Report_Periods] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_003_Report_Branches] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_004_Report_Territories] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_005_Report_Currencies] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_006_Report_Terms] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_008_Report_Products] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_008_Report_Products_kpki] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_009_000_Report_Clients] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_009_010_Report_Clients_In_Benefits] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_009_011_Report_Clients_In_Holdings] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_010_Report_Benefits] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_011_Report_Holdings] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_101_Agg_By_Territory_Mapping] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_102_Agg_By_Branch_Mapping] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_100_104_Agg_By_Product_Mapping] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_300_102_KPI_Indicators] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_PCA_DKK_DATA].dbo.[t_300_300_CIB_Profit_Concentration] TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE MIS_PCA_DKK_DATA GRANTED'
END	
ELSE
	PRINT 'BASE MIS_PCA_DKK_DATA DOES NOT EXIST ON ' + @@SERVERNAME
GO

IF DB_ID('MIS_MOBILE') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [MIS_MOBILE]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT SELECT ON [MIS_MOBILE].dbo.[stat_ch_hier] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[stat_client_hier] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[stat_prod_hier] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t 000 020 CredType] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t 000 020 hier_bank] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t 000 030 ATM type_heirarchy] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t 000 040 Transaction type_heirarchy] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t 000 090 IndType] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t 000 100 depart hierarchy] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t 002 005 norm] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t 002 007 norm] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t 100 010 Requests Pvt] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t 200 010 Volume Sums Pvt] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Actives_Concentration] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Actives_Concentration_Total] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Actives_Dynamics_CIB] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Actives_Dynamics_Corp] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Actives_Quality] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Actives_Slice_CIB] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Actives_Slice_Corp] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Actives_Term_Currency] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Actives_Term_Currency_Terr] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Actives_Term_Currency_Terr_Chart] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_IBGM] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_IBGM_default_params] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_KPI_AllData_Table] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_KPI_DefaultParams] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_KPKI_AllData_TableCredit_contract] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_KPKI_AllData_TableCredit_NoContract] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_KPKI_AllData_TableProduct] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_KPKI_Contract] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_KPKI_DefaultParams] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_KPKI_Mapping_GroupClient] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_MARGIN_blocksData] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_MARGIN_blocksData_plan] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_MARGIN_cib_products] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_MARGIN_CIBData] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_MARGIN_CIBData_plan] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_MARGIN_Default_Params] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Passives_Concentration_Client] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Passives_Concentration_Client_Bank] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Passives_Concentration_Total] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Passives_Dynamics_CIB] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Passives_Dynamics_Corp] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Passives_Slice_CIB] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Passives_Slice_Corp] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Passives_Term_Curr_Terr] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_cib_product_gm] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_cib_product_ib] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_cib_product_mb] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_cib_products_commissions] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_PROFIT_ACTIVE_all_contracts] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_PROFIT_ACTIVE_cl_bagg] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_PROFIT_ACTIVE_clients_mapping] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_PROFIT_ACTIVE_contracts] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_PROFIT_ACTIVE_Default_Params] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_PROFIT_ACTIVE_sec_cl] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_PROFIT_ACTIVE_sec_contracts] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_PROFIT_CONCENTRATION_Default_Params] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_PROFIT_CONCENTRATION_sums] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Profit_Passive_Clients] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Profit_Passive_Contracts] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Profit_Passive_Default_Params] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Profit_Products_Default_Params] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Profit_Total] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_Profit_Total_Default_Params] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_MOBILE].dbo.[t_900_CIB_EXECUTE_Date] TO [ipad_generator_user]
	GRANT EXECUTE ON [MIS_MOBILE].dbo.[f_mobile_upd_cib_lastupdate] TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE MIS_MOBILE GRANTED'
END	
ELSE
	PRINT 'BASE MIS_MOBILE DOES NOT EXIST ON ' + @@SERVERNAME
GO

IF DB_ID('mis_competitors') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [mis_competitors]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT SELECT ON [mis_competitors].dbo.[miscc_bank] TO [ipad_generator_user]
	GRANT SELECT ON [mis_competitors].dbo.[miscc_data] TO [ipad_generator_user]
	GRANT SELECT ON [mis_competitors].dbo.[miscc_metric] TO [ipad_generator_user]
	GRANT SELECT ON [mis_competitors].dbo.[miscc_metric_vs] TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE mis_competitors GRANTED'
END	
ELSE
	PRINT 'BASE mis_competitors DOES NOT EXIST ON ' + @@SERVERNAME
GO

IF DB_ID('dashboard_ppr_mobile') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [dashboard_ppr_mobile]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[block] TO [ipad_generator_user]
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[data] TO [ipad_generator_user]
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[Files] TO [ipad_generator_user]
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[ForecastDates] TO [ipad_generator_user]
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[General_values] TO [ipad_generator_user]
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[goal] TO [ipad_generator_user]
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[goal_type] TO [ipad_generator_user]
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[Mark5Plus_data] TO [ipad_generator_user]
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[Mark5PlusCompetences] TO [ipad_generator_user]
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[measure] TO [ipad_generator_user]
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[MyMark_data] TO [ipad_generator_user]
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[person] TO [ipad_generator_user]
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[person_in_year] TO [ipad_generator_user]
	GRANT SELECT ON [dashboard_ppr_mobile].[dbo].[position] TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE dashboard_ppr_mobile GRANTED'
END	
ELSE
	PRINT 'BASE dashboard_ppr_mobile DOES NOT EXIST ON ' + @@SERVERNAME
GO

IF DB_ID('mis_navigator_kpi') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [mis_navigator_kpi]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_NAV_KPI_DASHBOARD_DEFAULT_PARAMS] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_200] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_200_W] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_201] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_201_W] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_202] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_202_W] TO [ipad_generator_user]	
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_203] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_203_W] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_205] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_205_W] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_206] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_206_W] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_208] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_navigator_kpi].[dbo].[GET_MOBILE_PUB_NAV_DATA_208_W] TO [ipad_generator_user]	
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_ARROW_POINT] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_CARD] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_DASHBOARD2ROLE] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_DASHBOARD] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_DASHBOARD_DOMAINS] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_DASHBOARD_MENU] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_DOMAIN_CARDS] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_DOMAINS] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_ENTERPOINT_DASHBOARD] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_MENU] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_MENU_HIER] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_MENU_ITEM] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_MENU_ITEM_CUSTOM_PARAMS] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_MENU_ITEM_PARAM] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_MENU_ITEM_PARAMS] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_TREE_ARROW] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_TREE_CARD_HIER] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[NAV_KPI_TREE_CARDS] TO [ipad_generator_user]
	GRANT SELECT ON [mis_navigator_kpi].[dbo].[PUB_NAV_KPI_POKS] TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE mis_navigator_kpi GRANTED'
END	
ELSE
	PRINT 'BASE mis_navigator_kpi DOES NOT EXIST ON ' + @@SERVERNAME
GO

IF DB_ID('MIS_NAVIGATOR_DATA') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [MIS_NAVIGATOR_DATA]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_AXIS] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_AXISATRIBUTEVALUE] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_AXISATTRIBUTE] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_AXISDETAIL] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_KPI_POK] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_MEASURE] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_SLICE] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_SLICEDETAIL] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_DATA] TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE MIS_NAVIGATOR_DATA GRANTED'
END	
ELSE
	PRINT 'BASE MIS_NAVIGATOR_DATA DOES NOT EXIST ON ' + @@SERVERNAME
GO

IF DB_ID('mis_iup_kpi') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [mis_iup_kpi]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT EXECUTE ON [mis_iup_kpi].[dbo].[GET_MOBILE_DATA] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_iup_kpi].[dbo].[GET_MOBILE_DEPARTMENT] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_iup_kpi].[dbo].[GET_MOBILE_KPI] TO [ipad_generator_user]
	GRANT EXECUTE ON [mis_iup_kpi].[dbo].[GET_MOBILE_MEASURE] TO [ipad_generator_user]
	GRANT SELECT ON [mis_iup_kpi].[dbo].[Dashboards] TO [ipad_generator_user]
	GRANT SELECT ON [mis_iup_kpi].[dbo].[Default_params] TO [ipad_generator_user]
	GRANT SELECT ON [mis_iup_kpi].[dbo].[Iup_role] TO [ipad_generator_user]
	GRANT SELECT ON [mis_iup_kpi].[dbo].[Links] TO [ipad_generator_user]
	GRANT SELECT ON [mis_iup_kpi].[dbo].[UnitIdDeprt] TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE mis_iup_kpi GRANTED'
END	
ELSE
	PRINT 'BASE mis_iup_kpi DOES NOT EXIST ON ' + @@SERVERNAME
GO

IF DB_ID('mis_iup_data') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [mis_iup_data]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	GRANT SELECT ON [MIS_IUP_DATA].[dbo].[measure] TO ipad_generator_user
	GRANT SELECT ON [MIS_IUP_DATA].[dbo].[kpi] TO ipad_generator_user
	GRANT SELECT ON [MIS_IUP_DATA].[dbo].[department] TO ipad_generator_user
	GRANT SELECT ON [MIS_IUP_DATA].[dbo].[data] TO ipad_generator_user'
	EXEC (@sql)
	PRINT 'BASE mis_iup_data GRANTED'
END	
ELSE
	PRINT 'BASE mis_iup_data DOES NOT EXIST ON ' + @@SERVERNAME
GO

IF DB_ID('mis_prognoz_sb_data') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [mis_prognoz_sb_data]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[BANKS_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[COMPETITION_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[COMPETITION_VALUE_TYPE_CATEGORIES] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[COMPETITION_VALUE_TYPES] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[DATA_TYPES] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PENETRATION_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PENETRATION_DATA_VALUE_TYPES] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_AUCTIONS] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_CALENDAR_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_CARDS] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_CARDS_POKS] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_CIB_BRANCH_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_COMM_TYPE_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_CORP_HR_STRUCT] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_DATAMART] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_INDICATORS_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_INFORMATION_EMPLOYEES] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_LOAN_TYPE_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_MAP_APP] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_ORG_STRUCTURE_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_PCA_CIB_COMM_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_PCA_CIB_DEPO_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_PCA_CIB_DEPO_S_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_PCA_CIB_LOANS_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_PCA_CIB_ProductsGM_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_PCA_CIB_ProductsIB_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_PCA_CIB_RKO_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_PCA_CIB_SEC_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_PCA_CIB_VEKS_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_PHOTO_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_PRODUCTS_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_PROJECTS] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_RISKGROUP_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_RISKPROFILE_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_RISKPROFILE_TYPE_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_SALARY_PROJECTS] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_SEGMENTS_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_SLICES_CARDS_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_SLICES_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_SLICES_POKS_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_SLICES_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_TERR_REF] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[PR_TOP10_DATA] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[SEGMENTS_DATA_ECONOMY_COMM] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[SEGMENTS_DATA_ECONOMY_CREDITS] TO [ipad_generator_user]
	GRANT SELECT ON [mis_prognoz_sb_data].[dbo].[SEGMENTS_DATA_ECONOMY_DEPOSITS] TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE mis_prognoz_sb_data GRANTED'
END	
ELSE
	PRINT 'BASE mis_prognoz_sb_data DOES NOT EXIST ON ' + @@SERVERNAME
GO

IF DB_ID('MIS_NAVIGATOR_DATA') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [MIS_NAVIGATOR_DATA]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_AXIS] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_AXISATRIBUTEVALUE] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_AXISATTRIBUTE] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_AXISDETAIL] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_KPI_POK] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_MEASURE] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_SLICE] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_SLICEDETAIL] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_NAVIGATOR_DATA].[dbo].[PUB_NAV_DATA] TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE MIS_NAVIGATOR_DATA GRANTED'
END	
ELSE
	PRINT 'BASE MIS_NAVIGATOR_DATA DOES NOT EXIST ON ' + @@SERVERNAME
GO

IF DB_ID('MIS_DASHBOARD_RB_KPI') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [MIS_DASHBOARD_RB_KPI]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT EXECUTE ON [MIS_DASHBOARD_RB_KPI].[dbo].[GET_LAST_LOAD_DATE] TO [ipad_generator_user]
	GRANT SELECT  ON [MIS_DASHBOARD_RB_KPI].[dbo].[default_params]    TO [ipad_generator_user]
	GRANT SELECT  ON [MIS_DASHBOARD_RB_KPI].[dbo].[start_screen]      TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE MIS_DASHBOARD_RB_KPI GRANTED'
END	
ELSE
	PRINT 'BASE MIS_DASHBOARD_RB_KPI DOES NOT EXIST ON ' + @@SERVERNAME
GO

IF DB_ID('MIS_DASHBOARD_RB') IS NOT NULL 
BEGIN
	DECLARE @sql VARCHAR(MAX) = '
	USE [MIS_DASHBOARD_RB]
	if not exists ( select ''ipad_generator_user'' from sys.database_principals where name = ''ipad_generator_user'')
		CREATE USER ipad_generator_user FOR LOGIN ipad_generator_user;
	--EXEC SP_ADDROLEMEMBER ''PUBLIC'', ''ipad_generator_user''
	GRANT SELECT ON [MIS_DASHBOARD_RB].[dbo].[DATA] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_DASHBOARD_RB].[dbo].[DEPRT] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_DASHBOARD_RB].[dbo].[KPI] TO [ipad_generator_user]
	GRANT SELECT ON [MIS_DASHBOARD_RB].[dbo].[MEASURE] TO [ipad_generator_user]'
	EXEC (@sql)
	PRINT 'BASE MIS_DASHBOARD_RB GRANTED'
END	
ELSE
	PRINT 'BASE MIS_DASHBOARD_RB DOES NOT EXIST ON ' + @@SERVERNAME
GO