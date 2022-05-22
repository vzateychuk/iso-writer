package ru.vez.iso.desktop.main;

import javafx.collections.ObservableMap;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.*;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MainSrvImpl implements MainSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final ScheduledExecutorService exec;
    private final MessageSrv msgSrv;

    private Future<Void> future;
    private ScheduledFuture<?> scheduledReload;

    public MainSrvImpl(
            ObservableMap<AppStateType, AppStateData> appState,
            ScheduledExecutorService exec,
            MessageSrv msgSrv
    ) {
        this.appState = appState;
        this.exec = exec;
        this.msgSrv = msgSrv;
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
            logger.error(ex);
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
            this.msgSrv.news("Записан диск: " + su.getNumberSu());
            return status;
        }, exec).thenAccept(st -> readOpsDayAsync(20))
                .exceptionally( ex -> {
                    logger.error(ex);
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
            this.msgSrv.news("Создан ISO образ: " + su.getNumberSu());
            return su;
        }, exec)
                .thenAccept(st -> readOpsDayAsync(20))
                .exceptionally((ex) -> {
                    logger.error(ex);
                    return null;
                });
    }

    @Override
    public void checkSumAsync(Path dirZip) {

        CompletableFuture.supplyAsync( () -> {
            UtilsHelper.makeDelaySec(1);
            final MessageDigest gostDigest = DigestUtils.getDigest(MyConst.ALGO_GOST);
            try ( InputStream dirZipFis = Files.newInputStream(dirZip) ) {
                String actualHash = Hex.encodeHexString(DigestUtils.digest(gostDigest, dirZipFis));
                String expectedHash = actualHash; // TODO read from ABDD server response
                logger.debug("Compare Hash\nexpect:\t'{}'\nactual:\t'{}'", expectedHash, actualHash);
                String result = expectedHash.equals(actualHash) ? "УСПЕШНО" : "НЕУСПЕШНО";
                this.msgSrv.news("Проверка ключа: " + result + " (" + dirZip + ")");
            } catch (Exception ex) {
                logger.error("Unable to compare checksums for: {}", dirZip, ex);
                throw new RuntimeException(ex);
            }
            return null;
        }, exec)
                .exceptionally((ex) -> {
                    logger.error(ex);
                    return null;
                });
    }

    @Override
    public void scheduleReadInterval(int refreshMinutes, int filterDays) {

        logger.debug("periodMin: {}, filterDays: {}", refreshMinutes, filterDays);

        if (refreshMinutes > 0) {
            if (scheduledReload != null) {
                scheduledReload.cancel(true);
            }
            this.scheduledReload = exec.scheduleWithFixedDelay(
                    ()-> readOpsDayAsync(filterDays),
                    1,
                    refreshMinutes *60,
                    TimeUnit.SECONDS
            );
        } else {
            logger.warn("incorrect period: {}", refreshMinutes);
        }

    }

    //region PRIVATE

    List<OperatingDayFX> getOpsDaysWithDelay(int period) {

        UtilsHelper.makeDelaySec(1);    // TODO send request for Operation Days
        return IntStream.rangeClosed(0, period)
                .mapToObj(i -> {
                    LocalDate date = LocalDate.of(1900+i, i%12+1, i%12+1);
                    return new OperatingDayFX(String.valueOf(i), date, TypeSu.CD, OpsDayStatus.READY_TO_RECORDING, date, i%2==0);
                })
                .collect(Collectors.toList());
    }

    List<StorageUnitFX> getStorageUnitsWithDelay(int period) {

        UtilsHelper.makeDelaySec(1);    // TODO send request for StorageUnits
        Random rnd = new Random();
        List<StorageUnitStatus> statuses = Collections.unmodifiableList(Arrays.asList(StorageUnitStatus.values()));
        return IntStream.rangeClosed(0, period * 10)
                .mapToObj(i -> {
                    LocalDate date = LocalDate.of(1900+i, i%12+1, i%12+1);
                    String opsDayId = String.valueOf(i%period);
                    return new StorageUnitFX( String.valueOf(i), opsDayId, "numberSu-" + i,
                            date, i, date, statuses.get(rnd.nextInt(statuses.size())), date, "", i%3==0);
                })
                .collect(Collectors.toList());
    }

    //endregion
}
