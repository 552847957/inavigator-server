/**
 *
 */
package ru.sberbank.syncserver2.service.log;

/**
 * @author Yuliya Solomina
 *
 */
public enum LogEventType {
    /**
     * События связанные с публичными сервисами
     */
     FILE_LIST_START       , //– получение списка файлов
     FILE_LIST_FINISH      , //– получение списка файлов
     FILE_DOWNLOAD_START   , //– начало загрузки файлов
     FILE_DOWNLOAD_FINISH  , //– конец загрузки файлов
     SQL_START             , // – начало выполнения запроса SQL
     SQL_FINISH            , // – конец выполнения запроса SQL
     CONFIG_GET            , //- запрос конфигурации (ConfigService)

    /**
      * События в домене Alpha и Sigma, связанные с генератором, видимые для оператора
      */
     GEN_QUEUED              , //после добавления в очередь
     GEN_DB_CONNECTION_START , //перед соединением в БД
     GEN_DB_CONNECTION_FINISH, //после соединения с БД
     GEN_GENERATION_START    , //начало генерации
     GEN_GENERATION_FINISH   , //конец генерации
     GEN_GENERATION_PROGRESS , //промежуточное событие генерации для пользователя
     GEN_TRANSFER_START      , //начало передачи в Sigma (начало сжатия файла)
     GEN_TRANSFER_FINISH     , //получение файла в Sigma (успешное разархивирование)
     GEN_CACHING_START       , //начало загрузки файла
     GEN_CACHING_FINISH      ,  //завершение загрузки файла
     GEN_CANCELLED           , //- отмена генерации

    /**
     * События в домене Alpha и Sigma, связанные с генератором, невидимые для оператора
     */
     GEN_DEBUG               , //отладочное сообщение при генерации

    /**
     * Общие события, не связанные с генератором и передачей файлов и публичными сервисами
     */
     SERV_START         , //– запуск сервиса
     SERV_STOP          , //– остановка сервиса
	 ERROR                 , // –  ошибка
     DEBUG                 , // – отладочное сообщение
     OTHER;                // - другая запись
}
