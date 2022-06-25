package ru.vez.iso.desktop.main.storeunits.http;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitHttpResponse;
import ru.vez.iso.desktop.shared.UtilsHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class StorageUnitsHttpClientNoopImpl implements StorageUnitsHttpClient {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public StorageUnitHttpResponse requestISOList(String url, String token, LocalDate from) {

        logger.debug("URL: {}, token: {}, date from: {}", url, token, from.format(DateTimeFormatter.ISO_LOCAL_DATE));

        UtilsHelper.makeDelaySec(1);

        String json = UtilsHelper.readJsonFromFile("noop/data/storageUnits.json");
        StorageUnitHttpResponse response = new Gson().fromJson(json, StorageUnitHttpResponse.class);
        return response;

    }

    @Override
    public void requestCreateISO(String url, String token) {
        logger.debug("URL: {}, token: {}", url, token);
    }

    @Override
    public void downloadISO(String url, String token, String fileName) {

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
}
