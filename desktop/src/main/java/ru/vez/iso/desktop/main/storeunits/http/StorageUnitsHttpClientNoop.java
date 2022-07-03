package ru.vez.iso.desktop.main.storeunits.http;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitDetailResponse;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitListResponse;
import ru.vez.iso.desktop.shared.UtilsHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * StorageUnitsHttpClient NOOP implementation
 * */
public class StorageUnitsHttpClientNoop implements StorageUnitsHttpClient {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public StorageUnitListResponse loadISOList(String url, String token, LocalDate from) {

        logger.debug("API: {}, date from: {}, token: {}", url, from.format(DateTimeFormatter.ISO_LOCAL_DATE), token);

        UtilsHelper.makeDelaySec(1);

        String json = UtilsHelper.readJsonFromFile("noop/data/storageUnits.json");
        return new Gson().fromJson(json, StorageUnitListResponse.class);
    }

    @Override
    public void post(String url, String token, String body) {
        logger.debug("URL: {}, token: {}, body: {}", url, token, body);
    }

    @Override
    public void downloadAndSaveFile(String url, String token, String fileName) {

        logger.debug("URL: {}, token: {}, destination: {}", url, token, fileName);

        Path path = Paths.get(fileName);
        byte[] strToBytes = url.getBytes();

        try {
            Files.write(path, strToBytes);
        } catch (IOException ex) {
            logger.error(ex);
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public StorageUnitDetailResponse getHashCode(String API, String token) {

        logger.debug("URL: {}, token: {}", API, token);

        UtilsHelper.makeDelaySec(1);

        String json = UtilsHelper.readJsonFromFile("noop/data/storageUnit.json");
        StorageUnitDetailResponse response = new Gson().fromJson(json, StorageUnitDetailResponse.class);
        return response;
    }

}
