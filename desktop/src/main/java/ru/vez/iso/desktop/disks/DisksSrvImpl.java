package ru.vez.iso.desktop.disks;

import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class DisksSrvImpl implements DisksSrv {

    private static Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private CompletableFuture<Void> future = CompletableFuture.allOf();

    public DisksSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec) {
        this.appState = appState;
        this.exec = exec;
    }

    @Override
    public void readIsoFileNamesAsync(String dir) {
        // Avoid multiply invocation
        if (!future.isDone()) {
            logger.debug("Async operation in progress, skipping");
            return;
        }

        logger.debug("readIsoFileNamesAsync. dir: " + dir);
        future = CompletableFuture.supplyAsync(() -> this.readIsoFileNames(dir), exec)
                .thenAccept(fileNames ->
                        appState.put(AppStateType.ISO_FILES, AppStateData.<List<String>>builder().value(fileNames).build())
                ).exceptionally((ex) -> {
                    logger.debug("Unable: " + ex.getLocalizedMessage());
                    return null;
                });
    }

    @Override
    public List<String> readIsoFileNames(String spath) {

        Path path = Paths.get(spath);
        return readAndFilter(path, 1,
                (p,a) -> p.toString().endsWith(".iso") && a.isRegularFile())
                .stream().map(p -> p.getFileName().toString()).collect(Collectors.toList());
    }

    //region Private

    public List<Path> readAndFilter(Path path, int depth, BiPredicate<Path, BasicFileAttributes> filter) {

        try {
            return Files.find(path, depth, filter).collect(Collectors.toList());
        } catch (IOException e) {
            logger.warn("Unable to read: " + path);
            throw new RuntimeException(e);
        }
    }

    //endregion
}
