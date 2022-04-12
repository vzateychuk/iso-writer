package ru.vez.iso.desktop.login;

public interface LoginSrv {

    void loginAsync(String username, String password);

    void logout();

}
