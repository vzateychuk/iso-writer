package ru.vez.iso.desktop.nav;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.ViewType;
import ru.vez.iso.desktop.model.UserDetails;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Navigation controller. Load all other views/and shows application status
 */
public class NavigationCtl implements Initializable {

    private static Logger logger = LogManager.getLogger();

    @FXML private BorderPane navigationView;
    @FXML private Button disks;
    @FXML private Button login;
    @FXML private Button main;
    @FXML private Button settings;
    @FXML private Button documents;

    private final NavigationSrv service;
    private final Map<ViewType, Parent> viewCache;
    private final ObservableMap<AppStateType, AppStateData> appState;
    private ViewType currView = ViewType.WELCOME;

    public NavigationCtl(
            ObservableMap<AppStateType, AppStateData> appState,
            NavigationSrv service,
            Map<ViewType, Parent> viewCache
    ) {
        this.service = service;
        this.viewCache = viewCache;
        this.appState = appState;
        // Login listener: Enable controls on login
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.USER_DETAILS.equals(change.getKey())) {
                        UserDetails userDetails = (UserDetails) change.getValueAdded().getValue();
                        Platform.runLater(()->lockControls(userDetails == UserDetails.NOT_SIGNED_USER));
                    }
                });
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("initialize");
        // lock controls in prod mode
        boolean prodMode = (Boolean)appState.get(AppStateType.APP_PROD_MODE).getValue();
        this.lockControls(prodMode);
    }

    public void onShowLogin(ActionEvent ev) {
        logger.debug(".onShowLogin");
        showView(ViewType.LOGIN);
    }
    public void onShowAbdd(ActionEvent ev) {
        logger.debug("onShowAbdd");
        showView(ViewType.ABDD_VIEW);
    }
    public void onShowSettings(ActionEvent ev) {
        logger.debug("onShowSettings");
        showView(ViewType.SETTINGS);
    }
    public void onShowDisks(ActionEvent ev) {
        logger.debug("onShowDisks");
        showView(ViewType.DISK);
    }
    public void onShowDocuments(ActionEvent ev) {
        logger.debug("onShowDocuments");
        showView(ViewType.DOCUMENTS);
    }

    //region Private

    private void lockControls(boolean lock) {
        logger.debug("lockControls: " + lock);
        disks.setDisable(lock);
        main.setDisable(lock);
        documents.setDisable(lock);
        login.setText(lock ? "Вход" : "Выход");
    }

    private void showView(ViewType view) {

        if (currView == view) {
            return;
        }

        Parent root = viewCache.get(view);
        navigationView.setCenter(root);
        currView = view;
    }

    //endregion
}
