package ru.sberbank.syncserver2.gui.db.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.sberbank.syncserver2.service.pushnotifications.model.*;
import ru.sberbank.syncserver2.service.pushnotifications.senders.BaseNotificationSender.PushedNotificationInfo;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Набор методов для поддержки работы Push уведомлений 
 * @author sbt-gordienko-mv
 *
 */

@Component
public class PushNotificationDao {
	@Autowired(required = false)
	@Qualifier("pushSource")
	protected DataSource pushDataSource;

	@Autowired
	@Qualifier("configSource")
	protected DataSource alternativeDataSource;

	protected JdbcTemplate jdbcTemplate;

	public DataSource getAlternativeDataSource() {
		return alternativeDataSource;
	}

	public void setAlternativeDataSource(DataSource alternativeDataSource) {
		this.alternativeDataSource = alternativeDataSource;
	}

	public DataSource getPushDataSource() {
		return pushDataSource;
	}

	public void setPushDataSource(DataSource pushDataSource) {
		this.pushDataSource = pushDataSource;
	}

	@PostConstruct
	public void init() {
		jdbcTemplate = new JdbcTemplate(pushDataSource != null ? pushDataSource : alternativeDataSource);
	}

	private String decorateStringListForWhereInClause(String list) {
		String result = "";
		if (list != null && !list.equals(""))
			result =  "'" + list.replace(",", "','") + "'";
		return result;
	}

	public static String decorateStringListForWhereInClause(String list[]) {
		String result = "";
		if (list != null && list.length != 0) {
			boolean first = true;
			for(String element:list) {
				if (!first)
					result = result + ",";
				result = result + "'" + element + "'";
				first = false;
			}
		}
		return result;
	}
	
	public List<SourcePushStatus> getStatusesForUpdate() {
		List<SourcePushStatus> result = jdbcTemplate.query("exec GET_NOTIFICATIONS_FOR_UPDATE_STATUS", new RowMapper<SourcePushStatus>() {

			@Override
			public SourcePushStatus mapRow(ResultSet arg0, int arg1) throws SQLException {
				return new SourcePushStatus(arg0.getLong("SYNC_SOURCE_PN_ID"), arg0.getInt("SUCCESS"));
			}
			
		});
		
		return result;
	}
	
	public static class SourcePushStatus {
		private final long id;
		private final int successCount;
		public SourcePushStatus(long id, int successCount) {
			super();
			this.id = id;
			this.successCount = successCount;
		}
		public long getId() {
			return id;
		}
		public int getSuccessCount() {
			return successCount;
		}
	}
	
	
	public List<DictionaryItem> getEmails() {
		List<DictionaryItem> result = jdbcTemplate.query("exec GET_EMAILS ?", new Object[] {""}, new RowMapper<DictionaryItem>() {

			@Override
			public DictionaryItem mapRow(ResultSet arg0, int arg1) throws SQLException {
				return new DictionaryItem(arg0.getString("ITEM_CODE"),arg0.getString("ITEM_CODE"));
			}
			
		});
		
		return result;
	}

	public List<DictionaryItem> getDevices(String email) {
		if (email == null || email.trim().isEmpty())
			return Collections.emptyList();
		List<DictionaryItem> result = jdbcTemplate.query("exec GET_DEVICES ?", new Object[] {decorateStringListForWhereInClause(email)}, new RowMapper<DictionaryItem>() {

			@Override
			public DictionaryItem mapRow(ResultSet arg0, int arg1) throws SQLException {
				return new DictionaryItem(arg0.getString("ITEM_NAME"),arg0.getString("ITEM_CODE"));
			}
			
		});
		
		return result;
	}	
	
	
	public List<DictionaryItem> getOsCodes() {
		List<DictionaryItem> result = jdbcTemplate.query("exec GET_OS_CODES", new RowMapper<DictionaryItem>() {

			@Override
			public DictionaryItem mapRow(ResultSet arg0, int arg1) throws SQLException {
				return new DictionaryItem(arg0.getString("ITEM_CODE"),arg0.getString("ITEM_CODE"));
			}
			
		});
		
		return result;
	}
	
