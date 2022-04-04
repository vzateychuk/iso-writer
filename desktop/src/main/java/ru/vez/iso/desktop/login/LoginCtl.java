package ru.vez.iso.desktop.login;

public class LoginCtl {

    private final LoginService service;

    public LoginCtl(LoginService service) {
        this.service = service;
    }

    public void onSubmit() {
        System.out.println("LoginCtl.onSubmit");
    }
}
