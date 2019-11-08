declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out, 'ru.sberbank.syncserver2.service.file.cache.list.SimpleFileLister'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'quottesApplicationId'		,'N','application id for quottes'
go

declare @id int
exec SP_SYNC_ADD_TEMPLATE2 @id out     , 'ru.sberbank.syncserver2.service.upload.FileUploadToCacheService'
exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'quottesFileLoader'		,'N','bean name of single file loader fro quottes'
go

set identity_insert SYNC_FOLDERS on
insert into SYNC_FOLDERS(SYNC_FOLDER_ID,SYNC_FOLDER_CODE,START_ORDER,SYNC_FOLDER_DESC) values(4,'quottes', 4,'This folder contains services for quottes')
set identity_insert SYNC_FOLDERS off
go

--4.4. Filling single services
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE      ,PARENT_BEAN_PROPERTY ,START_ORDER,SERVLET_PATH, BEAN_DESC)
values(                   203             ,'quottes'        ,'quottesFileLister'        ,'ru.sberbank.syncserver2.service.file.cache.list.SimpleFileLister'         ,'quottesFileLoader'       ,'fileLister'      ,203         ,null           , 'This service queries database to get hostnames the file should be moved to')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY    ,START_ORDER,SERVLET_PATH   , BEAN_DESC)
values(                   204             ,'quottes'        ,'quottesFileLoader'        ,'ru.sberbank.syncserver2.service.file.cache.SingleFileLoader'                  ,'quottesFileCache'  ,'loader'                ,204         ,null           , 'This service split files to chunks and loads them to memory (to fileCache service)' )
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY    ,START_ORDER,SERVLET_PATH   , BEAN_DESC)
values(                   205             ,'quottes'        ,'quottesFileCache'        ,'ru.sberbank.syncserver2.service.file.cache.FileCache'                          ,'quottesFileService','fileCache'             ,205         ,null           , 'This service stores data in memory and reply to requests from misFileService')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY    ,START_ORDER,SERVLET_PATH   , BEAN_DESC)
values(                   206             ,'quottes'        ,'quottesFileService'       ,'ru.sberbank.syncserver2.service.file.FileService'                             ,null           ,null                    ,206         , 'download.do'      , 'This service accepts requests from iPad')
insert into SYNC_SERVICES(SYNC_SERVICE_ID,SYNC_FOLDER_CODE,BEAN_CODE              ,BEAN_CLASS                                                                     ,PARENT_BEAN_CODE,PARENT_BEAN_PROPERTY    ,START_ORDER,SERVLET_PATH   , BEAN_DESC)
values(                   207             ,'quottes'        ,'quottesFileUploadService'       ,'ru.sberbank.syncserver2.service.upload.FileUploadToCacheService'                             ,null           ,null                    ,207         , 'fileUpload.do'      , 'This service accepts requests from iPad')

--4.2.3. For misFileLoader
exec SP_SYNC_RESET_PROPERTIES 'quottes','quottesFileLoader'
exec SP_SYNC_SET_PROPERTY     'quottes','quottesFileLoader','chunkSize','1048576'
exec SP_SYNC_SET_PROPERTY     'quottes','quottesFileLoader','inboxFolder', '@ROOT_FOLDER@/@DB_NAME@/quottes/local/inflater/inflated'
exec SP_SYNC_SET_PROPERTY     'quottes','quottesFileLoader','tempFolder', '@ROOT_FOLDER@/@DB_NAME@/quottes/local/loader/temp'
exec SP_SYNC_SET_PROPERTY     'quottes','quottesFileLoader','archiveFolder', '@ROOT_FOLDER@/@DB_NAME@/quottes/local/loader/archive'
exec SP_SYNC_SET_PROPERTY     'quottes','quottesFileLoader','cacheFolder', '@ROOT_FOLDER@/@DB_NAME@/quottes/local/fileCache'

--4.2.3. For misFileLoader
exec SP_SYNC_RESET_PROPERTIES 'quottes','quottesFileLister'
exec SP_SYNC_SET_PROPERTY     'quottes','quottesFileLister','quottesApplicationId','QuottesApplication'

--4.2.3. For misFileLoader
exec SP_SYNC_RESET_PROPERTIES 'quottes','quottesFileUploadService'
exec SP_SYNC_SET_PROPERTY     'quottes','quottesFileUploadService','quottesFileLoader','quottesFileLoader'
