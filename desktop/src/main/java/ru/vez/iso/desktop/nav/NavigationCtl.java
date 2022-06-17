package ru.vez.iso.desktop.nav;

import javafx.application.Platform;
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
import ru.vez.iso.desktop.shared.MessageSrv;
import ru.vez.iso.desktop.shared.UserDetails;
import ru.vez.iso.desktop.state.ApplicationState;

import java.net.URL;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.ResourceBundle;

/**
 * Navigation controller. Load all other views/and shows application status
 */
public class NavigationCtl implements Initializable, Observer {

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
    private final ApplicationState state;
    private final MessageSrv msgSrv;
    private ViewType currView = ViewType.WELCOME;

    public NavigationCtl(
            ApplicationState state,
            NavigationSrv service,
            Map<ViewType, Parent> viewCache,
            MessageSrv msgSrv) {
        this.service = service;
        this.viewCache = viewCache;
        this.state = state;
        this.msgSrv = msgSrv;
        this.msgSrv.addObserver(this);

        // Login listener: Enable controls on login
        this.state.userDetailsProperty().addListener( (o, old, newVal) ->
            Platform.runLater(()->lockControls(newVal == UserDetails.NOT_SIGNED_USER))
        );
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.debug(location);
    }

    @Override
    public void update(Observable o, Object message) {
        Platform.runLater(()->showMessage((String)message));
    }

    public void onLoginLogout(ActionEvent ev) {
        logger.debug("");
        showView(ViewType.LOGIN);
    }
    public void onShowMain(ActionEvent ev) {
        showView(ViewType.MAIN_VIEW);
    }
    public void onShowSettings(ActionEvent ev) {
        showView(ViewType.SETTINGS);
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

        logger.debug(lock);
        butMain.setDisable(lock);
        showView(lock ? ViewType.LOGIN : ViewType.MAIN_VIEW);
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
