Для деплоя дистрибутива в Nexus PROM необходимо:
1. Собрать все компоненты приложения в один архив - для этого в папке `inavigator-web/_fpd/scripts/` есть скрипт `build_all.cmd` _todo: переписать на что-то удобоваримое_
2. В файл `settings.xml` в папке `~/.m2/` добавить следующую конфигурацию:
   ```xml
   <settings>
       ...
       <servers>
           ...
           <server>
               <id>nexus.prom</id>
               <username>CI_CI00324280</username>
               <password>/*узнать у Сироткина В.Д. или Горбова Л.С.*/</password>
           </server>
       </servers>
   </settings>
   ```
3. Из папки с получившимся zip _(надо сделать)_ дистрибутивом необходимо  вызвать следующую команду (необходимо наличие java и maven в PATH):
   ```bash
   mvn deploy:deploy-file -DgroupId=Nexus_PROD \
     -DartifactId=CI00377171_i-Navigator \
     -Dversion=/*!!! ПОДСТАВИТЬ ПРАВИЛЬНУЮ ВЕРСИЮ !!!*/ \
     -Dpackaging=zip \
     -Dfile=/* !!! ПОДСТАВИТЬ ПРАВИЛЬНОЕ НАЗВАНИЕ ФАЙЛА С ДИСТРИБУТИВОМ !!! */ \
     -DrepositoryId=nexus.prom \
     -Dclassifier=distrib \
     -Durl=https://sbrf-nexus.sigma.sbrf.ru/nexus/content/repositories/Nexus_PROD \
     -Dmaven.wagon.http.ssl.insecure=true
   ```
   > ВАЖНО! Обратить внимание на то, что в нужные места надо подставить номер версии и название файла.
4. Зайти под учеткой указанной выше (CI_CI00324280) на адрес: https://sbrf-nexus.sigma.sbrf.ru/nexus/content/repositories/Nexus_PROD/Nexus_PROD/CI00377171_i-Navigator/ и проверить что новая версия и дистрибутив появились.


 mvn deploy:deploy-file -DgroupId=Nexus_PROD -s settings.xml -DartifactId=CI00377171_i-Navigator -Dversion="03.005.01-08"  -Dpackaging=zip  -Dfile="c:\Users\16715006\Downloads\CI00377171_i-Navigator-03.005.01-07-distrib.zip"  -DrepositoryId=nexus.deploy.repo  -Dclassifier=distrib  -Durl=https://sbrf-nexus.sigma.sbrf.ru/nexus/content/repositories/Nexus_PROD  -Dmaven.wagon.http.ssl.insecure=true  -Dnexus.deploy.repo.username=CI_CI00324280  -Dnexus.deploy.repo.password=Ee654321 -Dproduct_version="03.005.01-08"