	public List<DictionaryItem> getApplications(String osCode) {
		List<DictionaryItem> result = jdbcTemplate.query("exec GET_APPLICATIONS ?", new Object[] {osCode}, new RowMapper<DictionaryItem>() {

			@Override
			public DictionaryItem mapRow(ResultSet arg0, int arg1) throws SQLException {
				return new DictionaryItem(arg0.getString("ITEM_CODE"),arg0.getString("ITEM_CODE"));
			}
			
		});
		
		return result;
	}

	
	public List<DictionaryItem> getApplicationVersions(String osCode,String appCode) {
		List<DictionaryItem> result = jdbcTemplate.query("exec GET_APPLICATION_VERSIONS ?,?", new Object[] {osCode,appCode}, new RowMapper<DictionaryItem>() {

			@Override
			public DictionaryItem mapRow(ResultSet arg0, int arg1) throws SQLException {
				return new DictionaryItem(arg0.getString("ITEM_CODE"),arg0.getString("ITEM_CODE"));
			}
			
		});
		
		return result;
	}

    /**
     * Добавить Push уведомление 
     * @param notification
     */
    public Long addPushNotification(final PushNotification notification) {
    	try {
    		String sql = "exec INSERT_PUSH_NOTIFICATION ?,?,?,?,?,?,?";
    		Long id = jdbcTemplate.query(sql, new PreparedStatementSetter() {

				@Override
				public void setValues(PreparedStatement arg0)
						throws SQLException {
					arg0.setString(1, notification.getMessageText());
					arg0.setInt(2, notification.getBadgeNumber());
					arg0.setString(3, notification.getSoundName());
					arg0.setTimestamp(4, new java.sql.Timestamp(notification.getPushDate().getTime()));
					arg0.setString(5, notification.getEventType().toString());
					arg0.setString(6, notification.getCustomParameters());
					arg0.setString(7, notification.getTitle());
				}
    			
    		}, new ResultSetExtractor<Long>() {
				@Override
				public Long extractData(ResultSet arg0) throws SQLException,
						DataAccessException {
						if (arg0.next())
							return arg0.getLong("ID");
						else 
							return null;
				}
			});
    		return id;
    	} catch (Throwable e) {
    		e.printStackTrace();
    	}
    	return null;
    }	
	
	public List<PushNotificationClient> getNotificationClients(String osCode,String appCode,String appVersion,String emails[],String devices[]) {
		List<PushNotificationClient> result = jdbcTemplate.query("exec SELECT_NOTIFICATION_CLIENTS ?,?,?,?,?", 
				new Object[] {
				decorateStringListForWhereInClause(osCode),
				decorateStringListForWhereInClause(appCode),
				decorateStringListForWhereInClause(appVersion),
				decorateStringListForWhereInClause(emails),
				decorateStringListForWhereInClause(devices)
			}, new RowMapper<PushNotificationClient>() {

			@Override
			public PushNotificationClient mapRow(ResultSet arg0, int arg1) throws SQLException {
				return new PushNotificationClient(
						Long.valueOf(arg0.getLong("SYNC_PNC_ID")),
						OperationSystemTypes.valueOf(arg0.getString("SYNC_PNC_OS_CODE")),
						arg0.getString("SYNC_PNC_APPLICATION_CODE"),
						arg0.getString("SYNC_PNC_APPLICATION_VERSION"),
						arg0.getString("SYNC_PNC_APPLE_TOKEN")
				);
			}
			
		});
		
		return result;
	}
	
	public void registerNotificationClient(final PushNotificationClient client) throws Exception {
		String sql = "exec INSERT_PUSH_NOTIFICATIONS_CLIENT ?,?,?,?,?,?,?";
		jdbcTemplate.update(sql,new PreparedStatementSetter() {

			@Override
			public void setValues(PreparedStatement arg0)
					throws SQLException {
				arg0.setString(1, client.getOsCode().toString());
				arg0.setString(2, client.getApplicationCode());
				arg0.setString(3, client.getApplicationVersion());
				arg0.setString(4, client.getEmail());
				arg0.setString(5, client.getDeviceCode());
				arg0.setString(6, client.getDeviceName());
				arg0.setString(7, client.getToken());
			}
			
		});
	}

	public void addClientToNotification(final Long notificationId, List<PushNotificationClient> clients) {
		String sql = "exec ADD_CLIENTS_TO_NOTIFICATION ?,?";
		List<Object[]> args = new ArrayList<Object[]>(clients.size()); 
		for (PushNotificationClient client: clients) {
			args.add(new Long[] {notificationId, client.getClientId()});
		}
		jdbcTemplate.batchUpdate(sql, args, new int[] {Types.INTEGER,Types.INTEGER});
	}
	
