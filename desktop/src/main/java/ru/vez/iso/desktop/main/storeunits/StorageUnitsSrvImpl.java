package ru.vez.iso.desktop.main.storeunits;

import com.google.gson.Gson;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitDto;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitsResponse;
import ru.vez.iso.desktop.shared.DataMapper;
import ru.vez.iso.desktop.shared.HttpClientWrap;
import ru.vez.iso.desktop.shared.UserDetails;
import ru.vez.iso.desktop.shared.UtilsHelper;
import ru.vez.iso.desktop.state.ApplicationState;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to get StorageUnits from backend
 * */
public class StorageUnitsSrvImpl implements StorageUnitsSrv {

    private static final Logger logger = LogManager.getLogger();
    private static final String API_STORAGE_UNITS = "/abdd/storageunits/page";

    private final ApplicationState state;
    private final HttpClientWrap httpClient;
    private final DataMapper<StorageUnitDto, StorageUnitFX> mapper;

    public StorageUnitsSrvImpl(
            ApplicationState state,
            HttpClientWrap httpClient,
            DataMapper<StorageUnitDto, StorageUnitFX> mapper
    ) {
        this.state = state;
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @Override
    public List<StorageUnitFX> loadStorageUnits(LocalDate from) {

        UtilsHelper.makeDelaySec(1);    // TODO send request for StorageUnits
        // Create POST request
        String api = state.getSettings().getBackendAPI() + API_STORAGE_UNITS;
        UserDetails userData = state.getUserDetails();
        if (userData == UserDetails.NOT_SIGNED_USER) {
            return Collections.emptyList();
        }
        String token = userData.getToken();
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
