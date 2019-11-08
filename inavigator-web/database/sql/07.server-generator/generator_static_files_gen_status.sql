if object_id('SP_SYNC_STATIC_FILES_GEN_START') is not null DROP PROCEDURE SP_SYNC_STATIC_FILES_GEN_START
go
if object_id('SP_SYNC_STATIC_FILES_GEN_ADD_STATE') is not null DROP PROCEDURE SP_SYNC_STATIC_FILES_GEN_ADD_STATE
go
if object_id('SP_SYNC_STATIC_FILES_GEN_GET_CURRENT_STATE') is not null DROP PROCEDURE SP_SYNC_STATIC_FILES_GEN_GET_CURRENT_STATE
go
if object_id('SP_SPLIT_STRING') is not null DROP FUNCTION SP_SPLIT_STRING
go
if object_id('SP_SYNC_STATIC_FILES_GET_FILES_AND_HOSTS') is not null DROP PROCEDURE SP_SYNC_STATIC_FILES_GET_FILES_AND_HOSTS
go


if object_id('SYNC_STATIC_FILES_GEN_HISTORY') is not null DROP TABLE SYNC_STATIC_FILES_GEN_HISTORY
GO
if object_id('SYNC_STATIC_FILES_GEN_STATUS') is not null DROP TABLE SYNC_STATIC_FILES_GEN_STATUS
GO
if object_id('SYNC_STATIC_FILES_GEN_PHASE') is not null DROP TABLE SYNC_STATIC_FILES_GEN_PHASE
GO

-- Список статусов, в которых может находить фаза выполнения
CREATE TABLE SYNC_STATIC_FILES_GEN_STATUS(
 GEN_STATUS_ID INT IDENTITY(1,1) PRIMARY KEY,
 GEN_STATUS_CODE VARCHAR(50) NOT NULL,
 NAME VARCHAR(128) NOT NULL 
)
GO

--список фаз генерации файла
CREATE TABLE SYNC_STATIC_FILES_GEN_PHASE(
 GEN_PHASE_ID INT IDENTITY(1,1) PRIMARY KEY,
 GEN_PHASE_CODE VARCHAR(50) NOT NULL,
 NAME VARCHAR(128) NOT NULL 
)
GO

--  История статусов генерации заданий
CREATE TABLE SYNC_STATIC_FILES_GEN_HISTORY (
  GEN_HISTORY_ID INT IDENTITY(1,1) PRIMARY KEY,
  GEN_PHASE_ID INT REFERENCES SYNC_STATIC_FILES_GEN_PHASE(GEN_PHASE_ID) NOT NULL,
  GEN_STATUS_ID INT REFERENCES SYNC_STATIC_FILES_GEN_STATUS(GEN_STATUS_ID) NOT NULL,
  GEN_HISTORY_DATE DATETIME NOT NULL,
  DATA_FILE_NAME VARCHAR(100) NOT NULL,
  COMMENT VARCHAR(200) NOT NULL,
  SIGMA_HOST VARCHAR(50) NULL,
  WEB_HOST_NAME VARCHAR(100) NULL
)
GO

-- Заполняем справочник статусов
INSERT INTO SYNC_STATIC_FILES_GEN_STATUS
SELECT 		'PERFORM',					'В процессе'
UNION ALL
SELECT 		'COMPLETED_SUCCESSFULLY',	'Завершен успешно'
UNION ALL
SELECT		'COMPLETED_ERROR',			'Завершен с ошибкой'
UNION ALL
SELECT		'CANCELED_BY_USER',			'Отменен  пользователем'
GO

-- заполняем справочник ФАЗ
INSERT INTO SYNC_STATIC_FILES_GEN_PHASE
SELECT		'DB_CONNECT',		'Подключение к БД'
UNION ALL
SELECT 		'FILE_GEN',			'Генерация Файла'
UNION ALL
SELECT 		'SEND_TO_SIGMA',	'Отправка в сигма'
UNION ALL
SELECT		'LOADED_TO_SIGMA',	'Кеширование в Сигма'
GO

