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

@Log
public class LoginCtl {

    @FXML private TextField username;
    @FXML private TextField password;
    @FXML private Button butLogin;
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

    @FXML void onLogin(ActionEvent ev) {
        if (!isValid(username.getText(), password.getText())) {
            lbStatus.setText("Username/Password invalid");
        } else {
            service.tryLogin(username.getText(), password.getText());
        }
    }

    @FXML public void onPressEnter(KeyEvent ev) {
        if( ev.getCode() == KeyCode.ENTER ) {
            onLogin(null);
        }
    }

    //region Private

    private void displayLogin(UserDetails userDetails) {
        if (userDetails.isLogged()) {
            lbStatus.setText(String.format("Logged as '%s'", userDetails.getUsername()));
            username.setText("");
            password.setText("");

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText(null);
            alert.setContentText(String.format("Logged as '%s'", userDetails.getUsername()));
            alert.showAndWait();

        } else {
            lbStatus.setText("Logged out.");
        }
    }

    private boolean isValid(String username, String pwd) {
        Predicate<String> notEmpty = s -> s != null && s.trim().length() > 0;
        return notEmpty.test(username) && notEmpty.test(pwd);
    }

    //endregion
}
