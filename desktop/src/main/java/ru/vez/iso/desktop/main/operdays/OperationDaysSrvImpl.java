package ru.vez.iso.desktop.main.operdays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.main.operdays.dto.OperationDayDto;
import ru.vez.iso.desktop.main.operdays.dto.OperationDaysHttpResponse;
import ru.vez.iso.desktop.main.operdays.http.OperationDayHttpClient;
import ru.vez.iso.desktop.shared.DataMapper;
import ru.vez.iso.desktop.state.ApplicationState;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service to load Operation Days
 * */
public class OperationDaysSrvImpl implements OperationDaysSrv {

    private static final Logger logger = LogManager.getLogger();
    private static final String API_OP_DAYS = "/abdd/operating-day/page";

    private final ApplicationState state;
    private final OperationDayHttpClient httpClient;
    private final DataMapper<OperationDayDto, OperatingDayFX> mapper;

    public OperationDaysSrvImpl(
            ApplicationState appState,
            OperationDayHttpClient httpClient,
            DataMapper<OperationDayDto, OperatingDayFX> mapper
    ) {
        this.state = appState;
        this.httpClient = httpClient;
        this.mapper = mapper;
    }

    @Override
    public List<OperatingDayFX> loadOperationDays(LocalDate from) {

        logger.debug("Start load from: {}", from.format(DateTimeFormatter.ISO_LOCAL_DATE));

        // get Authentication token or raise exception
        final String token = this.getAuthTokenOrException(this.state);

        // Getting backend API
        final String API = state.getSettings().getBackendAPI() + API_OP_DAYS;

        // Create HTTP request
        OperationDaysHttpResponse resp = this.httpClient.loadOperationDays(API, token, from);
        if (!resp.isOk()) {
            throw new IllegalStateException("Server response: " + resp.isOk());
        }

        List<OperatingDayFX>  loaded = resp.getData()
                .getObjects()
                .stream()
                .map(mapper::map)
                .sorted((d1,d2)->d2.getOperatingDay().compareTo(d1.getOperatingDay()))
                .collect(Collectors.toList());

        logger.debug("Loaded: {}", loaded.size());
        return loaded;
    }
}
