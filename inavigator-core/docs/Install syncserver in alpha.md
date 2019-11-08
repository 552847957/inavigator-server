1. Скачать с nexus новый дистрибутив
2. Установить generator, согласно инструкции generator-XX.XXX.XX\doc\GeneratorSetup.docx
3. Зайти в администраторскую консоль генератора http(s)://server:port/generator/gui/welcome.public.gui и поменять
   свойство ALPHA_FILE_MOVER_DUPLICATE_FOLDER на местоположение, куда будут складироваться генерируемые отчеты кроме
   файл-перекладчика.
4. Установить syncserver, согласно инструкции  syncserver-XX.XXX.XX\doc\CacheServerSetup.docx
    1. Так как сервер впервые устанавливается на ALPHA стандартный путь накатки скриптов базы данных
       (утилитой inav-backup) не годится. Для установки DB необходимо создать в BD пользователя (базу данных). Накатить
       туда все скрипты из папки sql последовательно вручную.
    2. На апликейшт сервере (WebSphere) настроить DataSource согласно инструкции, п. "6.3.	Порядок настройки сервера
       приложений"
5. Зайти в администраторскую консоль syncserver https://host:sslport/syncserver/gui/welcome.public.gui и настроить все
   параметры.
    1. NETWORK_ROOT_FOLDER переопределить туда же куда generator будет дублировать файлы с файл перекладчиком
       (ALPHA_FILE_MOVER_DUPLICATE_FOLDER)
    2. Настроить все остальные параметры, например переопределить DATAPOWER_URL1 и DATAPOWER_URL2 на proxyserver alpha,
       и так далее.
    3. Есть альтернативный путь. Прежде чем накатывать syncserver в файле syncserver-XX.XXX.XX\sql\1.install.sql сразу
       поменять значения всех настраиваемых параметров, сохранить скрипт и установить. 
     

 