package ru.vez.iso.desktop.main;

import ru.vez.iso.desktop.burn.RecorderInfo;
import ru.vez.iso.desktop.main.storeunits.StorageUnitFX;

public interface MainSrv {

    /**
     * Загрузить список операционных дней и StorageUnits
     */
    void refreshDataAsync(int period, Runnable postAction);

    /**
     * Получить информацию о рекордере и о диске
     * */
    RecorderInfo getRecorderInfo(int recorderIndex);

    /**
     * Opening burner's tray
     * */
    void openTray(int recorderIndex);

    /**
     * Записать ISO на внешний диск
     *
     *
     * */
    void burnISOAsync(StorageUnitFX storageUnit, String diskTitle, Runnable postAction);

    /**
     * Запрос на создание ISO файла (EX в статусе "Удален")
     * */
    void isoCreateAsync(StorageUnitFX storageUnit);

    /**
     * This method will reschedule "loadOpsDay" with the refreshPeriod
     *
     * @param refreshPeriod - in minutes
     * @param filterDays - количество опер.дней за которые будет выполняться запрос на сервер
     */
    void scheduleReadInterval(int refreshPeriod, int filterDays, Runnable postAction);

    /**
     * Load ISO file async
     *  @param objectId - ISO file
     * */
    void loadISOAsync(StorageUnitFX objectId, Runnable postAction);

    /**
     * Delete the file and invoke reload
     *
     * @param fileName - имя удаляемого файла
     * */
    void deleteFileAsync(String fileName);

}
