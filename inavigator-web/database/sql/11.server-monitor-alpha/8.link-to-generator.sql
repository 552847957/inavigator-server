if object_id('SYNC_CACHE_STATIC_FILES', N'U') is not null drop table SYNC_CACHE_STATIC_FILES
GO

if object_id('SYNC_CACHE_STATIC_FILES', N'V') is not null drop view SYNC_CACHE_STATIC_FILES
GO

CREATE VIEW SYNC_CACHE_STATIC_FILES AS SELECT * FROM MIS_IPAD_GENERATOR..SYNC_CACHE_STATIC_FILES
GO