-- Процедура очистки записей-логов генерации для указнного имени файла
-- Должна быть вызвана перед началом генерации файла
CREATE PROCEDURE SP_SYNC_STATIC_FILES_GEN_START
  @data_file_name varchar(256)
AS
	DELETE FROM SYNC_STATIC_FILES_GEN_HISTORY WHERE DATA_FILE_NAME = @data_file_name
GO

-- Процедура Добавление новой записи при генерации файла
CREATE PROCEDURE SP_SYNC_STATIC_FILES_GEN_ADD_STATE
  @data_file_name varchar(256),
  @phase_code varchar(256),
  @status_code varchar(256),
  @comment varchar(256),
  @sigmahost varchar(256),
  @webhost varchar(256)
AS
	declare @status_id int
	declare @phase_id int
	declare @has_messages int

	select @status_id = GEN_STATUS_ID from SYNC_STATIC_FILES_GEN_STATUS where GEN_STATUS_CODE = @status_code
	select @phase_id = GEN_PHASE_ID from SYNC_STATIC_FILES_GEN_PHASE where GEN_PHASE_CODE = @phase_code
	select @has_messages=COUNT(*) from SYNC_STATIC_FILES_GEN_HISTORY WHERE DATA_FILE_NAME=@data_file_name
	
	-- если событие связано с переносом файла в ФП на альфе или загрузкой на сигме, 
	-- то Сохраняем событие только в том случае, если для файла уже были сообщения
	-- Необходимо для того чтобы исключить события связанные с changeSet-тами
	IF (@has_messages!=0 OR (@phase_code != 'SEND_TO_SIGMA' AND @phase_code != 'LOADED_TO_SIGMA'))  
	BEGIN 
		INSERT INTO SYNC_STATIC_FILES_GEN_HISTORY 
			(GEN_PHASE_ID,GEN_STATUS_ID,GEN_HISTORY_DATE,DATA_FILE_NAME,COMMENT,SIGMA_HOST,WEB_HOST_NAME) 
		VALUES
			(@phase_id,@status_id,getdate(),@data_file_name,@comment,@sigmahost,@webhost)
	END
GO

-- процедура возвращает курсор подстрок исходной строки разбитой по разделителю
-- (К сожалению  в MSSQL нет встроенной SPLIT функции)
CREATE FUNCTION SP_SPLIT_STRING(
	@List NVARCHAR(MAX),
	@Delim VARCHAR(255)
) 
RETURNS TABLE
AS
	RETURN ( SELECT value FROM ( 
		SELECT 
			value = LTRIM(RTRIM(SUBSTRING(@List, [Number],
			CHARINDEX(@Delim, @List + @Delim, [Number]) - [Number])))
			FROM (SELECT Number = ROW_NUMBER() OVER (ORDER BY name)
			FROM sys.all_objects) AS x
			WHERE Number <= LEN(@List)
			AND SUBSTRING(@Delim + @List, [Number], LEN(@Delim)) = @Delim
		) AS y
	);
GO

-- Процедура возвращает список файлов и хостов в которые эти файлы должны быть доставлены
-- берется множество файлов которые есть в таблице SYNC_STATIC_FILES_GEN_HISTORY (то есть те, для которых уже есть уведомления)
-- и анализируется список хостов в SYNC_CACHE_STATIC_FILES для каждого файла
create procedure SP_SYNC_STATIC_FILES_GET_FILES_AND_HOSTS
AS
	if object_id('#tmp_files_hosts') is not null DROP TABLE #tmp_files_hosts
	create table #tmp_files_hosts (file_name varchar(100),host varchar(100))
	if object_id('cursor1') is not null DEALLOCATE cursor1
	DECLARE cursor1 CURSOR FOR select FILE_NAME,HOSTS from  SYNC_CACHE_STATIC_FILES a where a.FILE_NAME in (select distinct data_file_name from SYNC_STATIC_FILES_GEN_HISTORY)
	OPEN cursor1;
	declare @a varchar(100),@b varchar(100);
	fetch next from cursor1 into @a,@b;
	while @@FETCH_STATUS = 0 
	BEGIN
		insert into #tmp_files_hosts
		select @a,value from SP_SPLIT_STRING(@b,';')
		fetch next from cursor1 into @a,@b;
	END
	CLOSE cursor1
	deallocate cursor1 

	select * from #tmp_files_hosts
