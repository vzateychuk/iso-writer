package ru.vez.iso.desktop.main.filecache;

import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

/**
 * Работа с файловым cache
 * */
public class FileCacheSrvImpl implements FileCacheSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private final MessageSrv msgSrv;

    public FileCacheSrvImpl(
            ObservableMap<AppStateType, AppStateData> appState,
            Executor exec,
            MessageSrv msgSrv
    ) {
        this.appState = appState;
        this.exec = exec;
        this.msgSrv = msgSrv;
    }

    @Override
    public void readFileCacheAsync(String dir) {

        logger.debug("dir: {}", dir);
        CompletableFuture.supplyAsync( () -> this.readIsoFileNames(dir), exec )
                .thenAccept(isoFiles ->
                    appState.put(AppStateType.ISO_FILES_NAMES, AppStateData.<List<IsoFileFX>>builder().value(isoFiles).build())
                ).exceptionally((ex) -> {
                    logger.error(ex);
                    return null;
                });
    }

    @Override
    public void deleteFileAndReload(String fileName) {

        logger.debug("file: {}", fileName);
        CompletableFuture.supplyAsync(() -> this.deleteFile(fileName), exec)
                .thenApply(this::readIsoFileNames)
                .thenAccept(isoFiles ->
                        {
                            appState.put(AppStateType.ISO_FILES_NAMES, AppStateData.<List<IsoFileFX>>builder().value(isoFiles).build());
                            msgSrv.news("Удален " + fileName);
                        }
                ).exceptionally((ex) -> {
                    logger.error(ex);
                    return null;
                });
    }

    /**
     * Загрузка ISO файла в локальный файловый cache
     * */
    @Override
    public void loadISOAsync(String name) {

        logger.debug(name);

        AppSettings sets = ((AppStateData<AppSettings>)appState.get(AppStateType.SETTINGS)).getValue();
        String dir = sets.getIsoCachePath();

        CompletableFuture.supplyAsync( () -> {
            UtilsHelper.makeDelaySec(1);    // TODO load from service

            StringBuilder sb = new StringBuilder("Hello:\n");
            for (int i = 0; i < 2000_000; i++) {
                sb.append("something");
            }
            String fileName = name + ".iso";
            Path path = Paths.get(dir, fileName);
            try {
                Files.write(path, sb.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                logger.warn("unable to write to: " + path);
                throw new RuntimeException("unable to write to: " + path);
            }
            msgSrv.news("Загружен : " + fileName);
            return name;
        }, exec)
                .thenAccept( nm -> this.readFileCacheAsync(SettingType.ISO_CACHE_PATH.getDefaultValue()) )
                .exceptionally( ex -> {
                    logger.error(ex);
                    return null;
                });
    }

    //region PRIVATE

    List<IsoFileFX> readIsoFileNames(String dir) {

        Path path = Paths.get(dir);
        List<String> fileNames = this.readAndFilter(path, 1, (p,a) -> p.toString().endsWith(".iso") && a.isRegularFile() )
                .stream().map(p -> p.getFileName().toString()).collect(Collectors.toList());

        return fileNames.stream()
                .sorted(String::compareTo)
                .map(IsoFileFX::new)
                .collect(Collectors.toList());
    }

    String deleteFile(String fileName) {

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
