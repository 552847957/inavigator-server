if object_id('SP_MIS_BASE2_GET_USERS_LIST') is not null DROP PROCEDURE SP_MIS_BASE2_GET_USERS_LIST
GO
if object_id('SP_MIS_BASE2_GET_INAV_ROLES') is not null DROP PROCEDURE SP_MIS_BASE2_GET_INAV_ROLES
GO
if object_id('SP_MIS_BASE2_GET_USER_INFO') is not null DROP PROCEDURE SP_MIS_BASE2_GET_USER_INFO
GO
if object_id('SP_MIS_BASE2_GET_USER_ROLES') is not null DROP PROCEDURE SP_MIS_BASE2_GET_USER_ROLES

if object_id('SP_MIS_BASE2_GET_BLOCKS') is not null DROP PROCEDURE SP_MIS_BASE2_GET_BLOCKS
GO
if object_id('SP_MIS_BASE2_GET_UNITS') is not null DROP PROCEDURE SP_MIS_BASE2_GET_UNITS
GO
if object_id('SP_MIS_BASE2_GET_TERRBANKS') is not null DROP PROCEDURE SP_MIS_BASE2_GET_TERRBANKS
GO
if object_id('SP_MIS_BASE2_UPDATE_USER') is not null DROP PROCEDURE SP_MIS_BASE2_UPDATE_USER
GO
if object_id('SP_MIS_BASE2_SPLIT_STRING') is not null DROP FUNCTION SP_MIS_BASE2_SPLIT_STRING
GO
if object_id('SP_MIS_BASE2_UPDATE_USER_ROLES') is not null DROP PROCEDURE SP_MIS_BASE2_UPDATE_USER_ROLES
GO



-- (1) Получить список пользователей
CREATE PROCEDURE SP_MIS_BASE2_GET_USERS_LIST
	@userName varchar(100),
	@ip varchar(100),
	@terrbankName varchar(100),
	@businessUnitId varchar(100),
	@withNavigatorRole bit,
	@selectAll bit
AS
	set nocount on
	
-- temp таблица с id ролей навигатора
if object_id('tempdb..#temp') is not null DROP TABLE #temp
create table #temp(id int)
insert #temp SELECT RoleId FROM mis_base2.dbo._tRole 
left join mis_base2.dbo._tRoleType on mis_base2.dbo._tRole.RoleTypeID = mis_base2.dbo._tRoleType.RoleTypeID 
where mis_base2.dbo._tRole.RoleTypeID in (9)

