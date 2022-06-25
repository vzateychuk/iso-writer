package ru.vez.iso.desktop.main.storeunits;

import java.time.LocalDate;
import java.util.List;

/**
 * Service to get StorageUnits from backend
 * */
public interface StorageUnitsSrv {

    /**
     * Получить список Единиц хранения
     * */
    List<StorageUnitFX> loadStorageUnits(LocalDate from);

    /**
     * Получить образ ЕХ по коду (ISO file)
     * */
    void loadFile(String objectId);

    /**
     * Request backend API to create a storageUnit (ISO file)
     * */
    void requestCreateISO(String objectId);
}