	public List<PushNotificationClient> getClientsByNotification(Long notificationId, int from, int count) {
		List<PushNotificationClient> result = jdbcTemplate.query("exec GET_CLIENTS_BY_NOTIFICATION ?, ?, ?", 
				new Object[] {notificationId,from,count}, new RowMapper<PushNotificationClient>() {

			@Override
			public PushNotificationClient mapRow(ResultSet arg0, int arg1) throws SQLException {
				return new PushNotificationClient(
						Long.valueOf(arg0.getLong("SYNC_PNC_ID")), 
						OperationSystemTypes.valueOf(arg0.getString("SYNC_PNC_OS_CODE")), 
						arg0.getString("SYNC_PNC_APPLICATION_CODE"), 
						arg0.getString("SYNC_PNC_APPLICATION_VERSION"), 
						arg0.getString("SYNC_PNC_APPLE_TOKEN"), 
						arg0.getString("SYNC_PNC_EMAIL"), 
						arg0.getString("SYNC_PNC_DEVICE_NAME"), 
						arg0.getString("SYNC_PNC_STATUS")
				);
			}
			
		});		
		return result;
	}
	
	/**
	 * проставить статусы всем клиентам
	 * @param infos
	 * @return
	 */
	public void setClientStatusForNotification(List<PushedNotificationInfo> infos) {
		List<Object[]> args = new ArrayList<Object[]>();
		for (PushedNotificationInfo info: infos) {
			if (info.isFailed()) {
				args.add(new Object[] {info.getNotification().getNotificationId(), info.getClient().getClientId(), info.getError()});
			} else {
				args.add(new Object[] {info.getNotification().getNotificationId(), info.getClient().getClientId(), "OK"});
			}
		}
		jdbcTemplate.batchUpdate("exec SET_NOTIFICATION_2_CLIENT_STATUS ?, ?, ?", args);
	}
	
	/**
	 * проставить всем клиентам статус с ошибкой
	 * @param notifications
	 * @param error
	 */
	public void setClientStatusForNotification(List<PushNotification> notifications, String error) {
		List<Object[]> args = new ArrayList<Object[]>();
		for (PushNotification notification: notifications) {
			args.add(new Object[] {error, notification.getNotificationId()});
		}
		jdbcTemplate.batchUpdate("update SYNC_PUSH_NOTIFICATIONS2CLIENT set SYNC_PNC_STATUS = ? WHERE SYNC_PN_ID = ?", args);
	}
	
	/**
	 * проставить всем клиентам статус с ошибкой
	 * @param notifications
	 * @param clients
	 * @param error
	 */
	public void setClientStatusForNotification(List<PushNotification> notifications, List<List<PushNotificationClient>> clients, String error) {
		List<Object[]> args = new ArrayList<Object[]>();
		for (int i = 0; i< notifications.size() ; i ++) {
			PushNotification notification = notifications.get(i);
			List<PushNotificationClient> clientsList = clients.get(i);
			for (PushNotificationClient client: clientsList) {
				args.add(new Object[] {error, notification.getNotificationId(), client.getClientId()});
			}
		}
		jdbcTemplate.batchUpdate("update SYNC_PUSH_NOTIFICATIONS2CLIENT set SYNC_PNC_STATUS = ? WHERE SYNC_PN_ID = ? AND SYNC_PNC_ID = ?", args);
	}
	
	
	public void notificationWasSent(List<PushNotification> notifications) {
		if (notifications.size() == 0)
			return;
		List<Object[]> ids = new ArrayList<Object[]>();
		for (PushNotification notification: notifications) {
			ids.add(new Object[]{notification.getNotificationId()});
		}
		jdbcTemplate.batchUpdate("exec SEND_NOTIFICATION ?", ids);
	}
	
	public int getClientsByNotificationCount(Long notificationId) {
		return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYNC_PUSH_NOTIFICATIONS2CLIENT WHERE SYNC_PN_ID = "+notificationId);		
	}

	public List<String[]> getNotificationsFromArch(int from, int records) {
		final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
		
		String sql = "exec GET_NOTIFICATIONS "+from+", "+records;
		List<String[]> result = jdbcTemplate.query(sql, new RowMapper<String[]>() {

			@Override
			public String[] mapRow(ResultSet arg0, int arg1) throws SQLException {
				String messageText = (arg0.getString("SYNC_PNA_MESSAGE_TEXT") != null?arg0.getString("SYNC_PNA_MESSAGE_TEXT"):"");
				return new String[]{messageText.length() > 140?messageText.substring(0, 140):messageText,sdf.format(new Date(arg0.getTimestamp("SYNC_PNA_NOTIFY_TIME").getTime())),arg0.getString("SYNC_PNA_ID")};
			}
			
		});
		
		return result;		
	}	
	
