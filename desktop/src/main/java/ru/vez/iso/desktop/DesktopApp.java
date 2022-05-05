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
import ru.vez.iso.desktop.document.*;
import ru.vez.iso.desktop.login.LoginCtl;
import ru.vez.iso.desktop.login.LoginSrv;
import ru.vez.iso.desktop.login.LoginSrvImpl;
import ru.vez.iso.desktop.main.*;
import ru.vez.iso.desktop.nav.NavigationCtl;
import ru.vez.iso.desktop.nav.NavigationSrvImpl;
import ru.vez.iso.desktop.settings.SettingsCtl;
import ru.vez.iso.desktop.settings.SettingsSrv;
import ru.vez.iso.desktop.settings.SettingsSrvImpl;
import ru.vez.iso.desktop.shared.*;
import ru.vez.iso.desktop.utils.UtilsHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runnable Application class
 * DI implemented for :
 * - Views/Controllers
 * - Services
 * - ApplicationState
 * */
public class DesktopApp extends Application {

    private static final Logger logger = LogManager.getLogger();
    private static boolean isProdMode = false;

    @Override
    public void start(Stage stage) throws IOException {

        // Create application state
        ObservableMap<AppStateType, AppStateData> appState = createAppStateMap();

        // Create executor where all background tasks will be executed
        int numOfCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores * 4);

        // Build ViewCache with all views
        Map<ViewType, Parent> viewCache = buildViewCache(appState, executorService);

        // create filecache directory
        createFileCacheIfNotExists(SettingType.ISO_CACHE_PATH.getDefaultValue());

        // Set OnClose confirmation hook
        stage.setOnCloseRequest(e -> {
            e.consume();
            if (isProdMode && !UtilsHelper.getConfirmation("Вы уверены? Может быть запущена запись на диск.")) {
                return;
            }
            executorService.shutdownNow();
            stage.close();
        });

        // Build and show the navigation view
        stage.setTitle("Writer App" + (isProdMode ? "" : " DEV Mode"));
        stage.getIcons().add(new Image(DesktopApp.class.getResourceAsStream("image/iso.png")));
        Parent navigation = buildView(
                ViewType.NAVIGATION, t->new NavigationCtl(appState, new NavigationSrvImpl(), viewCache)
        );
        stage.setScene(new Scene(navigation));
        logger.info("Starting the application! -----------------------");
        stage.show();
    }

    public static void main(String[] args) {

        // parse command argument to define application run-mode
        isProdMode = getAppIsProdMode(args);
        logger.debug("Main. DEV mode? " + !isProdMode);
        launch(args);
    }

    //region Private


    /**
     * Created ApplicationState Map with initial values
     * */
    private ObservableMap<AppStateType, AppStateData> createAppStateMap() {

        ObservableMap<AppStateType, AppStateData> appState = createDefaultAppState();
        appState.put(AppStateType.APP_PROD_MODE, AppStateData.builder().value(isProdMode).build());
        Map<String, LoadStatus> loadStatusMap = new ConcurrentHashMap<>();
        appState.put(AppStateType.LOAD_ISO_STATUS, AppStateData.builder().value(loadStatusMap).build());
        return appState;
    }

    private static boolean getAppIsProdMode(String[] args) {

        Options options = new Options();

        Option input = new Option("m", "mode", true, "run mode: prod/dev");
        input.setRequired(false);
        options.addOption(input);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;//not a good practice, it serves it purpose

        try {
            cmd = parser.parse(options, args);
            String mode = cmd.getOptionValue("mode", "prod");
            return !mode.isEmpty() && "prod".equals(mode.toLowerCase());
        } catch (ParseException e) {
            logger.debug(e.getMessage());
            formatter.printHelp("jar-name", options);
            System.exit(1);
        }
        return false;
    }

    /**
     * Create Listenable application state Map just to to keep UI consistent by listening state change events.
     * */
    private ObservableMap<AppStateType, AppStateData> createDefaultAppState() {

        Map<AppStateType, AppStateData> mapState = new ConcurrentHashMap<>();
        mapState.put( AppStateType.USER_DETAILS, AppStateData.builder().value(UserDetails.NOT_SIGNED_USER).build() );

        return FXCollections.observableMap(mapState);
    }

    /**
     * Create viewCache Map just to switch between views
     * Application state and services created and injected
     * */
    private Map<ViewType, Parent> buildViewCache(ObservableMap<AppStateType, AppStateData> appState, Executor exec) throws IOException {

        Map<ViewType, Parent> viewCache = new HashMap<>();

        // create SettingsView and read application settings async
        SettingsSrv settingsSrv = new SettingsSrvImpl(appState, exec);
        viewCache.put(ViewType.SETTINGS, buildView( ViewType.SETTINGS, t -> new SettingsCtl(appState, settingsSrv) ));

        LoginSrv loginSrv = new LoginSrvImpl(appState, exec);
        loginSrv.logout();
        viewCache.put(ViewType.LOGIN, buildView(ViewType.LOGIN,t->new LoginCtl(appState, loginSrv)));

        DocumentMapper mapper = new DocumentMapperImpl();
        DocumentSrv docSrv = new DocumentSrvImpl(appState, exec, mapper);
        viewCache.put(ViewType.DOCUMENTS, buildView(ViewType.DOCUMENTS,t->new DocumentCtl(appState, docSrv)));

        MainSrv mainSrv = new MainSrvImpl(appState, exec);
        DisksSrv disksSrv = new DisksSrvImpl(appState, exec);
        viewCache.put(ViewType.MAIN_VIEW, buildView(ViewType.MAIN_VIEW, t->new MainCtl(appState, mainSrv, disksSrv)));

        settingsSrv.loadAsync(SettingType.SETTING_FILE.getDefaultValue());
        disksSrv.readFileCacheAsync(SettingType.ISO_CACHE_PATH.getDefaultValue());

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

    //endregion
}
