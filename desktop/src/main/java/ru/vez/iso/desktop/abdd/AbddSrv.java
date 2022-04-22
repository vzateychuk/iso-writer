package ru.vez.iso.desktop.abdd;

public interface AbddSrv {

     /**
      * Загрузить список операционных дней и StorageUnits
      * */
     void loadOpsDayAsync(int period);

     /**
      * Загрузить ISO файл в локальный файловый кэш
      * */
     void loadISOAsync(String objectId);
}
