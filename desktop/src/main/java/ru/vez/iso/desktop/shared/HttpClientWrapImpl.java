package ru.vez.iso.desktop.shared;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * HttpClient wrapper real implementation
 * */
public class HttpClientWrapImpl implements HttpClientWrap {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public String postDataRequest(HttpPost httpPost) {

        logger.debug(httpPost.toString());

        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {
            // Create response handler
            int code = response.getStatusLine().getStatusCode();
            final HttpEntity resEntity = response.getEntity();
            if (code != HttpStatus.SC_OK || resEntity == null) {
                throw new RuntimeException("Server response: " + code);
            }
            Reader reader = new InputStreamReader(resEntity.getContent(), StandardCharsets.UTF_8);
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            EntityUtils.consume(resEntity);
            if (!jsonObject.get("ok").getAsBoolean()) {
                throw new RuntimeException("Server resp OK: " + jsonObject.get("ok").getAsBoolean());
            }
            return jsonObject.get("data").getAsString();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

}
