declare @list_template_id int
declare @PROPERTY_KEY_id VARCHAR(256)
select @PROPERTY_KEY_id=PROPERTY_KEY from SYNC_CONFIG where PROPERTY_KEY='SKIP_EMAIL_VERIFICATION'
if @PROPERTY_KEY_id is null
       INSERT INTO SYNC_CONFIG VALUES('SKIP_EMAIL_VERIFICATION'               ,'false'                                                              ,'Настройки ALPHA'         ,'Пропускать сверку email в запросе с email в личном сертификате')
select @PROPERTY_KEY_id=PROPERTY_KEY from SYNC_CONFIG where PROPERTY_KEY='SKIP_EMAIL_VERIFICATION_IP_LIST'
if @PROPERTY_KEY_id is null
       INSERT INTO SYNC_CONFIG VALUES('SKIP_EMAIL_VERIFICATION_IP_LIST'       ,''                                                                   ,'Настройки ALPHA'         ,'Список ip адресов, для которых надо пропускать сверку email в запросе с email в личном сертификате')
select @list_template_id=list_template_id from PROPERTY_LIST_TEMPLATES where LIST_TEMPLATE_CODE='ru.sberbank.syncserver2.service.sql.SQLRequestAndCertificateEmailVerifier'

declare @template_id int
select @template_id = template_id from PROPERTY_TEMPLATES where list_template_id = @list_template_id and template_code = 'skipEmailVerification'
if @template_id is null
  exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @list_template_id, 'skipEmailVerification'    ,'N','Пропускать сверку email'
exec SP_SYNC_SET_PROPERTY     'online','requestEmailVerifier','skipEmailVerification'    ,'@SKIP_EMAIL_VERIFICATION@'
select @template_id = template_id from PROPERTY_TEMPLATES where list_template_id = @list_template_id and template_code = 'skipEmailVerificationIps'
if @template_id is null
  exec SP_SYNC_ADD_TEMPLATE_PROPERTY2 @list_template_id, 'skipEmailVerificationIps', 'N', 'Список ip адресов, для которых не делается проверка email'
exec SP_SYNC_SET_PROPERTY     'online','requestEmailVerifier','skipEmailVerificationIps'    ,'@SKIP_EMAIL_VERIFICATION_IP_LIST@'
go
