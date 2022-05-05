package ru.vez.iso.desktop.main;

import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;
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

public class MainSrvImpl implements MainSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private Future<Void> future;

    public MainSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec) {
        this.appState = appState;
        this.exec = exec;
        this.future = CompletableFuture.allOf();
    }

    /**
     * Загрузка списков операционных дней и единиц хранения
     * */
    @Override
    public void readOpsDayAsync(int period) {

        // Avoid multiply invocation
        if (!future.isDone()) {
            logger.debug("Async operation in progress, skipping");
            return;
        }

        logger.debug("period: " + period);
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
     * Стартует прожиг диска
     * */
    @Override
    public void burnISOAsync(StorageUnitFX su, StorageUnitStatus status) {

        CompletableFuture.supplyAsync( () -> {
            logger.debug("id: {}:{}", su.getObjectId(), su.getNumberSu());
            UtilsHelper.makeDelaySec(1);    // TODO send request for change EX status
            appState.put(AppStateType.NOTIFICATION, AppStateData.builder().value("Записан диск: " + su.getNumberSu()).build());
            return status;
        }, exec).thenAccept(st -> readOpsDayAsync(20))
                .exceptionally( ex -> {
                    logger.warn("Error: " + ex.getLocalizedMessage());
                    return null;
                });
    }

    /**
     * Запрос на сервер для создания ISO (поскольку тот может быть удален)
     * */
    @Override
    public void isoCreateAsync(StorageUnitFX su) {
        CompletableFuture.supplyAsync( () -> {
            logger.debug("id: {}:{}", su.getObjectId(), su.getNumberSu());
            UtilsHelper.makeDelaySec(1);    // TODO send request for Create ISO
            appState.put(AppStateType.NOTIFICATION, AppStateData.builder().value("Создан ISO образ: " + su.getNumberSu()).build());
            return su;
        }, exec)
                .thenAccept(st -> readOpsDayAsync(20))
                .exceptionally((ex) -> {
                    logger.debug("Error: " + ex.getLocalizedMessage());
                    return null;
                });
    }

    //region PRIVATE

    List<OperatingDayFX> getOpsDaysWithDelay(int period) {

        logger.debug("");
        UtilsHelper.makeDelaySec(1);    // TODO send request for Operation Days
        return IntStream.rangeClosed(0, period)
                .mapToObj(i -> {
                    LocalDate date = LocalDate.of(1900+i, i%12+1, i%12+1);
                    return new OperatingDayFX(String.valueOf(i), date, TypeSu.CD, OpsDayStatus.READY_TO_RECORDING, date, i%2==0);
                })
                .collect(Collectors.toList());
    }

    List<StorageUnitFX> getStorageUnitsWithDelay(int period) {

        logger.debug("");
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

    //endregion
}
