package ru.vez.iso.desktop.nav;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import ru.vez.iso.desktop.ViewType;
import ru.vez.iso.desktop.model.UserDetails;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.util.Map;

/**
 * Navigation controller. Load all other views/and shows application status
 */
public class NavigationCtl {

    @FXML private BorderPane navigationView;
    @FXML private Button disks;
    @FXML private Button login;
    @FXML private Button main;
    @FXML private Button settings;
    @FXML private Button search;

    private ViewType currView = ViewType.WELCOME;
    private final NavigationSrv service;
    private final Map<ViewType, Parent> viewCache;
    private final ObservableMap<AppStateType, AppStateData> appState;

    public NavigationCtl(
            ObservableMap<AppStateType, AppStateData> appState,
            NavigationSrv service,
            Map<ViewType, Parent> viewCache
    ) {
        this.service = service;
        this.viewCache = viewCache;
        this.appState = appState;
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.USER_DETAILS.equals(change.getKey())) {
                        UserDetails userDetails = (UserDetails) change.getValueAdded().getValue();
                        Platform.runLater(()->lockControls(userDetails == UserDetails.NOT_SIGNED_USER));
                    }
                });
    }

    @FXML
    public void initialize() {
        System.out.println("NavigationCtl.initialize");
        // lock controls in prod mode
        boolean prodMode = (Boolean)appState.get(AppStateType.APP_PROD_MODE).getValue();
        this.lockControls(prodMode);
    }

    public void onShowLogin(ActionEvent ev) {
        System.out.println("NavigationCtl.onShowLogin");
        loadView(ViewType.LOGIN);
    }
    public void onShowMain(ActionEvent ev) {
        System.out.println("NavigationCtl.onShowMain");
        loadView(ViewType.MAIN);
    }
    public void onShowSettings(ActionEvent ev) {
        System.out.println("NavigationCtl.onShowSettings");
        loadView(ViewType.SETTINGS);
    }
    public void onShowDisks(ActionEvent ev) {
        System.out.println("NavigationCtl.onShowDisks");
        loadView(ViewType.DISK);
    }
    public void onShowSearch(ActionEvent ev) {
    System.out.println("NavigationCtl.onShowSearch");
    }
    public void onExit(ActionEvent ev) {
    System.out.println("NavigationCtl.onExit");
    }

    //region Private

    private void lockControls(boolean lock) {
        disks.setDisable(lock);
        main.setDisable(lock);
        search.setDisable(lock);
    }

    private void loadView(ViewType view) {

        if (currView == view) {
            return;
        }

        Parent root = viewCache.get(view);
        navigationView.setCenter(root);
        currView = view;
    }

    //endregion
}
