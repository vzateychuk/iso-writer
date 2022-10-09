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
import ru.vez.iso.desktop.shared.FileISO;
import ru.vez.iso.desktop.shared.MessageSrv;
import ru.vez.iso.desktop.shared.UtilsHelper;
import ru.vez.iso.desktop.state.ApplicationState;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.*;

public class MainSrvImpl implements MainSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ApplicationState state;
    private final ScheduledExecutorService exec;
    private final MessageSrv msgSrv;
    private final OperationDaysSrv operDaysSrv;
    private final StorageUnitsService storageUnitsSrv;
    private final FileCacheSrv fileCacheSrv;
    private final BurnSrv burner;

    private Future<Void> burnOperation = CompletableFuture.allOf();
    private Future<Void> loadDataOperation = CompletableFuture.allOf();
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
    public void refreshDataAsync(int period, Runnable postAction) {

        logger.debug("period: {}", period);

        // Avoid multiply invocation
        if (!loadDataOperation.isDone()) {
            this.msgSrv.news("Загрузка выполняется, подождите...");
            return;
        }

        assert period > 0 : "Incorrect period: " + period;

        this.period = period;
        LocalDate from = LocalDate.now().minusDays(period);
        CompletableFuture<List<OperatingDayFX>> loadOperationDays = CompletableFuture.supplyAsync(
                () -> this.operDaysSrv.loadOperationDays(from),
                exec
        );
        CompletableFuture<List<StorageUnitFX>> loadStorageUnits = CompletableFuture.supplyAsync(
                () -> storageUnitsSrv.loadStorageUnits(from),
                exec
        );
        CompletableFuture<List<FileISO>> readFileCache = CompletableFuture.supplyAsync(
                () -> fileCacheSrv.readFileCache( state.getSettings().getIsoCachePath() ),
                exec
        );

        loadDataOperation = CompletableFuture.allOf(loadOperationDays, loadStorageUnits, readFileCache)
                .thenAccept( (Void) -> {
                    List<OperatingDayFX> opDaysList = loadOperationDays.join();
                    state.setOperatingDays(opDaysList);

                    List<StorageUnitFX> storeUnitsList = loadStorageUnits.join();
                    List<FileISO> isoFilesList = readFileCache.join();
                    state.setFileNames(isoFilesList);

                    // Update isoFile property for storageUnitsList from local file cache
                    storeUnitsList.stream()
                            .sorted( Comparator.comparing(StorageUnitFX::getNumberSu) )
                            .forEach( su -> {
                                String expectedFileName = su.getObjectId() + ".iso";
                                if (isoFilesList.stream().anyMatch(
                                        fileISO -> expectedFileName.equals(fileISO.getFileName())
                                )) {
                                    su.setIsoFileName(expectedFileName);
                                } else {
                                    su.setIsoFileName("");
                                }
                            });
                    state.setStorageUnits(storeUnitsList);

                    logger.debug("loaded operationDays: {}, storageUnits: {}, cache files: {}", opDaysList.size(), storeUnitsList.size(), isoFilesList.size());
                    postAction.run();
                })
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
    public void burnISOAsync(StorageUnitFX su, String diskTitle, Runnable postAction) {

        // Avoid multiply invocation
        if (!burnOperation.isDone()) {
            this.msgSrv.news("Запись диска выполняется, подождите...");
            return;
        }

        this.msgSrv.news("Старт записи на диск: " + su.getNumberSu() + ", метка диска: " + diskTitle);

        burnOperation = CompletableFuture.runAsync(() -> {
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
                    postAction.run();
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
    public void scheduleReadInterval(int refreshMinutes, int filterDays, Runnable postAction) {

        logger.debug("periodMin: {}, filterDays: {}", refreshMinutes, filterDays);

        if (refreshMinutes < 1) {
            logger.warn("incorrect period: {}", refreshMinutes);
            return;
        }

        if (scheduledReload != null) {
            scheduledReload.cancel(true);
        }

        this.scheduledReload = exec.scheduleWithFixedDelay(
                () -> this.refreshDataAsync(filterDays, postAction),
                0,
                refreshMinutes * 60L,
                TimeUnit.SECONDS
        );

    }

    @Override
    public void loadISOAsync(StorageUnitFX su, Runnable postAction) {

        String fileName = su.getObjectId() + ".iso";
        if (state.isLoading(fileName)) {
            logger.debug("File {} already loading, exit", fileName);
            msgSrv.news(String.format("Загрузка ISO образа для EX '%s' начата, подождите.", su.getNumberSu()));
            return;
        }
        msgSrv.news(String.format("Начата загрузка ISO образа для EX '%s'", su.getNumberSu()));

        // will trigger update of StorageUnits table
        CompletableFuture.runAsync(() -> {
            state.addLoading(fileName);
            this.storageUnitsSrv.downloadAndSaveFile(su.getObjectId());
            state.getIsoFiles().add(new FileISO(fileName, LocalDate.now()));

            // replace storageUnit by value with fileName
            msgSrv.news(String.format("ISO образ для EX: '%s' загружен и готов для записи", su.getNumberSu()));
        }, exec)
        .whenComplete(
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
                    postAction.run();
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
                .exceptionally(ex -> {
                    logger.error(ex);
                    msgSrv.news("Не удалось удалить: " + fileName);
                    return null;
                });

    }

}
