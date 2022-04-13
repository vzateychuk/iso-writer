package ru.vez.iso.desktop.settings;

import javafx.collections.ObservableMap;
import lombok.extern.java.Log;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.io.*;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@Log
public class SettingsSrvImpl implements SettingsSrv {

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
            log.info("Async operation in progress, skipping");
            return;
        }

        log.info(String.format("saveAsync to file: '%s'", filePath));
        CompletableFuture.supplyAsync( () -> {
            Properties p = props;
            try(OutputStream outputStream = new FileOutputStream(filePath)){
                p.store(outputStream, "ISO Writer properties");
            } catch (IOException e) {
                log.warning(String.format("Unable to save file: '%s'\n%s", filePath, e));
                p = getDefaultPropsConfig();
            }
            return p;
        }, exec).thenAccept(
                p -> appState.put(AppStateType.SETTINGS, AppStateData.builder().value(p).build())
        );
    }

    /**
     * Load Settings asynchronously by using java 8 try-with-resources structure
     * so the input stream will be closed automatically
     */
    @Override
    public void loadAsync(String filePath) {

        log.info(String.format("loadAsync from file: '%s'", filePath));
        CompletableFuture.supplyAsync( () -> {
            Properties props = new Properties();
            try(InputStream inputStream = new FileInputStream(filePath)) {
                props.load(inputStream);
            } catch (IOException e) {
                log.warning(String.format("Unable to read file: '%s'\n%s", filePath, e));
                props = getDefaultPropsConfig();
            }
            return props;
        }, exec).thenAccept(
                p -> appState.put(AppStateType.SETTINGS, AppStateData.builder().value(p).build())
        );
    }

}
