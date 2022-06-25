package ru.vez.iso.desktop.main.storeunits.http;

import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitHttpResponse;

import java.time.LocalDate;

public interface StorageUnitsHttpClient {

    /**
     * Получить список Единиц хранения
     * */
    StorageUnitHttpResponse requestISOList(String url, String token, LocalDate from);

    /**
     * Create a storageUnit (ISO file) on backend server
     * */
    void requestCreateISO(String url, String token);

    /**
     * Download storage unit (ISO) to file 'destFile'
     *
     * @param url - backend API (http://host:port/api
     * @param token - authentication token
     * @param destFile - downloaded file destination
     * */
    void downloadISO(String url, String token, String destFile);

}
