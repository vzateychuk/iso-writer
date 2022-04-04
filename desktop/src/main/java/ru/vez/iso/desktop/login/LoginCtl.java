package ru.vez.iso.desktop.login;

public class LoginCtl {

    private final LoginSrv service;

    public LoginCtl(LoginSrv service) {
        this.service = service;
    }

    public void onSubmit() {
        System.out.println("LoginCtl.onSubmit");
    }
}
