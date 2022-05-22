package ru.vez.iso.desktop;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.docs.*;
import ru.vez.iso.desktop.login.LoginCtl;
import ru.vez.iso.desktop.login.LoginSrv;
import ru.vez.iso.desktop.login.LoginSrvImpl;
import ru.vez.iso.desktop.main.*;
import ru.vez.iso.desktop.nav.NavigationCtl;
import ru.vez.iso.desktop.nav.NavigationSrv;
import ru.vez.iso.desktop.nav.NavigationSrvImpl;
import ru.vez.iso.desktop.settings.SettingsCtl;
import ru.vez.iso.desktop.settings.SettingsSrv;
import ru.vez.iso.desktop.settings.SettingsSrvImpl;
import ru.vez.iso.desktop.shared.*;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.jar.Manifest;

/**
 * Runnable Application class
 * DI implemented for :
 * - Views/Controllers
 * - Services
 * - ApplicationState
 * */
public class DesktopApp extends Application {

    private static final Logger logger = LogManager.getLogger();
    private static RunMode runMode;

    @Override
    public void start(Stage stage) throws IOException {

        // Create application state
        ObservableMap<AppStateType, AppStateData> appState = createAppStateMap();

        // Create executor where all background tasks will be executed
        int numOfCores = Runtime.getRuntime().availableProcessors();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(numOfCores * 4);

        MessageSrv msgSrv = new MessageSrvImpl();
        // Build ViewCache with all views
        Map<ViewType, Parent> viewCache = buildViewCache(appState, executorService, msgSrv);

        // create filecache directory
        createFileCacheIfNotExists(SettingType.ISO_CACHE_PATH.getDefaultValue());

        // Set OnClose confirmation hook
        stage.setOnCloseRequest(e -> {
            e.consume();
            if (runMode == RunMode.PROD && !UtilsHelper.getConfirmation("Вы уверены?")) {
                return;
            }
            executorService.shutdownNow();
            stage.close();
        });

        // Build and show the navigation view
        stage.getIcons().add(new Image(DesktopApp.class.getResourceAsStream("image/iso.png")));
        NavigationSrv navSrv = new NavigationSrvImpl();
        Parent navigation = buildView(
                ViewType.NAVIGATION, t->new NavigationCtl(appState, navSrv, viewCache, msgSrv)
        );
        stage.setScene(new Scene(navigation));
        String appVersion = this.getVersion();
        stage.setTitle(String.format("Desktop. Режим:%s. Версия:%s", runMode.name(), appVersion));
        logger.info("---> Application started! ver: {} <---", appVersion);
        stage.show();
    }

    public static void main(String[] args) {

        // parse command argument to define application run-mode
        runMode = getAppRunMode(args);
        logger.info("Run mode: " + runMode);
        launch(args);
    }

    //region Private


    /**
     * Created ApplicationState Map with initial values
     * */
    private ObservableMap<AppStateType, AppStateData> createAppStateMap() {

        ObservableMap<AppStateType, AppStateData> appState = createDefaultAppState();
        appState.put(AppStateType.APP_RUN_MODE, AppStateData.builder().value(runMode).build());
        return appState;
    }

    /**
     * Parse args for Application run-mode
     * Possible values prod/dev/noop
     *
     * @see RunMode
     * */
    private static RunMode getAppRunMode(String[] args) {

        Options options = new Options();

        Option input = new Option("m", "mode", true, "run mode: prod/dev/noop");
        input.setRequired(false);
        options.addOption(input);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;//not a good practice, it serves it purpose

        try {
            cmd = parser.parse(options, args);
            String sMode = cmd.getOptionValue("mode", "noop").toUpperCase();
            return RunMode.valueOf(sMode);
        } catch (ParseException e) {
            logger.debug(e.getMessage());
            formatter.printHelp("jar-name", options);
            System.exit(1);
        }
        return RunMode.NOOP;
    }

    /**
     * Create Listenable application state Map just to to keep UI consistent by listening state change events.
     * */
    private ObservableMap<AppStateType, AppStateData> createDefaultAppState() {

        Map<AppStateType, AppStateData> appState = new ConcurrentHashMap<>();
        appState.put(AppStateType.ZIP_DIR, AppStateData.builder().value("").build());
        return FXCollections.observableMap(appState);
    }

    /**
     * Create viewCache Map just to switch between views
     * Application state and services created and injected
     * */
    private Map<ViewType, Parent> buildViewCache(
                            ObservableMap<AppStateType,
                            AppStateData> appState,
                            ScheduledExecutorService exec,
                            MessageSrv msgSrv) throws IOException {

        Map<ViewType, Parent> viewCache = new HashMap<>();

        // create SettingsView and read application settings async
        SettingsSrv setsSrv = new SettingsSrvImpl(appState, exec, msgSrv);
        viewCache.put(ViewType.SETTINGS, buildView( ViewType.SETTINGS, t -> new SettingsCtl(appState, setsSrv) ));

        LoginSrv loginSrv = new LoginSrvImpl(appState, exec, msgSrv);
        viewCache.put(ViewType.LOGIN, buildView(ViewType.LOGIN,t->new LoginCtl(appState, loginSrv)));

        DocMapper mapper = new DocMapperImpl();
        DocSrv docSrv = new DocSrvImpl(appState, exec, mapper, msgSrv);
        viewCache.put(ViewType.DOCUMENTS, buildView(ViewType.DOCUMENTS,t->new DocumentCtl(appState, docSrv, msgSrv)));

        MainSrv mainSrv = new MainSrvImpl(appState, exec, msgSrv);
        CacheSrv cacheSrv = new CacheSrvImpl(appState, exec, msgSrv);
        viewCache.put(ViewType.MAIN_VIEW, buildView(ViewType.MAIN_VIEW, t->new MainCtl(appState, mainSrv, cacheSrv, msgSrv)));

        setsSrv.loadAsync(SettingType.SETTING_FILE.getDefaultValue());
        cacheSrv.readFileCacheAsync(SettingType.ISO_CACHE_PATH.getDefaultValue());

        return viewCache;
    }

    private Parent buildView(ViewType view, Callback<Class<?>, Object> controllerFactory) throws IOException {

        logger.debug("BuildView from: " + view.getFileName());
        FXMLLoader loader = new FXMLLoader(getClass().getResource(view.getFileName()));
        loader.setControllerFactory(controllerFactory);
        return loader.load();
    }


    /**
     * Creates the directory along with any of the following parent directories if they do not already exist
     * */
    private void createFileCacheIfNotExists(String cachePath) {
        Path path = Paths.get(cachePath);
        if  (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException ex) {
                logger.debug("Unable to create directory: " + cachePath);
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Get application version
     * */
    private String getVersion() {
        URLClassLoader cl = (URLClassLoader) DesktopApp.class.getClassLoader();
        URL url = cl.findResource("META-INF/MANIFEST.MF");
        String result = "";
        try {
            Manifest manifest = new Manifest(url.openStream());
            result = manifest.getMainAttributes().getValue("version");
        } catch (IOException ex) {
            logger.warn("Unable to read version", ex);
        }
        return result;
    }
    //endregion
}
