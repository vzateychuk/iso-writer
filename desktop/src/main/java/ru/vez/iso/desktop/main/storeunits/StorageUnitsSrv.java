package ru.vez.iso.desktop.main.storeunits;

import java.time.LocalDate;
import java.util.List;

/**
 * Service to get StorageUnits from backend
 * */
public interface StorageUnitsSrv {

    List<StorageUnitFX> loadStorageUnits(LocalDate from);

    void loadFile(String objectId);
}
