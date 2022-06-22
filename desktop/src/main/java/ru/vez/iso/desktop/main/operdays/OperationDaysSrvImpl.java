package ru.vez.iso.desktop.main.operdays;

import com.google.gson.Gson;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.main.operdays.dto.OperDayDto;
import ru.vez.iso.desktop.main.operdays.dto.OperationDaysResponse;
import ru.vez.iso.desktop.shared.DataMapper;
import ru.vez.iso.desktop.shared.HttpClientWrap;
import ru.vez.iso.desktop.shared.UserDetails;
import ru.vez.iso.desktop.state.ApplicationState;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to load Operation Days
 * */
public class OperationDaysSrvImpl implements OperationDaysSrv {

    private static final Logger logger = LogManager.getLogger();
    private static final String API_OP_DAYS = "/abdd/operating-day/page";
    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final ApplicationState state;
    private final HttpClientWrap httpClient;
    private final DataMapper<OperDayDto, OperatingDayFX> mapper;

    public OperationDaysSrvImpl(
            ApplicationState appState,
            HttpClientWrap httpClient,
            DataMapper<OperDayDto, OperatingDayFX> mapper
    ) {
        this.state = appState;
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @Override
    public List<OperatingDayFX> loadOperationDays(LocalDate from) {

        // Create POST request
        final String operDaysAPI = state.getSettings().getBackendAPI() + API_OP_DAYS;
        UserDetails userData = state.getUserDetails();
        if (userData == UserDetails.NOT_SIGNED_USER) {
            return Collections.emptyList();
        }
        String token = userData.getToken();
        HttpPost httpPost = new HttpPost(operDaysAPI);
        String jsonRequest = this.buildJsonRequest(from);

        try {
            StringEntity entity = new StringEntity(jsonRequest);
            httpPost.setEntity(entity);
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", token);
            String json = this.httpClient.postDataRequest(httpPost);
            OperationDaysResponse response = new Gson().fromJson(json, OperationDaysResponse.class);

            return response.getObjects().stream()
                    .map(mapper::map)
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            logger.error(ex);
            throw new RuntimeException(ex);
        }
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
