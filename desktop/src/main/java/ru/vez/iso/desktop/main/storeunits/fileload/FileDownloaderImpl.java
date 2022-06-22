package ru.vez.iso.desktop.main.storeunits.fileload;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.client.LaxRedirectStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Downloading file using Apache HttpClient (>= v4.2) with support to HTTP REDIRECT 301 and 302 when using HTTP method GET
 * @See https://gist.github.com/rponte/09ddc1aa7b9918b52029
 * */
public class FileDownloaderImpl implements FileDownloader {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void download(String url, String token, String fileName) {

        logger.debug("URL: {}, destination file: {}", url, fileName);

        HttpGet get = new HttpGet(url); // we're using GET but it could be via POST as well
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
