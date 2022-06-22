package ru.vez.iso.desktop.main.storeunits.fileload;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileDownloaderNoopImpl implements FileDownloader {

    private static final Logger logger = LogManager.getLogger();

    @Override
    public void download(String url, String token, String fileName) {

        logger.debug("URL: {}, destination file: {}", url, fileName);

        Path path = Paths.get(fileName);
        byte[] strToBytes = url.getBytes();

        try {
            Files.write(path, strToBytes);
        } catch (IOException ex) {
            logger.error(ex);
            throw new IllegalStateException(ex);
        }
    }
}
