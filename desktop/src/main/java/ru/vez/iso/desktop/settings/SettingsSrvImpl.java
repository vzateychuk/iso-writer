package ru.vez.iso.desktop.settings;

import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.AppSettings;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;
import ru.vez.iso.desktop.shared.SettingType;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class SettingsSrvImpl implements SettingsSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private Future<Void> future = CompletableFuture.allOf();

    public SettingsSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec) {
        this.appState = appState;
        this.exec = exec;
    }

    @Override
    public void saveAsync(AppSettings sets) {

        // Avoid multiply pressing
        if (!future.isDone()) {
            logger.debug("Async operation in progress, skipping");
            return;
        }

        String filePath = sets.getSettingFile();

        future = CompletableFuture.supplyAsync( () -> {
            logger.debug(String.format("saveAsync to file: '%s'", filePath));
            Properties p = sets.getProperties();
            try(OutputStream outputStream = new FileOutputStream(filePath)){
                p.store(outputStream, "ISO Writer");
            } catch (IOException e) {
                logger.warn(String.format("Unable to save file: '%s'\n%s", filePath, e));
                // p = createDefaultSettings();
            }
            return sets;
        }, exec).thenAccept(
                settings -> appState.put(AppStateType.SETTINGS, AppStateData.builder().value(settings).build())
        ).exceptionally((ex) -> {
            logger.debug("Unable: " + ex.getLocalizedMessage());
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
                    .refreshOpsDaySec( Integer.parseInt(
                            props.getProperty(SettingType.REFRESH_PERIOD.name(), SettingType.REFRESH_PERIOD.getDefaultValue())
                    ) )
                    .filterOpsDays( Integer.parseInt(
                            props.getProperty(SettingType.OPERATION_DAYS.name(), SettingType.OPERATION_DAYS.getDefaultValue())
                    ) )
                    .isoCachePath(
                            props.getProperty(SettingType.ISO_CACHE_PATH.name(), SettingType.ISO_CACHE_PATH.getDefaultValue())
                    )
                    .abddAPI(props.getProperty(SettingType.ABDD_API.name(), SettingType.ABDD_API.getDefaultValue()))
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
