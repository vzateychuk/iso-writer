package ru.vez.iso.desktop.document;

import com.google.gson.Gson;
import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.document.reestr.Reestr;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DocumentSrvImpl implements DocumentSrv {

    private static Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private final DocumentMapper mapper;

    private Future<Void> future = CompletableFuture.allOf();

    public DocumentSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec, DocumentMapper mapper) {
        this.appState = appState;
        this.exec = exec;
        this.mapper = mapper;
    }

    @Override
    public void loadAsync(Path path) {

        // Avoid multiply invocation
        if (!future.isDone()) {
            logger.debug("DocumentSrv.loadAsync: Async operation in progress, skipping");
            return;
        }

        future = CompletableFuture.supplyAsync(() -> {
            logger.debug("DocumentSrvImpl.loadAsync: Read from: " + path.toString());

            // Read REESTR and save application state
            Reestr reestr = readReestrFromZipFile(path);
            appState.put(AppStateType.REESTR, AppStateData.<Reestr>builder().value(reestr).build());

            // Map REESTR to list of DocFX and return
            return reestr.getDocs().stream().map(mapper::mapToDocFX).collect(Collectors.toList());

        }, exec).thenAccept(docs ->
                appState.put(AppStateType.DOCUMENTS, AppStateData.<List<DocumentFX>>builder().value(docs).build())
        ).exceptionally((ex) -> {
            logger.debug("Unable: " + ex.getLocalizedMessage());
            return null;
        } );
    }

    //region PRIVATE

    /**
     * iterate through each ZipEntry and read a REESTR file
     * */
    private Reestr readReestrFromZipFile(Path path) {

        ZipFile zipFile;
        try {
            zipFile = new ZipFile(path.toFile());
        } catch (IOException e) {
            logger.warn("unable to open zip: {}", path );
            throw new RuntimeException(e);
        }

        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        Reestr reestr = null;
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
                logger.debug(entry);
                if ( "REESTR".equalsIgnoreCase(entry.getName()) ) {
                    String json = readZipEntry(zipFile, entry);
                    logger.debug(json);
                    reestr = new Gson().fromJson(json, Reestr.class);
                }
            }
        return reestr;
    }

    private String readZipEntry(ZipFile zipFile, ZipEntry entry) {

        logger.warn("READ " + entry.getName());
        String reestr = "";
        try (InputStream stream = zipFile.getInputStream(entry);
             InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
             Scanner inputStream = new Scanner(reader);
        ) {
            while (inputStream.hasNext()) {
                reestr = inputStream.nextLine(); // Gets a whole line
            }
        } catch (IOException ex) {
            logger.warn("unable to read REESTR file", ex);
        }
        return reestr;
    }

    //endregion

}
