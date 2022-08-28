package ru.vez.iso.desktop.main;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.burn.BurnSrv;
import ru.vez.iso.desktop.burn.RecorderInfo;
import ru.vez.iso.desktop.main.filecache.FileCacheSrv;
import ru.vez.iso.desktop.main.operdays.OperatingDayFX;
import ru.vez.iso.desktop.main.operdays.OperationDaysSrv;
import ru.vez.iso.desktop.main.storeunits.StorageUnitFX;
import ru.vez.iso.desktop.main.storeunits.StorageUnitsSrv;
import ru.vez.iso.desktop.main.storeunits.exceptions.Http404Exception;
import ru.vez.iso.desktop.shared.MessageSrv;
import ru.vez.iso.desktop.shared.MyConst;
import ru.vez.iso.desktop.shared.UtilsHelper;
import ru.vez.iso.desktop.state.ApplicationState;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class MainSrvImpl implements MainSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ApplicationState state;
    private final ScheduledExecutorService exec;
    private final MessageSrv msgSrv;
    private final OperationDaysSrv operDaysSrv;
    private final StorageUnitsSrv storageUnitsSrv;
    private final FileCacheSrv fileCacheSrv;
    private final BurnSrv burner;

    private Future<Void> future;
    private ScheduledFuture<?> scheduledReload;
    private int period;

    public MainSrvImpl(
            ApplicationState state,
            ScheduledExecutorService exec,
            MessageSrv msgSrv,
            OperationDaysSrv operDaysSrv,
            StorageUnitsSrv storageUnitsSrv,
            FileCacheSrv fileCacheSrv,
            BurnSrv burner) {
        this.state = state;
        this.exec = exec;
        this.msgSrv = msgSrv;
        this.operDaysSrv = operDaysSrv;
        this.storageUnitsSrv = storageUnitsSrv;
        this.fileCacheSrv = fileCacheSrv;
        this.burner = burner;
    }

    /**
     * Загрузка списков операционных дней и единиц хранения
     */
    @Override
    public void refreshDataAsync(int period) {

        // Avoid multiply invocation
        if (future != null && !future.isDone()) {
            logger.debug("Async operation in progress, skipping");
            return;
        }

        logger.debug("period: " + period);
        if (period < 1) {
            throw new IllegalArgumentException("Incorrect period: " + period);
        }

        this.period = period;
        LocalDate from = LocalDate.now().minusDays(period);
        CompletableFuture<List<OperatingDayFX>> operationDaysFuture = CompletableFuture.supplyAsync(
                () -> this.operDaysSrv.loadOperationDays(from),
                exec
        );
        CompletableFuture<List<StorageUnitFX>> storeUnitsFuture = CompletableFuture.supplyAsync(
                () -> storageUnitsSrv.loadStorageUnits(from),
                exec
        );

        future = operationDaysFuture.thenCombine(
                storeUnitsFuture,
                (opsDaysList, storeUnitList) -> {
                    opsDaysList.forEach(day -> {
                        List<StorageUnitFX> units = storeUnitList.stream()
                                .filter(u -> u.getOperatingDayId().equals(day.getObjectId())).collect(Collectors.toList());
                        day.setStorageUnits(units);
                    });

                    state.setOperatingDays(opsDaysList);
                    return opsDaysList;
                }).thenAccept(list -> logger.debug("loaded: {}", list.size()))
                .exceptionally((ex) -> {
                    logger.error(ex);
                    return null;
                });
    }

    @Override
    public RecorderInfo getRecorderInfo(int recorderIndex) {

        logger.debug("recorderIndex: {}", recorderIndex);
        return burner.recorderInfo(recorderIndex);
    }

    @Override
    public void openTray(int recorderIndex) {
        logger.debug("recorderIndex: {}", recorderIndex);
        burner.openTray(recorderIndex);
    }

    /**
     * Стартует прожиг диска
     */
    @Override
    public void burnISOAsync(StorageUnitFX su, String diskTitle) {

        this.msgSrv.news("Старт записи на диск: " + su.getNumberSu());

        // Avoid multiply invocation
        if (!future.isDone()) {
            this.msgSrv.news("Операция выполняется, подождите...");
            return;
        }

        future = CompletableFuture.runAsync(() -> {
            logger.debug("id: {}:{}", su.getObjectId(), su.getNumberSu());

            // su.getObjectId()
            Path isoPath = Paths.get(state.getSettings().getIsoCachePath(), su.getObjectId() + ".iso").toAbsolutePath();
            Path targetPath = Paths.get(state.getSettings().getIsoCachePath(), "burn", su.getObjectId()).toAbsolutePath();
            // unzip iso to local folder
            try {
                UtilsHelper.isoToFolder(isoPath, targetPath);
            } catch (IOException ioException) {
                String msg = String.format("Unable to open ISO: %s, and copy to: %s", isoPath, targetPath);
                logger.error(msg, ioException);
                throw new RuntimeException(msg, ioException);
            }
            // start burning ISO
            int burnSpeed = state.getSettings().getBurnSpeed();
            int recorderIndex = 0;
            this.state.setBurning(true);
            burner.burn(recorderIndex, burnSpeed, targetPath, diskTitle);
            this.refreshDataAsync(this.period);
        }, exec)
                .whenComplete((v, ex) -> {
                    this.state.setBurning(false);
                    if (ex == null) {
                        this.msgSrv.news("Записана EX: '" + su.getNumberSu() + "'");
                    } else {
                        logger.error(ex);
                        String msg = String.format("При записи EX: '%s', возникли ошибки. Запись не завершена. Запустите запись снова", su.getNumberSu());
                        this.msgSrv.news(msg);
                    }
                    this.storageUnitsSrv.sendBurnComplete(su.getObjectId(), ex);
                });
    }

    /**
     * Запрос на сервер для создания ISO (поскольку тот может быть удален)
     */
    @Override
    public void isoCreateAsync(StorageUnitFX su) {

        CompletableFuture.runAsync(() -> {
            this.storageUnitsSrv.requestCreateISO(su.getObjectId());
            this.msgSrv.news("Начат процесс формирования iso-образа для ЕХ: " + su.getNumberSu());
        }, exec)
                .exceptionally((ex) -> {
                    logger.error(ex);
                    return null;
                });
    }

    @Override
    public void checkSumAsync(String objectId, Path dirZip) {

        this.msgSrv.news("Начинаю проверку ключа для EX:" + objectId + " (" + dirZip + ")");

        CompletableFuture.supplyAsync(() -> {
            UtilsHelper.makeDelaySec(1);
            final MessageDigest gostDigest = DigestUtils.getDigest(MyConst.ALGO_GOST);
            try (InputStream dirZipFis = Files.newInputStream(dirZip)) {
                String actualHash = Hex.encodeHexString(DigestUtils.digest(gostDigest, dirZipFis));
                String expectedHash = this.storageUnitsSrv.getHashCode(objectId);
                logger.debug("Compare Hash\nexpect(on server):\t'{}'\nactual(on disk):\t'{}'", expectedHash, actualHash);
                String result = expectedHash.equals(actualHash) ? "УСПЕШНО" : "НЕ УДАЛАСЬ";
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

        if (refreshMinutes < 1) {
            logger.warn("incorrect period: {}", refreshMinutes);
            return;
        }

        if (scheduledReload != null) {
            scheduledReload.cancel(true);
        }

        this.scheduledReload = exec.scheduleWithFixedDelay(
                () -> this.refreshDataAsync(filterDays),
                0,
                refreshMinutes * 60L,
                TimeUnit.SECONDS
        );

    }

    @Override
    public void loadISOAsync(StorageUnitFX su) {

        String dir = state.getSettings().getIsoCachePath();

        // will trigger update of StorageUnits table
        CompletableFuture.supplyAsync(() -> {
            this.storageUnitsSrv.loadFile(su.getObjectId());
            msgSrv.news(String.format("ISO образ для EX: '%s' успешно скачан и готов для записи", su.getNumberSu()));
            return null;
        }, exec)
                .thenAccept(nm -> state.setFileNames( fileCacheSrv.readFileCache(dir) ) )
                .exceptionally((ex) -> {
                    logger.error(ex);
                    String msg = String.format("Загрузка ISO образа для EX: '%s' не удалась", su.getNumberSu());
                    msg += ex.getCause() instanceof Http404Exception ? "ISO образ не сформирован на сервере." : "Неизвестная ошибка";
                    msgSrv.news(msg);
                    return null;
                });
    }

    @Override
    public void readFileCacheAsync() {

        String dir = state.getSettings().getIsoCachePath();

        logger.debug("dir: {}", dir);

        CompletableFuture.runAsync(() -> state.setFileNames( fileCacheSrv.readFileCache(dir) ), exec)
                .exceptionally((ex) -> {
                    logger.error(ex);
                    return null;
                });
    }

    @Override
    public void deleteFileAsync(String fileName) {

        logger.debug("file: {}", fileName);

        CompletableFuture.supplyAsync(() -> this.fileCacheSrv.deleteFile(fileName), exec)
                .thenApply(fileCacheSrv::readFileCache)
                .thenAccept(isoFiles ->
                        {
                            state.setFileNames(isoFiles);
                            msgSrv.news("Удален " + fileName);
                        }
                ).exceptionally((ex) ->
                        {
                            logger.error(ex);
                            msgSrv.news("Не удалось удалить: " + fileName);
                            return null;
                        });

    }

}
