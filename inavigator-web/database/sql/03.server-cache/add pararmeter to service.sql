  declare @id int 

  select @id=LIST_TEMPLATE_ID from PROPERTY_LIST_TEMPLATES where LIST_TEMPLATE_CODE = 'ru.sberbank.syncserver2.service.sql.SQLRequestAndCertificateEmailVerifier'

  exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @id, 'skipCertificateEmailChecking'    ,'N','Пропускать запросы без клиентского сертификата'
  exec SP_SYNC_SET_PROPERTY     'online','requestEmailVerifier','skipCertificateEmailChecking'    ,'false'
  
  
  