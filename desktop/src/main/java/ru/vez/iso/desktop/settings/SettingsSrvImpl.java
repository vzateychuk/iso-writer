package ru.vez.iso.desktop.settings;

import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.*;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class SettingsSrvImpl implements SettingsSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private final MessageSrv msgSrv;
    private Future<Void> future = CompletableFuture.allOf();

    public SettingsSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec, MessageSrv msgSrv) {
        this.appState = appState;
        this.exec = exec;
        this.msgSrv = msgSrv;
    }

    @Override
    public void saveAsync(AppSettings sets) {

        // Avoid multiply pressing
        if (!future.isDone()) {
            this.msgSrv.news("Операция выполняется, подождите");
            return;
        }

        String filePath = sets.getSettingFile();

        future = CompletableFuture.supplyAsync( () -> {
            logger.debug("file: '{}'", filePath);
            Properties p = sets.getProperties();
            try(OutputStream outputStream = new FileOutputStream(filePath)){
                p.store(outputStream, "ISO Writer");
                this.msgSrv.news("Настройки сохранены");
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            return sets;
        }, exec).thenAccept(
                settings -> appState.put(AppStateType.SETTINGS, AppStateData.builder().value(settings).build())
        ).exceptionally((ex) -> {
            this.msgSrv.news("Не удалось сохранить настройки");
            logger.error("Unable to save settings: ", ex);
            return null;
        } );
    }

    /**
     * Load Settings asynchronously.
     * The input stream will be closed automatically
     */
    @Override
    public void loadAsync(String filePath) {

        logger.debug(String.format("loadAsync from file: '%s'", filePath));
        CompletableFuture.supplyAsync( () -> {

            // Load from file
            Properties props = loadPropertiesFrom(filePath);

            // return new ApplicationSettings instance
            return AppSettings.builder()
                    .settingFile(props.getProperty(SettingType.SETTING_FILE.name(), SettingType.SETTING_FILE.getDefaultValue()))
                    .refreshMin( Integer.parseInt(
                            props.getProperty(SettingType.REFRESH_PERIOD.name(), SettingType.REFRESH_PERIOD.getDefaultValue())
                    ) )
                    .filterOpsDays( Integer.parseInt(
                            props.getProperty(SettingType.OPERATION_DAYS.name(), SettingType.OPERATION_DAYS.getDefaultValue())
                    ) )
                    .isoCachePath(
                            props.getProperty(SettingType.ISO_CACHE_PATH.name(), SettingType.ISO_CACHE_PATH.getDefaultValue())
                    )
                    .abddAPI(props.getProperty(SettingType.BACKEND_API.name(), SettingType.BACKEND_API.getDefaultValue()))
                    .build();

        }, exec).thenAccept(
                settings -> appState.put(AppStateType.SETTINGS, AppStateData.builder().value(settings).build())
        );
    }

    //region PRIVATE

    private Properties loadPropertiesFrom(String filePath) {
        Properties props = new Properties();
        try(InputStream inputStream = new FileInputStream(filePath)) {
            props.load(inputStream);
        } catch (IOException e) {
            logger.warn(String.format("Unable to read settings, default applied: %s", e));
            // props = createDefaultSettings();
        }
        return props;
    }

    //endregion

}
