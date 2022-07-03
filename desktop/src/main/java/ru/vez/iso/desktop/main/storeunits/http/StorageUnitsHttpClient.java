package ru.vez.iso.desktop.main.storeunits.http;

import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitDetailResponse;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitListResponse;

import java.time.LocalDate;

/**
 * HttpClientWrapper for StorageUnits to work with backend API
 * */
public interface StorageUnitsHttpClient {

    /**
     * Получить список Единиц хранения
     * */
    StorageUnitListResponse loadISOList(String API, String token, LocalDate from);

    /**
     * Send simple post request to backend
     * Create a storageUnit (ISO file) on backend server
     * */
    void post(String API, String token, String body);

    /**
     * Download storage unit (ISO) to file 'destFile'
     *
     * @param API - backend API (http://host:port/api
     * @param token - authentication token
     * @param destFile - downloaded file destination
     * */
    void downloadAndSaveFile(String API, String token, String destFile);

    /**
     * Get storage-unit's hash code from backend
     * */
    StorageUnitDetailResponse getHashCode(String API, String token);
}
