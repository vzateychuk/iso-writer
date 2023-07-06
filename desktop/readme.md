# ISO writer Desktop

## Требования к программному обеспечению Системы
Для работы приложения, на компьютере пользователя должно быть предустановлено ПО:
- Windows 10x64-разрядная операционная система.
- Java Runtime Environment (JRE) или Java Development Kit (JDK) версий 1.8.xxx (от версии 211 до более поздней 1.8.361)
[Скачать JRE-1.8 можно по ссылке](https://www.oracle.com/java/technologies/javase/javase8u211-later-archive-downloads.html)

## Первый запуск
Перед запуском приложения, необходимо убедится:
- У локальной учетной записи пользователя есть права записи/удаления к локальному диску компьютера.
- Определена переменная окружения JAVA_HOME в которой сохраняется путь к директории где установлена Java версии 1.8.
- В переменную окружения PATH входит путь к запускаемому файлу java.exe.
После того как эти переменные окружения определены в первый раз, необходимо перезапустить компьютер.

## Режимы запуска приложения
Приложение может работать в нескольких режимах:
- PROD - полный функционал приложения. Сетевое взаимодействие, данные получаются и отправляются на сервер. Приложение требует от пользователя подтверждений (например при попытке logout).
- DEV - Режим сетевым взаимодействием, но в котором отключено требование подтверждения ряда действий пользователя (запись на диск, logout, сохранение настроек и т.п.)
- NOOP - Режим "Только UI". Нет сетьевого взаимодействия и окон подтверждения. Данные, отображаемые приложением загружаются с локального диска (см папку ..resources/noop/data/). Пользователь не получает дополнительных окон подтверждения операций. 
  Выбор режима определяется ключом '-mode' (либо -m) в параметрах запуска java файла приложения. Например
```shell
java -Xmx128m -jar desktop-iso-writer-jar-with-dependencies.jar -m dev
``` 
Запустит приложение в DEV режиме.

## Сборка приложения
Для успешной сборки приложения, необходимо чтобы на машине разработчика были установлены: 
- Java JDK ver:1.8.x [Загрузить эту версию JDK можно например по ссылке](https://www.oracle.com/cis/java/technologies/javase/javase8-archive-downloads.html)  
- Apache Maven ver:3.8.x [Загрузить maven можно по ссылке](https://maven.apache.org/download.cgi) 
После установки maven нужно проверить что будет успешно выполняться команда:
```shell
mvn -version
```
Сборка приложения осуществляется стандартными средствами maven, с помощью команды.
```shell
mvn clean package
```
При этом, maven будет автоматически загружать все необходимые dependencies на компьютор где проходит сборка.
В случае успешной сборки, в папке 'desktop' автоматически создатся подпапка target, содержащая подпапки этапов сборки, а также сам запускаемый архив desktop-iso-writer-jar-with-dependencies.jar, который можно запустить в одном из режимов (см Режимы запуска приложения)
В случае ошибки сборки, maven сообщит 
Подробнее о maven: [как установить maven и использовать другие 'опции' maven (maven phases) можно ознакомится по ссылке](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html) 

## Links
[JavaFX Documentation Project](https://fxdocs.github.io/docs/html5/)
[MSDN IMAPI Reference](https://learn.microsoft.com/en-gb/windows/win32/imapi/imapi-reference?redirectedfrom=MSDN)
[Secure your Spring Boot Rest API with Keycloak](https://gauthier-cassany.com/posts/spring-boot-keycloak)
[JavaFX Weaver: JavaFX and Spring Boot integration](https://habr.com/ru/post/478402/)
