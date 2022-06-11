package ru.vez.iso.desktop.login;

import org.apache.http.client.methods.HttpPost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.HttpClientWrap;
import ru.vez.iso.desktop.shared.UtilsHelper;

/**
 * HttpClient wrapper NOOP implementation
 * Successful server response expected:
 * {
 *     "ok": true,
 *     "data": "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImp0aSI6Ijk4MzQ4NzA1LWYxMmItNGU2OC1iNDcwLWI2ZmY2NDgyMmYxYSIsImlhdCI6MTY1NDk3MjQ2OSwiZXhwIjoxNjU1MDU4ODY5fQ.x-ZLNhcuX5nSTOKnrcZg1TscmFZq_hNHNJfClmtsbpc",
 *     "errorCode": null,
 *     "errorMessage": null,
 *     "errorStack": null
 * }
 * */
public class HttpClientLoginNoopImpl implements HttpClientWrap {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public String postDataRequest(HttpPost httpPost) {

        logger.debug(httpPost.toString());

        UtilsHelper.makeDelaySec(1);

        return "Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImp0aSI6Ijk4MzQ4NzA1LWYxMmItNGU2OC1iNDcwLWI2ZmY2NDgyMmYxYSIsImlhdCI6MTY1NDk3MjQ2OSwiZXhwIjoxNjU1MDU4ODY5fQ.x-ZLNhcuX5nSTOKnrcZg1TscmFZq_hNHNJfClmtsbpc";
    }
}
