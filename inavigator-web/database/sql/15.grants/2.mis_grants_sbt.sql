USE [MIS_BALANCE]
GO
GRANT SELECT ON [MIS_BALANCE].[dbo].[t 000 Balance PNL Dept Mapping] TO [ipad_generator_user]
GRANT EXECUTE ON [MIS_BALANCE].[dbo].SP_IPAD_BALANCE_GENERATE_OPU_VALUES TO [ipad_generator_user]
GRANT EXECUTE ON [MIS_BALANCE].[dbo].SP_IPAD_BALANCE_GENERATE_BALANCE_VALUES TO [ipad_generator_user]
GO
USE mis_competitors
GO 
GRANT SELECT ON mis_competitors.dbo.miscc_bank TO [ipad_generator_user]
GRANT SELECT ON mis_competitors.dbo.miscc_metric_vs TO [ipad_generator_user]
GRANT SELECT ON mis_competitors.dbo.miscc_metric TO [ipad_generator_user]
GRANT SELECT ON mis_competitors.dbo.miscc_data TO [ipad_generator_user]
GO

USE [MIS_COMPETITORS_NT]
GRANT EXECUTE ON [MIS_COMPETITORS_NT].[dbo].[f_GetActualReportDate] TO [ipad_generator_user]
GO
