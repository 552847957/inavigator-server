package ru.sberbank.syncserver2.service.pushnotifications.model;

import java.util.Date;

/**
 * Модель данных - уведомление 
 * @author sbt-gordienko-mv
 *
 */
public class PushNotification {
	
	/**
	 * Идентификатор уведомления
	 */
	private Long notificationId;

	/**
	 * Текст сообщения
	 */
	private String messageText;
	
	/**
	 * число которое необходимо отобразить рядом с иконкой приложения при уведолмении
	 */
	private int badgeNumber;
	
	/**
	 *  Название звукового файла 
	 */
	private String soundName;
	
	/**
	 * Дата, когда push должен быть отправлен
	 * По умолчанию - текущей дате
	 */
	private Date pushDate;
	
	/**
	 * Тип события
	 */
	private PushEventTypes eventType;
	
	/**
	 * дополнительные параметры key:value через ';'
	 */
	private String customParameters;
	
	/**
	 * id уведомления в БД источника
	 */
	private Long sourceId;
	
	/**
	 * заголовок
	 */
	private String title;
	
	public PushNotification(Long notificationId,
			String messageText, int badgeNumber, String soundName,PushEventTypes eventType, String customParameters, Long sourceId, String title) {
		super();
		this.notificationId = notificationId;
		this.messageText = messageText;
		this.badgeNumber = badgeNumber;
		this.soundName = soundName;
		this.eventType = eventType;
		this.customParameters = customParameters;
		this.sourceId = sourceId;
		this.title = title;
	}

	public PushNotification(Long notificationId,
			String messageText, int badgeNumber) {
		this(notificationId,messageText,badgeNumber,"default",PushEventTypes.MESSAGE,"", null, null);
	}

	public PushNotification(Long notificationId,
			String messageText, int badgeNumber,String soundName) {
		this(notificationId,messageText,badgeNumber,soundName,PushEventTypes.MESSAGE,"", null, null);
	}
	
	public Long getNotificationId() {
		return notificationId;
	}

	public void setNotificationId(Long notificationId) {
		this.notificationId = notificationId;
	}

	public String getMessageText() {
		return messageText;
	}

	public void setMessageText(String messageText) {
		this.messageText = messageText;
	}

	public int getBadgeNumber() {
		return badgeNumber;
	}

	public void setBadgeNumber(int badgeNumber) {
		this.badgeNumber = badgeNumber;
	}

	public String getSoundName() {
		return soundName;
	}

	public void setSoundName(String soundName) {
		this.soundName = soundName;
	}

	public Date getPushDate() {
		if (pushDate == null)
			pushDate = new Date();
		return pushDate;
	}

	public void setPushDate(Date pushDate) {
		this.pushDate = pushDate;
	}

	public PushEventTypes getEventType() {
		if (eventType == null)
			eventType = PushEventTypes.MESSAGE;
		return eventType;
	}

	public void setEventType(PushEventTypes eventType) {
		this.eventType = eventType;
	}

	public String getCustomParameters() {
		if (customParameters==null)
			customParameters = "";
		return customParameters;
	}

	public void setCustomParameters(String parameters) {
		this.customParameters = parameters;
	}

	public Long getSourceId() {
		return sourceId;
	}

	public void setSourceId(Long sourceId) {
		this.sourceId = sourceId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
