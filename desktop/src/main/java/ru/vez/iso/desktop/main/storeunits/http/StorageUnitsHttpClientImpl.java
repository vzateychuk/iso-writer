package ru.vez.iso.desktop.main.storeunits.http;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.exceptions.ExceptionHelper;
import ru.vez.iso.desktop.exceptions.HttpRequestException;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitDetailResponse;
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitListResponse;
import ru.vez.iso.desktop.main.storeunits.exceptions.Http404Exception;
import ru.vez.iso.desktop.shared.MyConst;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

/**
 * StorageUnits HttpClient wrapper
 * */
public class StorageUnitsHttpClientImpl implements StorageUnitsHttpClient {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public StorageUnitListResponse loadISOList(String api, String token, LocalDate from) {

        // Create HTTP request
        HttpPost httpPost = new HttpPost(api);
        httpPost.setHeader(HttpHeaders.ACCEPT, APPLICATION_JSON.getMimeType());
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.getMimeType());
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        String jsonRequest = String.format(
                "{\"page\":1,\"rowsPerPage\":10000,\"criterias\":[{\"fields\":[\"operatingDay.operatingDayDate\"],\"operator\":\"GREATER_OR_EQUALS\",\"value\":\"%s\"}]}",
                from.format(MyConst.YYYY_MM_DD)
        );

        logger.debug("HttpPost: {}, body: {}", httpPost, jsonRequest);

        try {
            StringEntity entity = new StringEntity(jsonRequest);
            httpPost.setEntity(entity);
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex);
            throw new HttpRequestException(ex);
        }

        try (
                CloseableHttpClient httpClient = HttpClients.custom()
                        .setRedirectStrategy(new LaxRedirectStrategy())
                        .build();
                CloseableHttpResponse response = httpClient.execute(httpPost)
        ) {
            // Create response handler
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                ExceptionHelper.handleHttExceptionStatus(response.getStatusLine().getStatusCode());
            }
            final HttpEntity resEntity = response.getEntity();
            Reader reader = new InputStreamReader(resEntity.getContent(), StandardCharsets.UTF_8);
            StorageUnitListResponse resp = new Gson().fromJson(reader, StorageUnitListResponse.class);
            EntityUtils.consume(resEntity);
            return resp;
        } catch (IOException ex) {
            throw new HttpRequestException(ex);
        }
    }

    @Override
    public void post(String api, String token, String body) {

        HttpPost httpPost = new HttpPost(api);
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.getMimeType());

        logger.debug("HttpPost: {}, body: {}", httpPost, body);

        try {
            StringEntity entity = new StringEntity(body);
            httpPost.setEntity(entity);
        } catch (UnsupportedEncodingException ex) {
            logger.error(ex);
            throw new HttpRequestException(ex);
        }

        try (
                CloseableHttpClient httpClient = HttpClients.custom()
                        .setRedirectStrategy(new LaxRedirectStrategy())
                        .build();
                CloseableHttpResponse response = httpClient.execute(httpPost)
        ) {
            int code = response.getStatusLine().getStatusCode();
            logger.debug("HttpPost response code: {}", code);
            if (code != HttpStatus.SC_OK) {
                ExceptionHelper.handleHttExceptionStatus(response.getStatusLine().getStatusCode());
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Downloading file using Apache HttpClient (>= v4.2) with support to HTTP REDIRECT 301 and 302 when using HTTP method GET
     * https://gist.github.com/rponte/09ddc1aa7b9918b52029
     */
    @Override
    public void downloadAndSaveFile(String api, String token, String fileName) {

        HttpGet httpGet = new HttpGet(api);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.getMimeType());

        logger.debug("HttpGet: {}", httpGet);

        try (
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setRedirectStrategy(new LaxRedirectStrategy())
                    .build();
            CloseableHttpResponse response = httpClient.execute(httpGet)
        ) {
            // response handler
            int code = response.getStatusLine().getStatusCode();
            // Since
            if (code == HttpStatus.SC_NOT_FOUND) {
                throw new Http404Exception();
            } else if (code != HttpStatus.SC_OK) {
                ExceptionHelper.handleHttExceptionStatus(response.getStatusLine().getStatusCode());
            }
            InputStream source = response.getEntity().getContent();
            FileUtils.copyInputStreamToFile(source, new File(fileName));
        } catch (IOException ex) {
            throw new HttpRequestException("Unable to save ISO file",  ex);
        }
    }

    @Override
    public StorageUnitDetailResponse getHashCode(String api, String token) {

        HttpGet httpGet = new HttpGet(api);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);

        logger.debug("HttpGet: {}", httpGet);

        try (
                CloseableHttpClient httpClient = HttpClients.custom()
                        .setRedirectStrategy(new LaxRedirectStrategy())
                        .build();
                CloseableHttpResponse response = httpClient.execute(httpGet)
        ) {
            int code = response.getStatusLine().getStatusCode();
            if (code != HttpStatus.SC_OK) {
                ExceptionHelper.handleHttExceptionStatus(response.getStatusLine().getStatusCode());
            }
            final HttpEntity resEntity = response.getEntity();
            Reader reader = new InputStreamReader(resEntity.getContent(), StandardCharsets.UTF_8);
            StorageUnitDetailResponse resp = new Gson().fromJson(reader, StorageUnitDetailResponse.class);
            EntityUtils.consume(resEntity);
            return resp;
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
