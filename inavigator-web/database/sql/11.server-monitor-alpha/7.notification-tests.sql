--1. Adding servers
SET NOCOUNT ON
DELETE FROM NOTIFICATION_SERVERS
INSERT INTO NOTIFICATION_SERVERS(SOURCE_HOST, SOURCE_APP, LAST_PING_TIME)
SELECT 'sbtrpu004','monitor',getdate()
GO

--2. Listing missing ping notications
exec SP_PING 'sbtrpu004','monitor'

--4. Listing no notifications
exec SP_LIST_NOTIFICATIONS 'finik2'
