package ru.vez.iso.desktop.settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.exceptions.CantSaveSettingsException;
import ru.vez.iso.desktop.shared.AppSettings;
import ru.vez.iso.desktop.shared.MessageSrv;
import ru.vez.iso.desktop.shared.SettingType;
import ru.vez.iso.desktop.shared.UtilsHelper;
import ru.vez.iso.desktop.state.ApplicationState;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SettingsSrvImpl implements SettingsSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ApplicationState state;
    private final Executor exec;
    private final MessageSrv msgSrv;

    public SettingsSrvImpl(ApplicationState appState, Executor exec, MessageSrv msgSrv) {
        this.state = appState;
        this.exec = exec;
        this.msgSrv = msgSrv;
    }

    @Override
    public AppSettings load(String filePath) {

        logger.debug("file: '{}'", filePath);

        // Load from file
        Properties props = UtilsHelper.loadProperties(filePath);

        // return new ApplicationSettings instance
        int burnSpeed = Integer.parseInt(props.getProperty(SettingType.BURN_SPEED.name(), SettingType.BURN_SPEED.getDefaultValue()));
        if (burnSpeed < 1 || burnSpeed > 16) {
            logger.warn(
                    "Incorrect write speed: {}. Value: ({}) will be used",
                    props.getProperty(SettingType.BURN_SPEED.name()),
                    SettingType.BURN_SPEED.getDefaultValue()
            );
            burnSpeed = Integer.parseInt( SettingType.BURN_SPEED.getDefaultValue() );
        }
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
                .evictCacheDays( Integer.parseInt(
                        props.getProperty(SettingType.EVICT_CACHE_DAYS.name(), SettingType.EVICT_CACHE_DAYS.getDefaultValue())
                ) )
                .authAPI(props.getProperty(SettingType.AUTH_API.name(), SettingType.AUTH_API.getDefaultValue()))
                .authPath(props.getProperty(SettingType.AUTH_PATH.name(), SettingType.AUTH_PATH.getDefaultValue()))
                .clientSecret(props.getProperty(SettingType.CLIENT_SECRET.name(), SettingType.CLIENT_SECRET.getDefaultValue()))
                .clientId(props.getProperty(SettingType.CLIENT_ID.name(), SettingType.CLIENT_ID.getDefaultValue()))
                .grantType(props.getProperty(SettingType.GRANT_TYPE.name(), SettingType.GRANT_TYPE.getDefaultValue()))
                .burnSpeed( burnSpeed )
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
            throw new CantSaveSettingsException(ex);
        }
        return sets;
    }

    @Override
    public void saveAsync(AppSettings sets) {

        String filePath = sets.getSettingFile();
        CompletableFuture.supplyAsync( () -> this.save(filePath, sets), exec )
                .thenAccept( state::setSettings )
                .exceptionally(ex -> {
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

        CompletableFuture.supplyAsync(() -> this.load(filePath), exec)
                .thenAccept(state::setSettings)
                .exceptionally(ex -> {
                    this.msgSrv.news("Не удалось загрузить настройки");
                    logger.error("Unable to load settings: ", ex);
                    return null;
                });
    }

}
