package ru.vez.iso.desktop.login;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.extern.java.Log;
import ru.vez.iso.desktop.model.UserDetails;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.util.function.Predicate;

@Log
public class LoginCtl {

    @FXML private TextField username;
    @FXML private TextField password;
    @FXML private Button butLogin;
    @FXML private Label lbStatus;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final LoginSrv service;

    public LoginCtl(ObservableMap<AppStateType, AppStateData> appState, LoginSrv service) {
        this.appState = appState;
        this.service = service;
        appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                  if (AppStateType.USER_DETAILS.equals(change.getKey())) {
                      log.info("LoginCtl.onChanged: " + Thread.currentThread().getName());
                      UserDetails userDetails = (UserDetails) change.getValueAdded().getValue();
                      Platform.runLater(()->displayLogin(userDetails));
                  }
                });
    }

    @FXML void onLogin(ActionEvent ev) {
        log.info("LoginCtl.onLogin: " + Thread.currentThread().getName());
        if (!isValid(username.getText(), password.getText())) {
            lbStatus.setText("Username/Password invalid. User is logged out.");
        } else {
            service.tryLogin(username.getText(), password.getText());
        }
    }

    //region Private

    private void displayLogin(UserDetails userDetails) {
        if (userDetails.isLogged()) {
            lbStatus.setText(String.format("'%s' logged in successfully", username.getText()));
            username.setText("");
            password.setText("");
        } else {
            lbStatus.setText(String.format("'%s' unable to login. User is logged out.", username.getText()));
        }
    }

    private boolean isValid(String username, String pwd) {
        Predicate<String> notEmpty = s -> s != null && s.trim().length() > 0;
        return notEmpty.test(username) && notEmpty.test(pwd);
    }

    //endregion
}
