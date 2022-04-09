package ru.vez.iso.desktop.login;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import ru.vez.iso.desktop.ApplicationState;
import ru.vez.iso.desktop.model.UserDetails;

import java.util.function.Predicate;

public class LoginCtl {

    @FXML private TextField username;
    @FXML private TextField password;
    @FXML private Button butLogin;
    @FXML private Label lbStatus;

    private final ObservableMap<String, ApplicationState> appState;

    private final LoginSrv service;

    public LoginCtl(ObservableMap<String, ApplicationState> appState, LoginSrv service) {
        this.appState = appState;
        this.service = service;
        appState.addListener(
                (MapChangeListener<String, ApplicationState>) change -> {
                  if (ApplicationState.USER_DETAILS.equals(change.getKey())) {
                    System.out.println("LoginCtl.onChanged: " + change.getValueAdded());
                  }
                });
    }

    @FXML void onLogin(ActionEvent ev) {
        System.out.println("LoginCtl.onLogin: " + Thread.currentThread().getName());
        if (!isValid(username.getText(), password.getText())) {
            lbStatus.setText("Username/Password invalid. User is logged out.");
        } else {
            service.loginAsync(username.getText(), password.getText())
                    .thenAccept(userDetails -> Platform.runLater(()->displayLogin(userDetails)));
        }
    }

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

}
