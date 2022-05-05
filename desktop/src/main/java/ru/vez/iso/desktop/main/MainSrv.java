package ru.vez.iso.desktop.main;

public interface MainSrv {

    /**
     * Загрузить список операционных дней и StorageUnits
     */
    void readOpsDayAsync(int period);

    /**
     * Загрузить ISO файл в локальный файловый кэш
     */
    void loadISOAsync(String name);

    /**
     * Записать ISO на внешний диск
     * */
    void burnISOAsync(StorageUnitFX selected, StorageUnitStatus status);

    /**
     * Запрос на создание ISO файла (EX в статусе "Удален")
     * */
    void isoCreateAsync(StorageUnitFX selected);

}
