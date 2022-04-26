package ru.vez.iso.desktop.settings;

import javafx.collections.ObservableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class SettingsSrvImpl implements SettingsSrv {

    private static Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private Future<Void> future = CompletableFuture.allOf();

    public SettingsSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec) {
        this.appState = appState;
        this.exec = exec;
    }

    @Override
    public void saveAsync(Properties props, String filePath) {

        // Avoid multiply pressing
        if (!future.isDone()) {
            logger.debug("Async operation in progress, skipping");
            return;
        }

        future = CompletableFuture.supplyAsync( () -> {
            logger.debug(String.format("saveAsync to file: '%s'", filePath));
            Properties p = props;
            try(OutputStream outputStream = new FileOutputStream(filePath)){
                p.store(outputStream, "ISO Writer");
            } catch (IOException e) {
                logger.warn(String.format("Unable to save file: '%s'\n%s", filePath, e));
                p = getDefaultPropsConfig();
            }
            return p;
        }, exec).thenAccept(
                p -> appState.put(AppStateType.SETTINGS, AppStateData.builder().value(p).build())
        ).exceptionally((ex) -> {
            logger.debug("Unable: " + ex.getLocalizedMessage());
            return null;
        } );
    }

    /**
     * Load Settings asynchronously by using java 8 try-with-resources structure
     * so the input stream will be closed automatically
     */
    @Override
    public void loadAsync(String filePath) {

        logger.debug(String.format("loadAsync from file: '%s'", filePath));
        CompletableFuture.supplyAsync( () -> {
            Properties props = new Properties();
            try(InputStream inputStream = new FileInputStream(filePath)) {
                props.load(inputStream);
            } catch (IOException e) {
                logger.warn(String.format("Unable to read settings, default applied: %s", e));
                props = getDefaultPropsConfig();
            }
            return props;
        }, exec).thenAccept(
                p -> appState.put(AppStateType.SETTINGS, AppStateData.builder().value(p).build())
        );
    }

}
