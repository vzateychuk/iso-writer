package ru.vez.iso.desktop.login;

import javafx.application.Platform;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import lombok.extern.java.Log;
import ru.vez.iso.desktop.model.UserDetails;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.util.function.Predicate;

/**
 * Controller for LoginForm
 * */
@Log
public class LoginCtl {

    @FXML private TextField username;
    @FXML private TextField password;
    @FXML private Button butLogin;
    @FXML public Button butLogout;
    @FXML private Label lbStatus;

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final LoginSrv service;

    public LoginCtl(ObservableMap<AppStateType, AppStateData> appState, LoginSrv service) {
        this.service = service;
        this.appState = appState;
        this.appState.addListener(
                (MapChangeListener<AppStateType, AppStateData>) change -> {
                  if (AppStateType.USER_DETAILS.equals(change.getKey())) {
                      UserDetails userDetails = (UserDetails) change.getValueAdded().getValue();
                      Platform.runLater(()->displayLogin(userDetails));
                  }
                });
    }

    @FXML public void onLogin(ActionEvent ev) {
        service.loginAsync(username.getText(), password.getText());
    }

    @FXML public void onPressEnter(KeyEvent ev) {
        if( ev.getCode() == KeyCode.ENTER ) {
            onLogin(null);
        }
    }

    @FXML public void onLogout(ActionEvent ev) {
        service.logoutAsync();
    }

    //region Private

    private void displayLogin(UserDetails userDetails) {
        String statusMessage = "Logged out";
        if (userDetails.isLogged()) {
            username.setText("");
            password.setText("");
            statusMessage = String.format("Logged as '%s'", userDetails.getUsername());
        }
        butLogin.setDisable(userDetails.isLogged());
        butLogout.setDisable(!userDetails.isLogged());
        lbStatus.setText(statusMessage);
        showAlert(statusMessage);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Alert");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private boolean isValid(String username, String pwd) {
        Predicate<String> notEmpty = s -> s != null && s.trim().length() > 0;
        return notEmpty.test(username) && notEmpty.test(pwd);
    }

    //endregion
}
