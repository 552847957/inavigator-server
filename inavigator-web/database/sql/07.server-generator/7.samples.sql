
--1. Listing folders
exec SP_SYNC_LIST_FOLDERS
go

--2. Listing services for folder
exec SP_SYNC_LIST_SERVICES 'generator'
go

--3. Listing properties for service
exec SP_SYNC_LIST_PROPERTIES 'generator','singleGenerator'
go
