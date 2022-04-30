package ru.vez.iso.desktop.disks;

import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.AppSettings;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;
import ru.vez.iso.desktop.shared.IsoFileFX;

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

    private static final Logger logger = LogManager.getLogger();

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

        logger.debug("dir: " + dir);
        future = CompletableFuture.supplyAsync( () -> this.readIsoFileNames(dir), exec )
                .thenAccept(isoFiles ->
                    appState.put(AppStateType.ISO_FILES_NAMES, AppStateData.<List<IsoFileFX>>builder().value(isoFiles).build())
                ).exceptionally((ex) -> {
                    logger.debug("Error: " + ex.getLocalizedMessage());
                    return null;
                });
    }

    @Override
    public List<IsoFileFX> readIsoFileNames(String dir) {

        Path path = Paths.get(dir);
        List<String> fileNames = this.readAndFilter(path, 1, (p,a) -> p.toString().endsWith(".iso") && a.isRegularFile() )
                        .stream().map(p -> p.getFileName().toString()).collect(Collectors.toList());
        return fileNames.stream()
                .sorted(String::compareTo)
                .map(name -> new IsoFileFX(name, "docNum-"+name))
                .collect(Collectors.toList());
    }

    @Override
    public void deleteFileAndReload(String fileName) {

        // Avoid multiply invocation
        if (!future.isDone()) {
            logger.debug("Async operation in progress, skipping");
            return;
        }

        future = CompletableFuture.supplyAsync(() -> this.deleteFile(fileName), exec)
                .thenApply(this::readIsoFileNames)
                .thenAccept(isoFiles ->
                        appState.put(AppStateType.ISO_FILES_NAMES, AppStateData.<List<IsoFileFX>>builder().value(isoFiles).build())
                ).exceptionally((ex) -> {
                    logger.warn(ex.getLocalizedMessage());
                    return null;
                });
    }

    //region Private

    private String deleteFile(String fileName) {

        AppSettings sets = (AppSettings) appState.get(AppStateType.SETTINGS).getValue();
        Path filePath = Paths.get(sets.getIsoCachePath(), fileName);
        try {
            Files.delete(filePath);
        } catch (IOException ex) {
            logger.warn("unable to delete file: {}", filePath, ex);
            throw new RuntimeException("unable to delete file: " + filePath);
        }
        return sets.getIsoCachePath();
    }

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
