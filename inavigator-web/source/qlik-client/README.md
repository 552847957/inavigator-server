# API для выгрузки данных из Qlik Sense

## Создание сертификатов

1. Для доступа к серверу Qlik Sense через Engine API необходимо создать пользовательский сертификат серез 
 [интерфейс сервера](https://sbt-csit-011.ca.sbrf.ru/qmc/certificates) с указанием хоста с которого будет 
 осуществляться доступ к Engine API.  
2. Сертификаты сохраняем в формате PEM
3. Необходимо конвертировать файл пользовательского ключа при помощи утилиты **openssl**  
    `openssl pkcs8 -topk8 -inform PEM -outform PEM -nocrypt -in client_key.pem -out client_key_8.pem`

## Загрузка объектов

1. Для тостыпа к API необходимо создать экземпляр класса `QlikApi`
   Передав ему хост, порт и контекст сервера Qlik Engine API, пути к файлам сертификатов и пользовательского ключа, также указать домен и логин пользователя.
   ``` java
    QlikClient qlikSenseClient = new QlikClient(
        sbt-csit-011.ca.sbrf.ru,
        4747,
        "/app",
        "root.pen",
        "client.pem",
        "client_key_8.pem",
        "",
        "sbt-biryukov-su",
        "ALPHA");
   ``` 
   
2. Вызвать метод `QlikApi.getData` с указанием ID документа, к которому нужно подключиться 
   и списка ID объектов данные для которых мы хотим получить.
   Необходимые ID можно посмотреть в разделе [Dev Hub](https://sbt-csit-011.ca.sbrf.ru/dev-hub/) > [Single Configurator](https://sbt-csit-011.ca.sbrf.ru/dev-hub/single-configurator)
   
   >При выборе конкретного элемента формируется URL вида 
    https://sbt-csit-011.ca.sbrf.ru/single/?appid=7e129b08-cc35-4ad9-9dc9-b57e73bd455a&obj=eparPnX&opt=nointeraction&select=clearall
   откуда можно получить `appid` и `obj`.    
   В данном примере ID докуменат - `7e129b08-cc35-4ad9-9dc9-b57e73bd455a`, ID объекта - `eparPnX`
    
## Сериализация/Десериализация

Для сериализации/десериализации объектов запроса и ответа можно использовать 
`QlikApiUtils.getObjectMapper()`
