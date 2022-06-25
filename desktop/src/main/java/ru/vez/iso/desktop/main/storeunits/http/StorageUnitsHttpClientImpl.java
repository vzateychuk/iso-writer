package ru.vez.iso.desktop.main.storeunits.http;

import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
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
import ru.vez.iso.desktop.main.storeunits.dto.StorageUnitHttpResponse;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * StorageUnits HttpClient wrapper
 * */
public class StorageUnitsHttpClientImpl implements StorageUnitsHttpClient {

    private static final Logger logger = LogManager.getLogger();
    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public StorageUnitHttpResponse requestISOList(String url, String token, LocalDate from) {

        // Create HTTP request
        HttpPost httpPost = new HttpPost(url);
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
            StorageUnitHttpResponse resp = new Gson().fromJson(reader, StorageUnitHttpResponse.class);
            EntityUtils.consume(resEntity);
            return resp;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void requestCreateISO(String url, String token) {

        logger.debug("URL: {}", url);

        HttpPost post = new HttpPost(url);
        post.setHeader("Authorization", token);

        try (
                CloseableHttpClient httpClient = HttpClients.custom() // CloseableHttpClient httpClient = HttpClients.createDefault();
                        .setRedirectStrategy(new LaxRedirectStrategy()) // adds HTTP REDIRECT support to GET and POST methods
                        .build();
                CloseableHttpResponse response = httpClient.execute(post);
        ) {
            int code = response.getStatusLine().getStatusCode();
            if (code != HttpStatus.SC_OK) {
                throw new IllegalStateException("Server response: " + code);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Downloading file using Apache HttpClient (>= v4.2) with support to HTTP REDIRECT 301 and 302 when using HTTP method GET
     * @See https://gist.github.com/rponte/09ddc1aa7b9918b52029
     */
    @Override
    public void downloadISO(String url, String token, String fileName) {

        logger.debug("URL: {}, fileName: {}", url, fileName);
        HttpGet get = new HttpGet(url);
        get.setHeader("Authorization", token);

        try (
            CloseableHttpClient httpClient = HttpClients.custom() // CloseableHttpClient httpClient = HttpClients.createDefault();
                    .setRedirectStrategy(new LaxRedirectStrategy()) // adds HTTP REDIRECT support to GET and POST methods
                    .build();
            CloseableHttpResponse response = httpClient.execute(get);
        ) {
            // response handler
            int code = response.getStatusLine().getStatusCode();
            if (code != HttpStatus.SC_OK) {
                throw new IllegalStateException("Server response: " + code);
            }
            InputStream source = response.getEntity().getContent();
            FileUtils.copyInputStreamToFile(source, new File(fileName));
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
