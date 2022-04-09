package ru.vez.iso.desktop;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.util.Callback;
import lombok.extern.java.Log;
import ru.vez.iso.desktop.disks.DiskCtl;
import ru.vez.iso.desktop.disks.DisksSrvImpl;
import ru.vez.iso.desktop.login.LoginCtl;
import ru.vez.iso.desktop.login.LoginSrvImpl;
import ru.vez.iso.desktop.main.MainCtl;
import ru.vez.iso.desktop.main.MainSrvImpl;
import ru.vez.iso.desktop.model.UserDetails;
import ru.vez.iso.desktop.nav.NavigationCtl;
import ru.vez.iso.desktop.nav.NavigationSrvImpl;
import ru.vez.iso.desktop.settings.SettingsCtl;
import ru.vez.iso.desktop.settings.SettingsSrvImpl;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Runnable Application class
 * DI implemented for :
 * Views/Controllers
 * Services
 * ApplicationState
 * */
@Log
public class DesktopApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {

        // Create application state
        ObservableMap<AppStateType, AppStateData> appState = createDefaultAppState();

        // Create executor where all background tasks will be executed
        int numOfCores = Runtime.getRuntime().availableProcessors();
        ExecutorService executorService = Executors.newFixedThreadPool(numOfCores * 4);

        // Build ViewCache with all views
        Map<ViewType, Parent> viewCache = buildViewCache(appState, executorService);

        // Build and show the navigation view
        stage.setTitle("ISO Writer App");
/*
        stage.setOnCloseRequest(e -> {
            e.consume();
            if (getCloseConfirmation()) {
                executorService.shutdownNow();
                stage.close();
            }
        });
*/
        Parent navigation = buildView(
                ViewType.NAVIGATION, t->new NavigationCtl(appState, new NavigationSrvImpl(), viewCache)
        );
        stage.setScene(new Scene(navigation));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    //region Private

    /**
     * @See https://betacode.net/11529/javafx-alert-dialog
     * */
    private boolean getCloseConfirmation() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Warning!");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure? Some process might be running.");
        Optional<ButtonType> option = alert.showAndWait();
        return option.isPresent() && option.get() == ButtonType.OK;
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
    private Map<ViewType, Parent> buildViewCache(ObservableMap<AppStateType, AppStateData> state, Executor exec) throws IOException {

        Map<ViewType, Parent> viewCache = new HashMap<>();

        viewCache.put(ViewType.LOGIN, buildView( ViewType.LOGIN, t->new LoginCtl(state, new LoginSrvImpl(state, exec))));
        viewCache.put(ViewType.MAIN, buildView( ViewType.MAIN, t->new MainCtl(state, new MainSrvImpl(state, exec))));
        viewCache.put(ViewType.DISK, buildView( ViewType.DISK, t->new DiskCtl(new DisksSrvImpl())));
        viewCache.put(ViewType.SETTINGS, buildView( ViewType.SETTINGS, t -> new SettingsCtl(new SettingsSrvImpl())));

        return viewCache;
    }

    private Parent buildView(ViewType view, Callback<Class<?>, Object> controllerFactory) throws IOException {

        log.info("BuildView from file: " + view.getFileName());
        FXMLLoader loader = new FXMLLoader(getClass().getResource(view.getFileName()));
        loader.setControllerFactory(controllerFactory);
        return loader.load();
    }

    //endregion
}
