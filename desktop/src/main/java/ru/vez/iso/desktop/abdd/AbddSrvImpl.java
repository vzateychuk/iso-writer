package ru.vez.iso.desktop.abdd;

import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;
import ru.vez.iso.desktop.shared.SettingType;
import ru.vez.iso.desktop.utils.UtilsHelper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AbddSrvImpl implements AbddSrv {

    private static Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private Future<Void> future;

    private Map<String, LoadStatus> loadStatus;

    public AbddSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec) {
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

    @Override
    public void loadISOAsync(String objectId) {

        // check if load ISO file is not completed yet
        if (this.loadStatus.get(objectId) == LoadStatus.STARTED) {
            logger.debug("load not completed, skipping. objectId: " + objectId);
            return;
        }
        logger.debug("loadISOAsync started: " + objectId);

        Properties props = ((AppStateData<Properties>)appState.get(AppStateType.SETTINGS)).getValue();
        String dir = props.getProperty(SettingType.DOWNLOAD_ISO_PATH.name());

        CompletableFuture.supplyAsync( () -> {
            UtilsHelper.makeDelaySec(3);    // TODO load from service

            StringBuilder sb = new StringBuilder("Hello:\n");
            for (int i = 0; i < 2000_000; i++) {
                sb.append("something");
            }
            Path path = Paths.get(dir, objectId + ".iso");
            try {
                Files.write(path, sb.toString().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                logger.warn("unable to write to: " + path);
                throw new RuntimeException("unable to write to: " + path);
            }
            return LoadStatus.STARTED;
        }, exec).whenComplete( (st, ex) -> {
            LoadStatus loadStatus = ex != null ? LoadStatus.FAILED : LoadStatus.COMPLETED;
            logger.debug("load {} for objectId: {}", loadStatus.name(), objectId);
            this.loadStatus.put(objectId, loadStatus);
            Map<String, LoadStatus> map = new HashMap<>(this.loadStatus);
            appState.put(AppStateType.LOAD_ISO_STATUS, AppStateData.builder().value(map).build());
        } );

    }

    //region PRIVATE

    private List<OperatingDayFX> getOpsDaysWithDelay(int period) {

        logger.debug("getOpsDaysWithDelay");
        UtilsHelper.makeDelaySec(1);    // TODO load from file
        return IntStream.rangeClosed(0, period)
                .mapToObj(i -> {
                    LocalDate date = LocalDate.of(1900+i, i%12+1, i%12+1);
                    return new OperatingDayFX(String.valueOf(i), date, TypeSu.CD, OpsDayStatus.READY_TO_RECORDING, date, i%2==0);
                })
                .collect(Collectors.toList());
    }

    private List<StorageUnitFX> getStorageUnitsWithDelay(int period) {

        logger.debug("getStorageUnitsWithDelay");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Random rnd = new Random();
        List<StorageUnitStatus> statuses = Collections.unmodifiableList(Arrays.asList(StorageUnitStatus.values()));
        return IntStream.rangeClosed(0, period * 10)
                .mapToObj(i -> {
                    LocalDate date = LocalDate.of(1900+i, i%12+1, i%12+1);
                    String opsDayId = String.valueOf(i%period);
                    return new StorageUnitFX(
                            String.valueOf(i), opsDayId, "numberSu-" + i,
                            date, i, date, statuses.get(rnd.nextInt(statuses.size())), date
                    );
                })
                .collect(Collectors.toList());
    }

    //endregion
}