if (@selectAll>0)OR(@withNavigatorRole>0) begin
	Select
	cast(UserId as int) as UserId, 
	IP, 
	UserName, 
	case when ISNUMERIC(_tTerbank_1.TerbankShortName)= 1 then _tTerbank.TerbankShortName else _tTerbank_1.TerbankShortName end as TerbankShortName,
	BusinessUnitId
	FROM mis_base2.dbo._tUser
	LEFT OUTER JOIN mis_base2.dbo._tTerbank AS _tTerbank_1 ON _tUser.TerbankId = _tTerbank_1.TerbankId 
	LEFT OUTER JOIN mis_base2.dbo._tTerbank ON _tTerbank_1.TerbankParentId = _tTerbank.TerbankId
	WHERE	(UserName LIKE '%' + @username + '%' OR Comment LIKE '%' + @username + '%')
	AND CASE WHEN ISNUMERIC(_tTerbank_1.TerbankShortName)= 1 then _tTerbank.TerbankShortName else _tTerbank_1.TerbankShortName end LIKE '%' + @terrbankName + '%'
	AND IP LIKE '%' + @ip + '%'
	AND BusinessUnitId LIKE '%' + @businessUnitId + '%'
	AND not IP LIKE '%10.76.64%'
	AND (@withNavigatorRole = 0 OR (select count(_tRole2User.RoleId) from mis_base2.dbo._tRole2User where _tRole2User.UserId = _tUser.UserId AND (RoleId in (select id from #temp)))>0)
	order by UserID desc
end else begin
	Select TOP 40
	cast(UserId as int) as UserId, 
	IP, 
	UserName, 
	case when ISNUMERIC(_tTerbank_1.TerbankShortName)= 1 then _tTerbank.TerbankShortName else _tTerbank_1.TerbankShortName end as TerbankShortName,
	BusinessUnitId
	FROM mis_base2.dbo._tUser
	LEFT OUTER JOIN mis_base2.dbo._tTerbank AS _tTerbank_1 ON _tUser.TerbankId = _tTerbank_1.TerbankId 
	LEFT OUTER JOIN mis_base2.dbo._tTerbank ON _tTerbank_1.TerbankParentId = _tTerbank.TerbankId
	WHERE	(UserName LIKE '%' + @username + '%' OR Comment LIKE '%' + @username + '%')
	AND CASE WHEN ISNUMERIC(_tTerbank_1.TerbankShortName)= 1 then _tTerbank.TerbankShortName else _tTerbank_1.TerbankShortName end LIKE '%' + @terrbankName + '%'
	AND IP LIKE '%' + @ip + '%'
	AND BusinessUnitId LIKE '%' + @businessUnitId + '%'
	AND not IP LIKE '%10.76.64%'
	AND (@withNavigatorRole = 0 OR (select count(_tRole2User.RoleId) from mis_base2.dbo._tRole2User where _tRole2User.UserId = _tUser.UserId AND (RoleId in (select id from #temp)))>0)
	order by UserID desc
end
GO

-- (2) Список ролей навигатора
CREATE PROCEDURE SP_MIS_BASE2_GET_INAV_ROLES
AS
SELECT 
RoleId as id, RoleName as name, RoleDescription, RoleTypeDescription, mis_base2.dbo._tRole.RoleTypeID 
FROM 
mis_base2.dbo._tRole left join mis_base2.dbo._tRoleType on mis_base2.dbo._tRole.RoleTypeID = mis_base2.dbo._tRoleType.RoleTypeID 
where mis_base2.dbo._tRole.RoleTypeID in (9)
ORDER BY RoleName
GO

-- (3) Получить данные пользователя

CREATE PROCEDURE SP_MIS_BASE2_GET_USER_INFO
	@userId integer
AS
Select * FROM mis_base2.dbo._tUser WHERE UserId = @userId
GO
-- (4) Получить список ролей пользователя

CREATE PROCEDURE SP_MIS_BASE2_GET_USER_ROLES
	@userId integer
AS
SELECT _tRole2User.RoleId FROM mis_base2.dbo._tRole2User INNER JOIN mis_base2.dbo._tUser 
ON _tRole2User.UserId = _tUser.UserId WHERE _tRole2User.UserId  = @userId
GO

CREATE PROCEDURE SP_MIS_BASE2_GET_BLOCKS
AS
	SELECT BusinessBlockId, businessBlockName from mis_base2.dbo._tBusinessBlock order by businessBlockName
GO

CREATE PROCEDURE SP_MIS_BASE2_GET_UNITS
AS
	SELECT BusinessUnitId, businessUnitName from mis_base2.dbo._tBusinessUnit order by BusinessUnitId
GO

CREATE PROCEDURE SP_MIS_BASE2_GET_TERRBANKS
AS
	SELECT TerbankId, TerbankName from mis_base2.dbo._tTerbank order by TerbankId
GO


CREATE PROCEDURE SP_MIS_BASE2_UPDATE_USER 
	@userId integer,
	@userIp varchar(100),
	@userName varchar(100),
	@businessUnitId varchar(100),
	@email varchar(100),
	@position varchar(100),
	@terrbankId integer,
	@businessBlockId integer,
	@comment varchar(100),
	@emailAD varchar(100)
AS
SET NOCOUNT ON

IF (@userId IS NULL) BEGIN
	-- Валидация входных параметров
	create table #errors (
		error varchar(1000)
	)

	declare @a integer

	select @a = count(*) from mis_base2.dbo._tUser where IP = @userIp
	IF (@a > 0) BEGIN 
		insert into #errors values ('Указанный IP адрес уже используется в системе')
	END

	select @a = count(*) from mis_base2.dbo._tUser where Email = @email
	IF (@a > 0) BEGIN 
		insert into #errors values ('Указанный Sigma адрес почты уже используется в системе')
	END

	select @a = count(*) from mis_base2.dbo._tUser where EmailAD = @emailAD
	IF (@a > 0) BEGIN 
		insert into #errors values ('Указанный Alpha адрес почты уже используется в системе')
	END

	select @a = count(*) from #errors

	if (@a >0) BEGIN
	 select * from #errors
	 print 'Validation ERROR'
	 RETURN
	END
	print 'Validation OK'
END


-- (5) Добавление нового пользователя
if @userId IS NULL
	insert into mis_base2.dbo._tUser
		(IP,UserName,BusinessUnitId,Email,Position,TerbankId,BusinessBlockId,Comment,CreateDate,EmailAD)
	values 
		(@userIp,@userName,@businessUnitId,@email,@position,@terrbankId,@businessBlockId,@comment,GETDATE(),@emailAD)
ELSE
	update mis_base2.dbo._tUser
	set 
		IP = @userIp,
		UserName = @userName,
		BusinessUnitId = @businessUnitId,
		Email = @email,
		Position = @position,
		TerbankId = @terrbankId,
		BusinessBlockId = @businessBlockId,
		Comment = @comment,
		EmailAD = @emailAD
	where 
		UserId = @userId

	select CASE WHEN @userId IS NULL THEN @@IDENTITY ELSE @userId END as userId
GO

CREATE FUNCTION SP_MIS_BASE2_SPLIT_STRING(
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

CREATE PROCEDURE SP_MIS_BASE2_UPDATE_USER_ROLES
	@userId int,
	@deletedInavRoles varchar(MAX),
	@appendedInavRoles varchar(MAX)
AS
	SELECT ''
	DELETE FROM mis_base2.dbo._tRole2User WHERE USERID = @userId AND 
	RoleId in (select value from SP_MIS_BASE2_SPLIT_STRING(@deletedInavRoles,','))
	INSERT INTO mis_base2.dbo._tRole2User (UserId,RoleId)
	select @userId,value from SP_MIS_BASE2_SPLIT_STRING(@appendedInavRoles,',')
GO