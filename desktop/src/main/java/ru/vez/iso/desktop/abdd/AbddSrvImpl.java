package ru.vez.iso.desktop.abdd;

import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;
import ru.vez.iso.desktop.utils.UtilsHelper;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AbddSrvImpl implements AbddSrv {

    private static Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private Future<Void> future = CompletableFuture.allOf();

    public AbddSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec) {
        this.appState = appState;
        this.exec = exec;
    }

    @Override
    public void loadOpsDayAsync(int period) {

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

        // TODO load iso files to file-cache
        logger.debug("loadISOAsync");
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
