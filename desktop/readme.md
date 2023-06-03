# ISO writer Desktop

Приложение может работать в нескольких режимах:
- PROD - полный функционал приложения. Сетевое взаимодействие, данные получаются и отправляются на сервер. Приложение требует от пользователя подтверждений (например при попытке logout).
- DEV - Режим сетевым взаимодействием, но в котором отключено требование подтверждения ряда действий пользователя (запись на диск, logout, сохранение настроек и т.п.)
- NOOP - Режим "Только UI". Нет сетьевого взаимодействия и окон подтверждения. Данные, отображаемые приложением загружаются с локального диска (см папку ..resources/noop/data/). Пользователь не получает дополнительных окон подтверждения операций. 
  Выбор режима определяется ключом '-mode' (либо -m) в параметрах запуска java файла приложения. Например
```shell
java -Xmx128m -jar desktop-iso-writer-jar-with-dependencies.jar -m dev
``` 
Запустит приложение в DEV режиме.

## Links
[JavaFX Documentation Project](https://fxdocs.github.io/docs/html5/)
[MSDN IMAPI Reference](https://learn.microsoft.com/en-gb/windows/win32/imapi/imapi-reference?redirectedfrom=MSDN)
[Secure your Spring Boot Rest API with Keycloak](https://gauthier-cassany.com/posts/spring-boot-keycloak)
[JavaFX Weaver: JavaFX and Spring Boot integration](https://habr.com/ru/post/478402/)
