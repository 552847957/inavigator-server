Исполнить из корневой директории: 

```bash
mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent install
```

Чтобы опубликовать результаты в SonarQube:

```bash
mvn sonar:sonar -P sonar-publish
```