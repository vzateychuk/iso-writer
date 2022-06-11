package ru.vez.iso.desktop.settings;

import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.*;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SettingsSrvImpl implements SettingsSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private final MessageSrv msgSrv;

    public SettingsSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec, MessageSrv msgSrv) {
        this.appState = appState;
        this.exec = exec;
        this.msgSrv = msgSrv;
    }

    @Override
    public AppSettings load(String filePath) {

        logger.debug("file: '{}'", filePath);

        // Load from file
        Properties props = UtilsHelper.loadProperties(filePath);

        // return new ApplicationSettings instance
        return AppSettings.builder()
                .settingFile( SettingType.SETTING_FILE.getDefaultValue() )
                .refreshMin( Integer.parseInt(
                        props.getProperty(SettingType.REFRESH_PERIOD.name(), SettingType.REFRESH_PERIOD.getDefaultValue())
                ) )
                .filterOpsDays( Integer.parseInt(
                        props.getProperty(SettingType.OPERATION_DAYS.name(), SettingType.OPERATION_DAYS.getDefaultValue())
                ) )
                .isoCachePath(
                        props.getProperty(SettingType.ISO_CACHE_PATH.name(), SettingType.ISO_CACHE_PATH.getDefaultValue())
                )
                .backendAPI(
                        props.getProperty(SettingType.BACKEND_API.name(), SettingType.BACKEND_API.getDefaultValue())
                )
                .build();
    }

    @Override
    public AppSettings save(String filePath, AppSettings sets) {

        logger.debug("file: '{}'", filePath);

        Properties props = sets.getProperties();

        try( OutputStream outputStream = Files.newOutputStream( Paths.get(filePath) ) ){
            props.store(outputStream, "ISO Writer");
            this.msgSrv.news("Настройки сохранены");
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return sets;
    }

    @Override
    public void saveAsync(AppSettings sets) {

        String filePath = sets.getSettingFile();
        CompletableFuture.supplyAsync( () -> this.save(filePath, sets), exec)
                .thenAccept(
                        settings -> appState.put(AppStateType.SETTINGS, AppStateData.builder().value(settings).build())
                )
                .exceptionally( (ex) -> {
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

        CompletableFuture.supplyAsync( () -> this.load(filePath), exec )
                .thenAccept(
                        settings -> appState.put(AppStateType.SETTINGS, AppStateData.builder().value(settings).build())
                );
    }

}
