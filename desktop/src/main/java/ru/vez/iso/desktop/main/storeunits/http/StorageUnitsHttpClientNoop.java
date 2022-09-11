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
        final String formatted = from.format(DateTimeFormatter.ISO_LOCAL_DATE);
        logger.debug("API: {}, date from: {}, token: {}", url, formatted, token);

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
    public StorageUnitDetailResponse getHashCode(String api, String token) {

        logger.debug("URL: {}, token: {}", api, token);

        UtilsHelper.makeDelaySec(1);

        String json = UtilsHelper.readJsonFromFile("noop/data/storageUnit.json");
        return new Gson().fromJson(json, StorageUnitDetailResponse.class);
    }

}