	public int getNotificationsCount() {
		return jdbcTemplate.queryForInt("SELECT COUNT(*) FROM SYNC_PUSH_NOTIFICATIONS_ARCHIVE");
	}
	
	/**
	 * Добавить пуш уведолмение в очередь
	 * @return Количество клиентов, которым будет отправлено данное уведомление
	 */
	public int addPushNotificationToQueue(String message,String os,String app,String vers,String emailArray[],String devices[]) {
		// получаем список клиентов, которые подходят под критерий выборки
    	List<PushNotificationClient> clients = getNotificationClients(os, app, vers,emailArray, devices);

    	// если ни один клиент не был обнаружен, то уведолмение не создается.
    	if (clients.size() == 0)
    		return 0;
    	
    	// создаем и добавляем PUSH уведомление в БД
    	PushNotification notification = new PushNotification(null,message,0);
		Long notificationId = addPushNotification(notification);
		
		// добавляем привязку PUSH уведомлений к каждому клиенту
		addClientToNotification(notificationId, clients);
		
		return clients.size();
	}
	
	/**
	 * Добавить пуш уведолмение в очередь и обновить Id уведомления
	 * @return Количество клиентов, которым будет отправлено данное уведомление. Id уведомления обновляется. 
	 */
	public int addPushNotificationToQueue(Long sourceId, final PushNotification notification,String os,String app,String vers,String emailArray[],String devices[]) {
		
		return jdbcTemplate.query("EXEC ADD_PUSH_NOTIFICATION_TO_QUEUE ?,?,?,?,?,?,?,?,?,?,?,?,?", new Object[] {
				sourceId,
				notification.getMessageText(),
				notification.getBadgeNumber(),
				notification.getSoundName(),
				new java.sql.Timestamp(notification.getPushDate().getTime()),
				notification.getEventType().toString(),
				notification.getCustomParameters(),
				notification.getTitle(),
				
				decorateStringListForWhereInClause(os),
				decorateStringListForWhereInClause(app),
				decorateStringListForWhereInClause(vers),
				decorateStringListForWhereInClause(emailArray),
				decorateStringListForWhereInClause(devices)				
				
		}, new ResultSetExtractor<Integer>() {

			@Override
			public Integer extractData(ResultSet rs) throws SQLException,
					DataAccessException {
				if (rs.next()) {
					notification.setNotificationId(rs.getLong("id"));
					return rs.getInt("push_count");
				}
				return 0;				
				
			}
			
		});
	}
	
	public int updateToken(String oldToken, String newToken) {
		String sql = "update SYNC_PUSH_NOTIFICATIONS_CLIENTS set SYNC_PNC_APPLE_TOKEN = ?, SYNC_PNC_INVALIDATED_DATE = NULL where SYNC_PNC_APPLE_TOKEN = ?";
		return jdbcTemplate.update(sql, newToken, oldToken);
	}
	
	public void deleteInvalidToken(Long id) {
		String sql = "update SYNC_PUSH_NOTIFICATIONS_CLIENTS set SYNC_PNC_APPLE_TOKEN = NULL, SYNC_PNC_INVALIDATED_DATE = getdate() where SYNC_PNC_ID = ?";
		jdbcTemplate.update(sql, id);
	}
	
	public List<PushNotification> getNotificationsForSend(Integer amount) {
		return jdbcTemplate.query("exec SELECT_PUSH_NOTIFICATIONS ?",new Object[] {amount}, new RowMapper<PushNotification>() {
			@Override
			public PushNotification mapRow(ResultSet arg0, int arg1)
					throws SQLException {
				String msgType = arg0.getString("event_code");
				return new PushNotification(
					arg0.getLong("notification_id"),
					arg0.getString("message_text"),
					arg0.getInt("badge_number"),
					arg0.getString("sound_name"),
					msgType == null || msgType.isEmpty() ? PushEventTypes.MESSAGE : PushEventTypes.valueOf(msgType),
					arg0.getString("custom_parameters"),
					arg0.getLong("source_id"),
					arg0.getString("title")
				);
			}
			
		});
	}
	
	
}
