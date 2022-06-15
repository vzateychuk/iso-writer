package ru.vez.iso.desktop.main;

import ru.vez.iso.desktop.main.storeunits.StorageUnitFX;
import ru.vez.iso.desktop.main.storeunits.StorageUnitStatus;

import java.nio.file.Path;

public interface MainSrv {

    /**
     * Загрузить список операционных дней и StorageUnits
     */
    void readDataAsync(int period);

    /**
     * Записать ISO на внешний диск
     * */
    void burnISOAsync(StorageUnitFX selected, StorageUnitStatus status);

    /**
     * Запрос на создание ISO файла (EX в статусе "Удален")
     * */
    void isoCreateAsync(StorageUnitFX selected);

    /**
     * Проверка checkSum с данными сервера
     * @throws RuntimeException если нет возможности прочитать данные checksum с диска или сервера
     *
     * @param dirZip - путь к файлу DIR.zip
     * */
    void checkSumAsync(Path dirZip);

    /**
     * This method will reschedule "loadOpsDay" with the refreshPeriod
     *
     * @param refreshPeriod - in minutes
     * @param filterDays
     */
    void scheduleReadInterval(int refreshPeriod, int filterDays);

}
