package ru.vez.iso.desktop.main.operdays.http;

import com.google.gson.Gson;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.main.operdays.dto.OperationDaysHttpResponse;
import ru.vez.iso.desktop.shared.UtilsHelper;

/**
 * HttpClient wrapper NOOP implementation
 * */
public class OperationDayHttpClientNoop implements OperationDayHttpClient {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public OperationDaysHttpResponse loadOperationDays(String API, String token, LocalDate from) {

        logger.debug("API: {}, date from: {}, token: {}", API, from.format(DateTimeFormatter.ISO_LOCAL_DATE), token);

        UtilsHelper.makeDelaySec(1);

        String json = UtilsHelper.readJsonFromFile("noop/data/operationDays-missed-enum-values.json");
        OperationDaysHttpResponse response = new Gson().fromJson(json, OperationDaysHttpResponse.class);
        return response;
    }
}
