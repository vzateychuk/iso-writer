package ru.vez.iso.desktop.docs;

import com.google.gson.Gson;
import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.docs.reestr.Reestr;
import ru.vez.iso.desktop.shared.AppSettings;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;
import ru.vez.iso.desktop.shared.MyContants;
import ru.vez.iso.desktop.utils.UtilsHelper;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class DocumentSrvImpl implements DocumentSrv {

    private static final Logger logger = LogManager.getLogger();

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
    public void loadAsync(Path zipPath) {

        // Avoid multiply invocation
        if (!future.isDone()) {
            logger.debug("Async operation in progress, skipping");
            return;
        }

        logger.debug(zipPath);

        future = CompletableFuture.supplyAsync(() -> {

            // Clear unzipped files: delete path where unzip files will be stored
            String cachePath = ((AppStateData<AppSettings>)appState.get(AppStateType.SETTINGS)).getValue().getIsoCachePath();
            Path unzippedPath = Paths.get(cachePath, MyContants.UNZIP_FOLDER);
            UtilsHelper.clearFolder(unzippedPath);
            UtilsHelper.unzipToFolder(unzippedPath, zipPath);

            // Read REESTR and save application state
            Path reestrPath = Paths.get(unzippedPath.toString(), MyContants.REESTR_FILE);
            Reestr reestr = readReestrFrom(reestrPath);
            appState.put(AppStateType.REESTR, AppStateData.<Reestr>builder().value(reestr).build());

            // REESTR Map and return list of DocumentFX
            return reestr.getDocs().stream().map(mapper::mapToDocFX).collect(Collectors.toList());

        }, exec).thenAccept(docs ->
                appState.put(AppStateType.DOCUMENTS, AppStateData.<List<DocumentFX>>builder().value(docs).build())
        ).exceptionally((ex) -> {
            String errMsg = String.format("unable to create Reestr object: '%s'",zipPath);
            logger.error(errMsg,ex);
            appState.put(AppStateType.NOTIFICATION, AppStateData.<String>builder().value(errMsg).build());
            return null;
        } );
    }

    //region PRIVATE

    /**
     * Read and deserialize a REESTR file to POJO
     * */
    private Reestr readReestrFrom(Path path) {

        try {
            BufferedReader reader = Files.newBufferedReader(path);
            String fromFile = reader.readLine();
            return new Gson().fromJson(fromFile, Reestr.class);
        } catch (Exception ex) {
            throw new RuntimeException("unable to load: " + path, ex);
        }
    }

    //endregion

}
