package ru.vez.iso.desktop.main.storeunits;

import ru.vez.iso.desktop.shared.UserDetails;
import ru.vez.iso.desktop.state.ApplicationState;

import java.time.LocalDate;
import java.util.List;

/**
 * Service to get StorageUnits from backend
 * */
public interface StorageUnitsService {

    default String getAuthTokenOrException(ApplicationState state) {
        UserDetails userData = state.getUserDetails();
        if (userData == UserDetails.NOT_SIGNED_USER) {
            throw new IllegalStateException("Not authenticated");
        }
        return userData.getToken();
    }

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

    /**
     * Get storage-unit's hash code from backend
     * */
    String getHashValue(String storageUnitId);

    /**
     * Send HttpPost about complete status
     * */
    void sendBurnComplete(String objectId, Throwable ex);
}
