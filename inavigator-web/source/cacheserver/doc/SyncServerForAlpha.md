1.  Если в базе данных нет еще пользователя syncserver, то необходимо его создать при помощи скрипта
    sql\adm\adm.sql, который находится в дистрибутиве.
2.  Настроить источник данных в WebSphere jdbc/CACHESERVER_DB на пользователя syncserver, если его еще нет.
3.  Установить syncserver, согласно инструкции CacheServerSetup.docx.
4.  Запустить сервер.
5.  Зайти в администраторскую консоль сервера https://host:sslport/syncserver/gui/welcome.public.gui и настроить все 
    свойсва:
    
    'ROOT_FOLDER'                           ,'D:/usr/cache'								                                ,NULL		                ,'Корневая папка для локального кэша')
    'NETWORK_ROOT_FOLDER'                   ,'Для ALPHA здесь путь до ALPHA_FILE_MOVER_DUPLICATE_FOLDER из генератора'	,NULL		                ,'Корневая папка на файлоперекладчике')
    'NETWORK_SHARED_HOSTS_FOR_CHANGESETS'   ,'localhost'									                            ,NULL		                ,'Список хостов для iRubricator через ";". Changeset-ы будут загружены только на указанные сервера')
    'DATAPOWER_URL1'                        ,'ссылка на proxyserver alpha'					                            ,'Настройки DataPower'	    ,'DataPower URL #1. Сервер будет автоматически балансировать запросы между DataPower URL #1 and DataPower URL #2. При ошибке передачи запроса до MSSQL будет предпринята попытка отправить через другой DataPower')
    'DATAPOWER_URL2'                        ,'ссылка на proxyserver alpha'					                            ,'Настройки DataPower'	    ,'DataPower URL #2. Сервер будет автоматически балансировать запросы между DataPower URL #1 and DataPower URL #2. При ошибке передачи запроса до MSSQL будет предпринята попытка отправить через другой DataPower')
    'ALPHA_SQLPROXY_HOST1'                  ,'finik2-new'								                                ,'Настройки DataPower'	    ,'Адрес SQL Proxy Server используемый c DataPower #1')
    'ALPHA_SQLPROXY_HOST2'                  ,'finik2-new'								                                ,'Настройки DataPower'	    ,'Адрес SQL Proxy Server используемый c DataPower #2')
    'ALPHA_SOURCE_SERVICE'                  ,'finik2-new'								                                ,'Настройки ALPHA'		    ,'имя сервиса (TARGET) в SQL Proxy для доступа к ALPHA_SOURCE_DB на finik1/finik2')
    'ALPHA_MONITOR_DB'                      ,'MIS_IPAD_MONITOR'							                                ,'Настройки ALPHA'		    ,'имя базы данных Alpha Monitor-а')
    'ALPHA_MONITOR_SERVICE'                 ,'finik2-new'								                                ,'Настройки ALPHA'		    ,'имя сервиса (TARGET) в SQL Proxy для доступа к базе данных Alpha Monitor-а')
    'ALPHA_GENERATOR_DB'                    ,'MIS_IPAD_GENERATOR'						                                ,'Настройки ALPHA'		    ,'имя базы данных Generator-а')
    'ALPHA_GENERATOR_SERVICE'               ,'finik2-new'								                                ,'Настройки ALPHA'		    ,'имя сервиса (TARGET) в SQL Proxy для доступа к базе данных Generator-а')
    'LDAP_PROVIDER'                         ,'ldap://???;ldap://???'	                                                ,'Настройки LDAP'	        ,'Адреса серверов LDAP через ";"')
    'LDAP_DOMAIN'                           ,'ALPHA'                 					                                ,'Настройки LDAP'		    ,'имя Windows Domain')
    'LDAP_USERNAME'                         ,'IncidentManagement'    					                                ,'Настройки LDAP'		    ,'имя пользователя LDAP')
    'LDAP_PASSWORD'                         ,'c,th,fyr2013'          					                                ,'Настройки LDAP'		    ,'Пароль пользователя LDAP')
    'PUSH_CERTIFICATE_CONFIG_FOLDER'        ,'D:/usr/cache/push'							                            ,'Настройки PUSH сервисов'  ,'Путь до корневой папки с push сертификатами')
    'PUSH_NOTIFICATION_WHEN_NEW_FILE_LOADED ','false'										                            ,'Настройки PUSH сервисов'  ,'Отправлять Push-уведомление пользователям при загрузке файла в sigma')
    'LDAP_USER_GROUP_MANAGER_SETTINGS'	    ,''												                            ,'Настройки LDAP'		    ,'Java настройки в XML формате для ldap соединения (provider, domain, username, password, iNavigatorGroupLdapDN, base_ctx)')
    'PUSH_NOTIFICATIONS_APP_NAME'		    ,'iNavigator2'									                            ,'Настройки PUSH сервисов'  ,'имя приложений (appName) через запятую, пользователям которых предназначаются push уведомления из БД источника')
    'PUSH_NOTIFICATIONS_SOURCE_SERVICE'	    ,'finik2-new'									                            ,'Настройки PUSH сервисов'  ,'имя сервиса (TARGET) в SQL Proxy для доступа к источнику push уведомлений в Alpha')
    'MAINTENANCE_HOSTS'					    ,'http://10.21.137.27:9080'						                            ,NULL		                ,'Внутренние адреса серверов monitor-sigma через ";", которые будут использоваться для обновления статуса режима тех. поддержки')
    'IS_DB_LOGGING_ENABLED'                 ,'true'										                                ,NULL		                ,'Включить/выключить логирование в БД. Скрыто используется сервисом adminDbLogService')
    'SKIP_EMAIL_VERIFICATION'               ,'false'										                            ,'Настройки ALPHA'		    ,'Пропускать сверку email в запросе с email в личном сертификате')
    'SKIP_EMAIL_VERIFICATION_IP_LIST'       ,'список ip wf'						    				                    ,'Настройки ALPHA'		    ,'Список ip адресов, для которых надо пропускать сверку email в запросе с email в личном сертификате')
        
6.  Перезапустить сервер. 