package ru.sberbank.syncserver2.service.file.fragments;

/**
 * Возможные результаты операций при работе с фрагментами файлов
 * @author sbt-gordienko-mv
 *
 */
public enum FileFragmentOperationResultTypes {
	
	/**
	 * Фрагмент увпешно добавлен во временнюу папку
	 */
	FRAGMENT_SUCCESSFULLY_ADDED(false),
	
	/**
	 * Найдены все необходимые фрагменты, общий файл успещно склеен
	 */
	WHOLE_FILE_SUCCESSFULLY_COLLECTED(false),
	
	/**
	 * Входной файл успешно разделен на фграменты
	 */
	FILE_SUCCESFULLY_DIVIDED_INTO_FRAGMENTS(false),

	/**
	 * ОШИБКА: Операция завершилась с ошибкой
	 */
	OPERATION_ERROR(true),
	
	/**
	 * Операция проигнорирована
	 */
	OPERATION_IGNORED(false),
	
	/**
	 * Результат операции неизвестен. Ошибкой не считаем 
	 */
	OPERATION_RESULT_UNKNOWN(false),
	
	/**
	 * Результат операции  - успешен , без подробностей
	 */
	OPERATION_OK(false);
	
	/**
	 * Признак является ли результат ошибкой
	 */
	private final boolean error;
	
	FileFragmentOperationResultTypes(boolean error) {
		this.error = error;
	}

	public boolean isError() {
		return error;
	}
	
}
