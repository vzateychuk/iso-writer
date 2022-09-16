package ru.vez.iso.desktop.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.burn.BurnSrv;
import ru.vez.iso.desktop.burn.RecorderInfo;
import ru.vez.iso.desktop.exceptions.FileCacheException;
import ru.vez.iso.desktop.main.filecache.FileCacheSrv;
import ru.vez.iso.desktop.main.operdays.OperatingDayFX;
import ru.vez.iso.desktop.main.operdays.OperationDaysSrv;
import ru.vez.iso.desktop.main.storeunits.StorageUnitFX;
import ru.vez.iso.desktop.main.storeunits.StorageUnitsService;
import ru.vez.iso.desktop.main.storeunits.exceptions.Http404Exception;
import ru.vez.iso.desktop.shared.MessageSrv;
import ru.vez.iso.desktop.shared.UtilsHelper;
import ru.vez.iso.desktop.state.ApplicationState;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private final StorageUnitsService storageUnitsSrv;
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
            StorageUnitsService storageUnitsSrv,
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

        logger.debug("period: {}", period);
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

        future = operationDaysFuture
                .thenCombine(
                storeUnitsFuture,
                (opsDaysList, storeUnitList) -> {
                    opsDaysList.forEach(day -> {
                        List<StorageUnitFX> units = storeUnitList.stream()
                                .filter(u -> u.getOperatingDayId().equals(day.getObjectId())).collect(Collectors.toList());
                        day.setStorageUnits(units);
                    });

                    state.setOperatingDays(opsDaysList);
                    logger.debug("loaded: {}", opsDaysList.size());
                    return opsDaysList;
                })
                .thenAccept( list ->
                        state.setFileNames(
                                fileCacheSrv.readFileCache( state.getSettings().getIsoCachePath() )
                        )
                )
                .exceptionally(ex -> {
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

        this.msgSrv.news("Старт записи на диск: " + su.getNumberSu() + ", метка диска: " + diskTitle);

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
            } catch (IOException ioe) {
                String msg = String.format("Unable to open ISO: %s, and copy to: %s", isoPath, targetPath);
                logger.error(msg, ioe);
                throw new FileCacheException(msg, ioe);
            }
            // start burning ISO
            int burnSpeed = state.getSettings().getBurnSpeed();
            int recorderIndex = 0;
            this.state.setBurning(true);
            burner.burn(recorderIndex, burnSpeed, targetPath, diskTitle);
        }, exec)
                .whenComplete((v, ex) -> {
                    if (ex == null) {
                        this.msgSrv.news("Записана EX: '" + su.getNumberSu() + "'");
                    } else {
                        logger.error(ex);
                        String msg = String.format("При записи EX: '%s', возникли ошибки. Запись не завершена. Запустите запись снова", su.getNumberSu());
                        this.msgSrv.news(msg);
                    }
                    this.state.setBurning(false);
                    this.storageUnitsSrv.sendBurnComplete(su.getObjectId(), ex);
                    this.refreshDataAsync(this.period);
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
                .exceptionally(ex -> {
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

        String fileName = su.getObjectId() + ".iso";
        if (state.isLoading(fileName)) {
            logger.debug("File {} already loading, exit", fileName);
            msgSrv.news(String.format("Загрузка ISO образа для EX '%s' начата, подождите.", su.getNumberSu()));
            return;
        }
        msgSrv.news(String.format("Начата загрузка ISO образа для EX '%s'", su.getNumberSu()));

        String cachePath = state.getSettings().getIsoCachePath();

        // will trigger update of StorageUnits table
        CompletableFuture.supplyAsync(() -> {
            state.addLoading(fileName);
            this.storageUnitsSrv.loadFile(su.getObjectId());
            msgSrv.news(String.format("ISO образ для EX: '%s' успешно скачан и готов для записи", su.getNumberSu()));
            return null;
        }, exec)
        .handle(
                (s, ex) -> {
                    if (ex != null) {
                        logger.error(ex);
                        String msg = String.format("Загрузка ISO образа для EX: '%s' не удалась: ", su.getNumberSu());
                        msg += ex.getCause() instanceof Http404Exception
                                        ? "ISO образ не сформирован на сервере."
                                        : "Неизвестная ошибка";
                        msgSrv.news(msg);
                    }
                    // finally remove file from list of loading and refresh fileNames list
                    state.removeLoading(fileName);
                    state.setFileNames(fileCacheSrv.readFileCache(cachePath));
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
                )
                .exceptionally(ex ->
                {
                    logger.error(ex);
                    msgSrv.news("Не удалось удалить: " + fileName);
                    return null;
                });

    }

}
