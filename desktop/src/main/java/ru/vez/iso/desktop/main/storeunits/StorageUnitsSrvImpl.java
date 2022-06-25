package ru.vez.iso.desktop.main.storeunits;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitDto;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitHttpResponse;
import ru.vez.iso.desktop.main.storeunits.http.StorageUnitsHttpClient;
import ru.vez.iso.desktop.shared.DataMapper;
import ru.vez.iso.desktop.shared.UserDetails;
import ru.vez.iso.desktop.state.ApplicationState;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to get StorageUnits from backend
 * */
public class StorageUnitsSrvImpl implements StorageUnitsSrv {

    private static final Logger logger = LogManager.getLogger();
    private static final String API_STORAGE_UNITS = "/abdd/storageunits";

    private final ApplicationState state;
    private final StorageUnitsHttpClient httpClient;
    private final DataMapper<StorageUnitDto, StorageUnitFX> mapper;

    public StorageUnitsSrvImpl(
            ApplicationState state,
            StorageUnitsHttpClient httpClient,
            DataMapper<StorageUnitDto, StorageUnitFX> mapper
    ) {
        this.state = state;
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    /** Получить список Единиц хранения */
    @Override
    public List<StorageUnitFX> loadStorageUnits(LocalDate from) {

        // get Authentication token or raise exception
        final String token = this.getAuthTokenOrException();

        // Getting backend API
        final String API = state.getSettings().getBackendAPI() + API_STORAGE_UNITS + "/page";

        // Create HTTP request
        StorageUnitHttpResponse resp = this.httpClient.requestISOList(API, token, from);
        if (!resp.isOk()) {
            throw new IllegalStateException("Server response: " + resp.isOk());
        }

        return resp.getData()
                .getObjects()
                .stream()
                .map(mapper::map)
                .collect(Collectors.toList());
    }

    /** Получить образ ЕХ по коду (ISO file) */
    @Override
    public void loadFile(String objectId) {

        logger.debug("objectId: {}", objectId);

        // Get Authentication token or raise exception
        final String token = this.getAuthTokenOrException();

        // Getting backend API
        final String API = state.getSettings().getBackendAPI() + API_STORAGE_UNITS + "/" + objectId + "/iso";

        // Build destination file full path
        String dir = state.getSettings().getIsoCachePath();
        String fileName = objectId + ".iso";
        Path destination = Paths.get(dir, fileName);

        // request file and save to destination
        this.httpClient.downloadISO(API, token, destination.toString());
    }

    /**
     * Request backend API to create a storageUnit (ISO file)
     * */
    @Override
    public void requestCreateISO(String objectId) {
        // Get Authentication token or raise exception
        final String token = this.getAuthTokenOrException();
        final String API = state.getSettings().getBackendAPI() + API_STORAGE_UNITS + "/" + objectId + "/iso";
        this.httpClient.requestCreateISO(API, token);
    }

    //region PRIVATE

    String getAuthTokenOrException() {
        UserDetails userData = state.getUserDetails();
        if (userData == UserDetails.NOT_SIGNED_USER) {
            throw new IllegalStateException("Not authenticated");
        }
        return userData.getToken();
    }

    //endregion
}
