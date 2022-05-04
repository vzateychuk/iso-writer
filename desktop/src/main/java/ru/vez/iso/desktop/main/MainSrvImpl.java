package ru.vez.iso.desktop.main;

import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.*;
import ru.vez.iso.desktop.utils.UtilsHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainSrvImpl implements MainSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private Future<Void> future;

    private Map<String, LoadStatus> loadStatus;

    public MainSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec) {
        this.appState = appState;
        this.exec = exec;
        this.future = CompletableFuture.allOf();
        this.loadStatus = new ConcurrentHashMap<>();
    }

    @Override
    public void readOpsDayAsync(int period) {

        // Avoid multiply invocation
        if (!future.isDone()) {
            logger.debug("Async operation in progress, skipping");
            return;
        }

        logger.debug("getOperatingDaysAsync. period: " + period);
        CompletableFuture<List<OperatingDayFX>> opsDaysFut = CompletableFuture.supplyAsync(() -> getOpsDaysWithDelay(period), exec);
        CompletableFuture<List<StorageUnitFX>> storeUnitsFut = CompletableFuture.supplyAsync(() -> getStorageUnitsWithDelay(period), exec);

        future = opsDaysFut.thenCombine(
                storeUnitsFut,
                (opsDaysList, storeUnitList) -> {
                    opsDaysList.forEach(day -> {
                        List<StorageUnitFX> units = storeUnitList.stream().filter(u -> u.getOperatingDayId().equals(day.getObjectId())).collect(Collectors.toList());
                        day.setStorageUnits(units);
                    });
                    return opsDaysList;
        }).thenAccept(opsDay -> appState.put(
                AppStateType.OPERATION_DAYS, AppStateData.builder().value(opsDay).build()
        )).exceptionally((ex) -> {
            logger.warn("Unable: " + ex.getLocalizedMessage());
            return null;
        } );
    }

    /**
     * Загрузка ISO файла в локальный файловый cache
     * */
    @Override
    public void loadISOAsync(String name) {

        // check if load ISO file is not completed yet
        if (this.loadStatus.get(name) == LoadStatus.STARTED) {
            logger.debug("load not completed, skipping {}", name);
            return;
        }
        logger.debug("started: {}", name);
        this.loadStatus.put(name, LoadStatus.STARTED);

        AppSettings sets = ((AppStateData<AppSettings>)appState.get(AppStateType.SETTINGS)).getValue();
        String dir = sets.getIsoCachePath();

        CompletableFuture.supplyAsync( () -> {
            UtilsHelper.makeDelaySec(3);    // TODO load from service

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
            appState.put(AppStateType.NOTIFICATION, AppStateData.builder().value("Загружен : " + fileName).build());
            return LoadStatus.COMPLETED;
        }, exec).whenComplete( (st, ex) -> {
            LoadStatus loadStatus = ex != null ? LoadStatus.FAILED : LoadStatus.COMPLETED;
            logger.debug("load {} for objectId: {}", loadStatus.name(), name);
            this.loadStatus.put(name, loadStatus);
            Map<String, LoadStatus> map = new HashMap<>(this.loadStatus);
            appState.put(AppStateType.LOAD_ISO_STATUS, AppStateData.builder().value(map).build());
        } );

    }

    @Override
    public void burnISOAsync(StorageUnitFX su, StorageUnitStatus status) {

        CompletableFuture.supplyAsync( () -> {
            logger.debug("id: {}:{}", su.getObjectId(), su.getNumberSu());
            UtilsHelper.makeDelaySec(1);    // TODO send request for change EX status
            appState.put(AppStateType.NOTIFICATION, AppStateData.builder().value("Записан диск: " + su.getNumberSu()).build());
            return status;
        }, exec).thenAccept(st -> readOpsDayAsync(20))
                .exceptionally((ex) -> {
                    logger.debug("Error: " + ex.getLocalizedMessage());
                    return null;
                });
    }

    @Override
    public void isoCreateAsync(StorageUnitFX su) {
        CompletableFuture.supplyAsync( () -> {
            logger.debug("id: {}:{}", su.getObjectId(), su.getNumberSu());
            UtilsHelper.makeDelaySec(1);    // TODO send request for Create ISO
            appState.put(AppStateType.NOTIFICATION, AppStateData.builder().value("На сервере создан ISO образ: " + su.getNumberSu()).build());
            return su;
        }, exec)
                .thenAccept(loaded -> this.loadISOAsync(loaded.getNumberSu()))
                .thenAccept(st -> readOpsDayAsync(20))
                .exceptionally((ex) -> {
                    logger.debug("Error: " + ex.getLocalizedMessage());
                    return null;
                });
    }

    @Override
    public void deleteFileAndReload(String fileName) {

        // Avoid multiply invocation
        if (!future.isDone()) {
            logger.debug("Async operation in progress, skipping");
            return;
        }

        future = CompletableFuture.supplyAsync(() -> this.deleteIsoFile(fileName), exec)
                .thenApply(this::readIsoFileNames)
                .thenAccept(isoFiles ->
                        appState.put(AppStateType.ISO_FILES_NAMES, AppStateData.<List<IsoFileFX>>builder().value(isoFiles).build())
                ).exceptionally((ex) -> {
                    logger.warn(ex.getLocalizedMessage());
                    return null;
                });
    }

    //region PRIVATE

    private List<OperatingDayFX> getOpsDaysWithDelay(int period) {

        logger.debug("getOpsDaysWithDelay");
        UtilsHelper.makeDelaySec(1);    // TODO send request for Operation Days
        return IntStream.rangeClosed(0, period)
                .mapToObj(i -> {
                    LocalDate date = LocalDate.of(1900+i, i%12+1, i%12+1);
                    return new OperatingDayFX(String.valueOf(i), date, TypeSu.CD, OpsDayStatus.READY_TO_RECORDING, date, i%2==0);
                })
                .collect(Collectors.toList());
    }

    private List<StorageUnitFX> getStorageUnitsWithDelay(int period) {

        logger.debug("getStorageUnitsWithDelay");
        UtilsHelper.makeDelaySec(1);    // TODO send request for StorageUnits
        Random rnd = new Random();
        List<StorageUnitStatus> statuses = Collections.unmodifiableList(Arrays.asList(StorageUnitStatus.values()));
        return IntStream.rangeClosed(0, period * 10)
                .mapToObj(i -> {
                    LocalDate date = LocalDate.of(1900+i, i%12+1, i%12+1);
                    String opsDayId = String.valueOf(i%period);
                    return new StorageUnitFX( String.valueOf(i), opsDayId, "numberSu-" + i,
                            date, i, date, statuses.get(rnd.nextInt(statuses.size())), date, "" );
                })
                .collect(Collectors.toList());
    }

    private String deleteIsoFile(String fileName) {

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

    /**
     * iterates over a directory and outputs all of the fi les that end with a *.iso extension
     * */
    private List<IsoFileFX> readIsoFileNames(String dir) {

        Path path = Paths.get(dir);
        List<String> fileNames = this.readAndFilter(path, 1, (p,a) -> p.toString().endsWith(".iso") && a.isRegularFile() )
                .stream().map(p -> p.getFileName().toString()).collect(Collectors.toList());
        return fileNames.stream()
                .sorted(String::compareTo)
                .map(IsoFileFX::new)
                .collect(Collectors.toList());
    }

    private List<Path> readAndFilter(Path path, int depth, BiPredicate<Path, BasicFileAttributes> filter) {

        try {
            return Files.find(path, depth, filter).collect(Collectors.toList());
        } catch (IOException e) {
            logger.warn("Unable to read: " + path);
            throw new RuntimeException(e);
        }
    }


    //endregion
}
