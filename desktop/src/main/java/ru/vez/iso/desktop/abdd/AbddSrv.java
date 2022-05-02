package ru.vez.iso.desktop.abdd;

public interface AbddSrv {

    /**
     * Загрузить список операционных дней и StorageUnits
     */
    void readOpsDayAsync(int period);

    /**
     * Загрузить ISO файл в локальный файловый кэш
     */
    void loadISOAsync(String name);

    /**
     * Изменить статус EX (записано на внешний носитель)
     * */
    void changeStatusAsync(StorageUnitFX selected, StorageUnitStatus status);

    /**
     * Запрос на создание ISO файла (EX в статусе "Удален")
     * */
    void isoCreateAsync(StorageUnitFX selected);
}
