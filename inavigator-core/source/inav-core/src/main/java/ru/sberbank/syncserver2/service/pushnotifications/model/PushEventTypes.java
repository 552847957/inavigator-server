package ru.sberbank.syncserver2.service.pushnotifications.model;

/**
 * Типы событий пуш уведомлений
 * @author sbt-gordienko-mv
 *
 */
public enum PushEventTypes {
	/**
	 * Отправка пользовательского сообщения
	 */
	MESSAGE,
	
	/**
	 * Обнаружены новые данные
	 */
	DATAUPDATE,
	
	/**
	 * Обнаружена новая версия ПО
	 */
	VERSIONUPDATE
	
}
