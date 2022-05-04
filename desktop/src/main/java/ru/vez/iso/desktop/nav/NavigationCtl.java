package ru.vez.iso.desktop.nav;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.ViewType;
import ru.vez.iso.desktop.shared.AppStateData;
import ru.vez.iso.desktop.shared.AppStateType;
import ru.vez.iso.desktop.shared.UserDetails;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Navigation controller. Load all other views/and shows application status
 */
public class NavigationCtl implements Initializable {

    private static final Logger logger = LogManager.getLogger();

    @FXML private BorderPane navigationView;
    @FXML private Button butLogin;
    @FXML private Button butMain;
    @FXML private Button disks;
    @FXML private Button butSettings;
    @FXML private Button butDocuments;
    // Show log messages
    @FXML public Label labelMessages;
    @FXML public Tooltip statusMessage;

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

        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                    if (AppStateType.NOTIFICATION.equals(change.getKey())) {
                        String message = (String) change.getValueAdded().getValue();
                        Platform.runLater(()->showMessage(message));
                    }
                });
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug("");
        // lock controls in prod mode
        boolean prodMode = (Boolean)appState.get(AppStateType.APP_PROD_MODE).getValue();
        this.lockControls(prodMode);
    }

    public void onShowLogin(ActionEvent ev) {
        logger.debug("");
        showView(ViewType.LOGIN);
    }
    public void onShowAbdd(ActionEvent ev) {
        logger.debug("");
        showView(ViewType.ABDD_VIEW);
    }
    public void onShowSettings(ActionEvent ev) {
        logger.debug("");
        showView(ViewType.SETTINGS);
    }
    public void onShowDisks(ActionEvent ev) {
        logger.debug("");
        showView(ViewType.DISK);
    }
    public void onShowDocuments(ActionEvent ev) {
        logger.debug("");
        showView(ViewType.DOCUMENTS);
    }

    //region PRIVATE


    private void showMessage(String message) {
        labelMessages.setText(message);
        statusMessage.hide();
        statusMessage.setAutoHide(true);
        statusMessage.setText(message);
        if (labelMessages.getScene()!=null) {
            statusMessage.show(labelMessages.getScene().getWindow());
        }
    }

    private void lockControls(boolean lock) {

        logger.debug("lock: " + lock);
        butMain.setDisable(lock);
        butLogin.setText(lock ? "Вход" : "Выход");
        this.showMessage("Выполнен " + (lock ? "выход" : "вход"));
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