GO

-- Процедура возвращает список статусов каждого этапа для каждого файла у которого есть события генерации
CREATE PROCEDURE SP_SYNC_STATIC_FILES_GEN_GET_CURRENT_STATE
AS
	SET NOCOUNT ON
	declare @load_to_sigma_phase_code varchar(100),@load_to_sigma_phase_name varchar(100),@load_to_sigma_phase_id int
	SELECT 
		@load_to_sigma_phase_code = GEN_PHASE_CODE,
		@load_to_sigma_phase_name = NAME, 
		@load_to_sigma_phase_id = GEN_PHASE_ID
	FROM 
		SYNC_STATIC_FILES_GEN_PHASE 
	WHERE 
		GEN_PHASE_CODE='LOADED_TO_SIGMA'
	
	if object_id('#tmp_files_hosts') is not null DROP TABLE #tmp_files_hosts
	create table #tmp_files_hosts (file_name varchar(100),host varchar(100))
	insert into #tmp_files_hosts exec SP_SYNC_STATIC_FILES_GET_FILES_AND_HOSTS

	SELECT 
		t1.GEN_PHASE_CODE PHASE_CODE, t1.NAME PHASE_NAME,
		t4.GEN_STATUS_CODE STATUS_CODE ,t4.NAME STATUS_NAME,
		t2.GEN_HISTORY_DATE GEN_HISTORY_DATE,
		t2.DATA_FILE_NAME FILE_NAME,
		t2.SIGMA_HOST SIGMA_HOST,
		t2.WEB_HOST_NAME WEB_HOST_NAME	
	FROM 
		SYNC_STATIC_FILES_GEN_PHASE t1
		LEFT JOIN  SYNC_STATIC_FILES_GEN_HISTORY t2 ON 	t2.GEN_PHASE_ID = t1.GEN_PHASE_ID  AND t2.GEN_HISTORY_ID = (SELECT MAX(GEN_HISTORY_ID) FROM SYNC_STATIC_FILES_GEN_HISTORY t3 WHERE t3.GEN_PHASE_ID = t1.GEN_PHASE_ID AND t3.DATA_FILE_NAME = t2.DATA_FILE_NAME)
		JOIN SYNC_STATIC_FILES_GEN_STATUS t4 on t4.GEN_STATUS_ID = t2.GEN_STATUS_ID OR t2.GEN_STATUS_ID IS NULL 
		WHERE  t1.GEN_PHASE_CODE != @load_to_sigma_phase_code
	
	UNION ALL
	
	SELECT 
		@load_to_sigma_phase_code PHASE_CODE,@load_to_sigma_phase_name PHASE_NAME,
		t3.GEN_STATUS_CODE STATUS_CODE,t3.NAME STATUS_NAME,
		t2.GEN_HISTORY_DATE GEN_HISTORY_DATE,
		t1.file_name FILE_NAME,
		t1.HOST SIGMA_HOST,
		t2.WEB_HOST_NAME WEB_HOST_NAME
	FROM 
		#tmp_files_hosts t1
		left join SYNC_STATIC_FILES_GEN_HISTORY t2 
		inner join SYNC_STATIC_FILES_GEN_STATUS t3 on t3.GEN_STATUS_ID = t2.GEN_STATUS_ID
		on t2.data_file_name = t1.file_name and t2.GEN_PHASE_ID = 4 AND t2.SIGMA_HOST = t1.HOST AND t2.GEN_HISTORY_ID IN
		(select MAX(t2.GEN_HISTORY_ID) FROM SYNC_STATIC_FILES_GEN_HISTORY t2 
		inner join SYNC_STATIC_FILES_GEN_STATUS t3 on t3.GEN_STATUS_ID = t2.GEN_STATUS_ID
		WHERE t2.GEN_PHASE_ID = @load_to_sigma_phase_id
		GROUP BY t2.DATA_FILE_NAME,t2.SIGMA_HOST)	
GO