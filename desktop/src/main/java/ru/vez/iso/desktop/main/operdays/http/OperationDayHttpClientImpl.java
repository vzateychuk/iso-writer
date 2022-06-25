package ru.vez.iso.desktop.main.operdays.http;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.main.operdays.dto.OperationDaysHttpResponse;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * HttpClientWrapper for OperationDays to work with backend API
 * */
public class OperationDayHttpClientImpl implements OperationDayHttpClient {

    private static final Logger logger = LogManager.getLogger();
    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public OperationDaysHttpResponse loadOperationDays(String API, String token, LocalDate from) {

        // Create HTTP request
        HttpPost httpPost = new HttpPost(API);
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Content-type", "application/json");
        httpPost.setHeader("Authorization", token);
        String jsonRequest = String.format(
                "{\"page\":1,\"rowsPerPage\":500,\"criterias\":[{\"fields\":[\"operatingDayDate\"],\"operator\":\"GREATER_OR_EQUALS\",\"value\":\"%s\"}]}",
                from.format(YYYY_MM_DD)
        );
        try {
            StringEntity entity = new StringEntity(jsonRequest);
            httpPost.setEntity(entity);
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex);
            throw new RuntimeException(ex);
        }

        try (
                CloseableHttpClient httpClient = HttpClients.custom()
                        .setRedirectStrategy(new LaxRedirectStrategy())
                        .build();
                CloseableHttpResponse response = httpClient.execute(httpPost)
        ) {
            // Create response handler
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                throw new RuntimeException("Server response: " + response.getStatusLine().getStatusCode());
            }
            final HttpEntity resEntity = response.getEntity();
            Reader reader = new InputStreamReader(resEntity.getContent(), StandardCharsets.UTF_8);
            OperationDaysHttpResponse resp = new Gson().fromJson(reader, OperationDaysHttpResponse.class);
            EntityUtils.consume(resEntity);
            return resp;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }
}