package ru.vez.iso.desktop;

import javafx.application.Application;
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
import ru.vez.iso.desktop.login.HttpClientLoginNoopImpl;
import ru.vez.iso.desktop.login.LoginCtl;
import ru.vez.iso.desktop.login.LoginSrv;
import ru.vez.iso.desktop.login.LoginSrvImpl;
import ru.vez.iso.desktop.main.MainCtl;
import ru.vez.iso.desktop.main.MainSrv;
import ru.vez.iso.desktop.main.MainSrvImpl;
import ru.vez.iso.desktop.main.filecache.FileCacheSrv;
import ru.vez.iso.desktop.main.filecache.FileCacheSrvImpl;
import ru.vez.iso.desktop.main.noop.HttpClientOperationDaysNoopImpl;
import ru.vez.iso.desktop.main.operdays.OperationDayMapper;
import ru.vez.iso.desktop.main.operdays.OperationDaysSrv;
import ru.vez.iso.desktop.main.operdays.OperationDaysSrvImpl;
import ru.vez.iso.desktop.main.storeunits.StorageUnitMapper;
import ru.vez.iso.desktop.main.storeunits.StorageUnitsSrv;
import ru.vez.iso.desktop.main.storeunits.StorageUnitsSrvImpl;
import ru.vez.iso.desktop.main.storeunits.noop.HttpClientStorageUnitsNoopImpl;
import ru.vez.iso.desktop.nav.NavigationCtl;
import ru.vez.iso.desktop.nav.NavigationSrv;
import ru.vez.iso.desktop.nav.NavigationSrvImpl;
import ru.vez.iso.desktop.settings.SettingsCtl;
import ru.vez.iso.desktop.settings.SettingsSrv;
import ru.vez.iso.desktop.settings.SettingsSrvImpl;
import ru.vez.iso.desktop.shared.*;
import ru.vez.iso.desktop.state.ApplicationState;
import ru.vez.iso.desktop.state.RunMode;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
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

        // Set UncaughtExceptionHandler logger (Runtime exception logging)
        Thread.UncaughtExceptionHandler exceptionHandler = new ExceptionLog();
        Thread.currentThread().setUncaughtExceptionHandler(exceptionHandler);

        // Executor where all background tasks will be executed
        int numOfCores = Runtime.getRuntime().availableProcessors();
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(numOfCores * 4);

        // ApplicationState
        ApplicationState state = new ApplicationState();
        state.setRunMode(runMode);

        //region BUILD SERVICES

        // MessageService
        MessageSrv msgSrv = new MessageSrvImpl();

        // SettingService
        SettingsSrv settingsSrv = new SettingsSrvImpl(state, exec, msgSrv);
        String settingsFileName = SettingType.SETTING_FILE.getDefaultValue();
        AppSettings sets;
        if ( Files.exists( Paths.get(settingsFileName) ) ) {
            sets = settingsSrv.load(settingsFileName);
        } else {
            sets = AppSettings.builder()
                    .settingFile( SettingType.SETTING_FILE.getDefaultValue() )
                    .backendAPI( SettingType.BACKEND_API.getDefaultValue() )
                    .filterOpsDays( Integer.parseUnsignedInt(SettingType.OPERATION_DAYS.getDefaultValue()) )
                    .refreshMin( Integer.parseInt(SettingType.REFRESH_PERIOD.getDefaultValue()) )
                    .isoCachePath( SettingType.ISO_CACHE_PATH.getDefaultValue() )
                    .build();
            settingsSrv.save(settingsFileName, sets);
        }
        state.setSettings(sets);

        // FileCacheSrv
        FileCacheSrv fileCache = new FileCacheSrvImpl(state, exec, msgSrv);

        // OperationDaysService - сервис загрузки операционных дней
        HttpClientWrap httpClientOperDays = runMode != RunMode.NOOP ? new HttpClientImpl() : new HttpClientOperationDaysNoopImpl();
        OperationDayMapper operationDayMapper = new OperationDayMapper();
        OperationDaysSrv operDaysSrv = new OperationDaysSrvImpl(state, httpClientOperDays, operationDayMapper);

        // StorageUnitsService - сервис загрузки StorageUnits
        HttpClientWrap httpClientStorageUnits = runMode != RunMode.NOOP ? new HttpClientImpl() : new HttpClientStorageUnitsNoopImpl();
        StorageUnitMapper storageUnitMapper = new StorageUnitMapper();
        StorageUnitsSrv storageUnitsSrv = new StorageUnitsSrvImpl(state, httpClientStorageUnits, storageUnitMapper);

        MainSrv mainSrv  = new MainSrvImpl(state, exec, msgSrv, operDaysSrv, storageUnitsSrv);

        // LoginService
        HttpClientWrap httpClientLogin = runMode != RunMode.NOOP ? new HttpClientImpl() : new HttpClientLoginNoopImpl();
        LoginSrv loginSrv = new LoginSrvImpl(state, exec, msgSrv, httpClientLogin);

        // ViewCache with views
        Map<ViewType, Parent> viewCache = buildViewCache(state,exec,msgSrv,settingsSrv,loginSrv,mainSrv,fileCache);
        settingsSrv.loadAsync(SettingType.SETTING_FILE.getDefaultValue());

        //endregion

        // create filecache directory and readAsync
        createFileCacheIfNotExists( sets.getIsoCachePath() );
        fileCache.readFileCacheAsync( sets.getSettingFile() );

        // Set OnClose confirmation hook
        stage.setOnCloseRequest(e -> {
            e.consume();
            if (runMode == RunMode.PROD && !UtilsHelper.getConfirmation("Вы уверены?")) {
                return;
            }
            exec.shutdownNow();
            stage.close();
        });

        // Build and show the navigation view
        stage.getIcons().add(new Image(DesktopApp.class.getResourceAsStream("image/iso.png")));
        NavigationSrv navSrv = new NavigationSrvImpl();
        Parent navigation = buildView(
                ViewType.NAVIGATION, t->new NavigationCtl(state, navSrv, viewCache, msgSrv)
        );
        stage.setScene(new Scene(navigation));
        String appVersion = this.getVersion();
        stage.setTitle(String.format("Desktop. Версия:%s; Режим:%s", appVersion, runMode.name()));
        logger.info("---> Application started! ver: {}, mode: {} <---", appVersion, runMode);
        stage.show();
    }

    public static void main(String[] args) {

        // parse command argument to define application run-mode
        runMode = getAppRunMode(args);
        logger.debug("Run mode: " + runMode);
        launch(args);
    }

    //region PRIVATE

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
     * Create viewCache Map just to switch between views
     * Application state and services created and injected
     * */
    private Map<ViewType, Parent> buildViewCache(
            ApplicationState state,
            ScheduledExecutorService exec,
            MessageSrv msgSrv,
            SettingsSrv settingsSrv,
            LoginSrv loginSrv,
            MainSrv mainSrv,
            FileCacheSrv fileCache) throws IOException {

        Map<ViewType, Parent> viewCache = new HashMap<>();

        // SettingsView + SettingService
        viewCache.put(ViewType.SETTINGS, buildView( ViewType.SETTINGS, t->new SettingsCtl(state, settingsSrv) ));

        // LoginView + LoginService
        viewCache.put(ViewType.LOGIN, buildView( ViewType.LOGIN, t->new LoginCtl(state, loginSrv)) );

        // DocumentView + DocumentService
        DocMapper mapper = new DocMapperImpl();
        DocSrv docSrv = new DocSrvImpl(state, exec, mapper, msgSrv);
        viewCache.put(ViewType.DOCUMENTS, buildView( ViewType.DOCUMENTS, t->new DocumentCtl(state, docSrv, msgSrv)) );

        // MainView + MainService
        viewCache.put(ViewType.MAIN_VIEW, buildView(ViewType.MAIN_VIEW, t->new MainCtl(state, mainSrv, fileCache, msgSrv)));

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
