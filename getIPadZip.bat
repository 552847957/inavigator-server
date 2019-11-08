rem
rem Нихрена не работает, говорит not authoprized
rem
rem mvn -s settings.xml ^
rem org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get ^
rem -DrepositoryId=nexus.deploy.repo ^
rem -Dnexus.deploy.repo.username=CI_CI00324280 ^
rem -Dnexus.deploy.repo.password=Ee654321 ^
rem -Dmaven.wagon.http.ssl.insecure=true ^
rem -DremoteRepositories=nexus.deploy.repo::::https://sbrf-nexus.sigma.sbrf.ru/nexus/content/repositories/Nexus_PROD ^
rem -DgroupId=Nexus_PROD/CI00377171_i-Navigator ^
rem -DartifactId=D-03.008.01-1-2018-AUG-20-REV-4CDA670E ^
rem -Dversion=D-03.008.01-1-2018-AUG-20-REV-4CDA670E ^
rem -Dpackaging=zip ^
rem -Dclassifier=distrib ^
rem -Ddest=ipad/inav_ipad.zip
rem

rem
rem Тоже не могу авторизацию прикрутить. Но по ходу jenkins pipeline работает поl Linux, поэтому powershell идет нахер
rem
rem powershell -ExecutionPolicy ByPass -Command "(New-Object -TypeName System.Net.WebClient).DownloadFile('https://sbrf-nexus.sigma.sbrf.ru/nexus/service/local/repositories/Nexus_PROD/content/Nexus_PROD/CI00377171_i-Navigator/D-03.008.01-1-2018-AUG-20-REV-4CDA670E/D-03.008.01-1-2018-AUG-20-REV-4CDA670E/D-03.008.01-1-2018-AUG-20-REV-4CDA670E-D-03.008.01-1-2018-AUG-20-REV-4CDA670E-distrib.zip', 'ipad/inav_ipad.zip')"
rem

rem !!!!! вот это работает на линукс !!!!!
rem curl -u CI_CI00324280:Ee654321 https://sbrf-nexus.sigma.sbrf.ru/nexus/service/local/repositories/Nexus_PROD/content/Nexus_PROD/CI00377171_i-Navigator/D-03.008.01-1-2018-AUG-20-REV-4CDA670E/D-03.008.01-1-2018-AUG-20-REV-4CDA670E/D-03.008.01-1-2018-AUG-20-REV-4CDA670E-D-03.008.01-1-2018-AUG-20-REV-4CDA670E-distrib.zip --output ./ipad/ipad.zip
rem