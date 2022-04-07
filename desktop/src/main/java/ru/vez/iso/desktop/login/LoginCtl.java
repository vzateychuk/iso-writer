package ru.vez.iso.desktop.login;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class LoginCtl {

    @FXML private TextField username;
    @FXML private TextField password;
    @FXML private Button butLogin;

    private final LoginSrv service;

    public LoginCtl(LoginSrv service) {
        this.service = service;
    }

    @FXML void onLogin(ActionEvent ev) {
        System.out.println("LoginCtl.onLogin");
    }

}
