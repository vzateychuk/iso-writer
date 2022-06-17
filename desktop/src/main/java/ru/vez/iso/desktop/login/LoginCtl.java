package ru.vez.iso.desktop.login;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.state.ApplicationState;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for LoginForm
 * */
@Log
public class LoginCtl implements Initializable {

    private static final Logger logger = LogManager.getLogger();

    @FXML private TextField username;
    @FXML private TextField password;
    @FXML private Button butLogin;
    @FXML public Button butLogout;

    private final ApplicationState state;
    private final LoginSrv service;

    public LoginCtl(ApplicationState state, LoginSrv service) {
        this.state = state;
        this.service = service;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        this.state.userDetailsProperty().addListener(
                (o, oldVal, newVal) -> Platform.runLater( ()->changeControls(newVal.isLogged()) )
        );
    }

    @FXML public void onPressLogin(ActionEvent ev) {
        logger.debug("");
        service.loginAsync(username.getText(), password.getText());
    }

    @FXML public void onPressEnter(KeyEvent ev) {
        if( ev.getCode() == KeyCode.ENTER ) {
            onPressLogin(null);
        }
    }

    @FXML public void onLogout(ActionEvent ev) {
        logger.debug("");
        service.logout();
    }

    //region PRIVATE

    private void changeControls(boolean logged) {
        if (logged) {
            username.setText("");
            password.setText("");
        }
        butLogin.setDisable(logged);
        butLogout.setDisable(!logged);
    }

    //endregion
}
