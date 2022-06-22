package ru.vez.iso.desktop.main.operdays.noop;

import org.apache.http.client.methods.HttpPost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.HttpClientWrap;
import ru.vez.iso.desktop.shared.UtilsHelper;

/**
 * HttpClient wrapper NOOP implementation
 * */
public class HttpClientOperationDaysNoopImpl implements HttpClientWrap {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public String postDataRequest(HttpPost httpPost) {

        logger.debug(httpPost.toString());

        UtilsHelper.makeDelaySec(1);

        return UtilsHelper.readJsonFromFile("noop/data/operationDays.json");
    }
}
