package ru.vez.iso.desktop.abdd;

public interface AbddSrv {

     /**
      * Загрузить список операционных дней и StorageUnits
      * */
     void readOpsDayAsync(int period);

     /**
      * Загрузить ISO файл в локальный файловый кэш
      * */
     void loadISOAsync(String objectId);
}
