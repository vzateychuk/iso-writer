package ru.vez.iso.desktop.main.storeunits;

import com.google.gson.Gson;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitDto;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitsResponse;
import ru.vez.iso.desktop.shared.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service to get StorageUnits from backend
 * */
public class StorageUnitsSrvImpl implements StorageUnitsSrv {

    private static final Logger logger = LogManager.getLogger();
    private static final String API_STORAGE_UNITS = "/abdd/storageunits/page";

    private final Map<AppStateType, AppStateData> appState;
    private final HttpClientWrap httpClient;
    private final DataMapper<StorageUnitDto, StorageUnitFX> mapper;

    public StorageUnitsSrvImpl(
            Map<AppStateType, AppStateData> appState,
            HttpClientWrap httpClient,
            DataMapper<StorageUnitDto, StorageUnitFX> mapper
    ) {
        this.appState = appState;
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @Override
    public List<StorageUnitFX> loadStorageUnits(LocalDate from) {

        UtilsHelper.makeDelaySec(1);    // TODO send request for StorageUnits
        // Create POST request
        String api = ((AppStateData<AppSettings>) appState.get(AppStateType.SETTINGS)).getValue().getBackendAPI() + API_STORAGE_UNITS;
        AppStateData<UserDetails> userData = (AppStateData<UserDetails>) appState.get(AppStateType.USER_DETAILS);
        if (userData == null) {
            return Collections.emptyList();
        }
        String token = userData.getValue().getToken();
        HttpPost httpPost = new HttpPost(api);

        // TODO jsonRequest should be from RequestBuilder
        String jsonRequest = "{\"page\":1,\"rowsPerPage\":10,\"criterias\":[{\"fields\":[\"operatingDay.operatingDayDate\"],\"operator\":\"GREATER_OR_EQUALS\",\"value\":\"2022-03-24\"}]}";

        try {
            StringEntity entity = new StringEntity(jsonRequest);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", token);
            String json = this.httpClient.postDataRequest(httpPost);
            StorageUnitsResponse response = new Gson().fromJson(json, StorageUnitsResponse.class);
            return response.getObjects().stream().map(mapper::map).collect(Collectors.toList());
        } catch (Exception ex) {
            logger.error(ex);
            throw new RuntimeException(ex);
        }
    }

}
