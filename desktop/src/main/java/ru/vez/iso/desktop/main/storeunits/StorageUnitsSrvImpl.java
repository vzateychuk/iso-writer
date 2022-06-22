package ru.vez.iso.desktop.main.storeunits;

import com.google.gson.Gson;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitDto;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitsResponse;
import ru.vez.iso.desktop.main.storeunits.fileload.FileDownloader;
import ru.vez.iso.desktop.shared.DataMapper;
import ru.vez.iso.desktop.shared.HttpClientWrap;
import ru.vez.iso.desktop.shared.UserDetails;
import ru.vez.iso.desktop.state.ApplicationState;

import java.io.UnsupportedEncodingException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to get StorageUnits from backend
 * */
public class StorageUnitsSrvImpl implements StorageUnitsSrv {

    private static final Logger logger = LogManager.getLogger();
    private static final String API_STORAGE_UNITS = "/abdd/storageunits";
    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ApplicationState state;
    private final HttpClientWrap httpClient;
    private final FileDownloader downloader;
    private final DataMapper<StorageUnitDto, StorageUnitFX> mapper;

    public StorageUnitsSrvImpl(
            ApplicationState state,
            HttpClientWrap httpClient,
            FileDownloader downloader,
            DataMapper<StorageUnitDto, StorageUnitFX> mapper
    ) {
        this.state = state;
        this.httpClient = httpClient;
        this.downloader = downloader;
        this.mapper = mapper;
    }

    @Override
    public List<StorageUnitFX> loadStorageUnits(LocalDate from) {

        // Create POST request
        UserDetails userData = state.getUserDetails();
        if (userData == UserDetails.NOT_SIGNED_USER) {
            return Collections.emptyList();
        }
        final String backendAPI = state.getSettings().getBackendAPI() + API_STORAGE_UNITS + "/page";

        HttpPost httpPost = new HttpPost(backendAPI);
        String jsonRequest = this.buildJsonRequest(from);

        try {
            StringEntity entity = new StringEntity(jsonRequest);
            httpPost.setEntity(entity);
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex);
            throw new RuntimeException(ex);
        }
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("Authorization", userData.getToken());
        String json = this.httpClient.postDataRequest(httpPost);
        StorageUnitsResponse response = new Gson().fromJson(json, StorageUnitsResponse.class);

        return response.getObjects().stream().map(mapper::map).collect(Collectors.toList());
    }

    @Override
    public void loadFile(String objectId) {

        logger.debug("objectId: {}", objectId);

        UserDetails userData = state.getUserDetails();
        if (userData == UserDetails.NOT_SIGNED_USER) {
            return;
        }
        final String API = state.getSettings().getBackendAPI() + API_STORAGE_UNITS;

        String dir = state.getSettings().getIsoCachePath();
        String fileName = objectId + ".iso";
        Path path = Paths.get(dir, fileName);

        downloader.download(API, userData.getToken(), path.toString());
    }

    //region PRIVATE

    String buildJsonRequest(LocalDate from) {

        return String.format(
                "{\"page\":1,\"rowsPerPage\":500,\"criterias\":[{\"fields\":[\"operatingDayDate\"],\"operator\":\"GREATER_OR_EQUALS\",\"value\":\"%s\"}]}",
                from.format(YYYY_MM_DD)
        );
    }

    //endregion
}
