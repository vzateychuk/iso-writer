package ru.vez.iso.desktop.main;

import ru.vez.iso.desktop.main.storeunits.StorageUnitFX;

import java.nio.file.Path;

public interface MainSrv {

    /**
     * Загрузить список операционных дней и StorageUnits
     */
    void refreshDataAsync(int period);

    /**
     * Записать ISO на внешний диск
     * */
    void burnISOAsync(StorageUnitFX selected);

    /**
     * Запрос на создание ISO файла (EX в статусе "Удален")
     * */
    void isoCreateAsync(StorageUnitFX selected);

    /**
     * Проверка checkSum с данными сервера
     * @throws RuntimeException если нет возможности прочитать данные checksum с диска или сервера
     *@param objectId
     * @param dirZip - путь к файлу DIR.zip  */
    void checkSumAsync(String objectId, Path dirZip);

    /**
     * This method will reschedule "loadOpsDay" with the refreshPeriod
     *
     * @param refreshPeriod - in minutes
     * @param filterDays
     */
    void scheduleReadInterval(int refreshPeriod, int filterDays);

    /**
     * Load ISO file async
     *
     * @param objectId - ISO file
     * */
    void loadISOAsync(String objectId);

    /**
     * Загрузить ISO файл в локальный файловый кэш. Async
     * */
    void readFileCacheAsync();

    /**
     * Delete the file and invoke reload
     * */
    void deleteFileAsync(String fileName);
}
