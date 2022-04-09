package ru.vez.iso.desktop.login;

import javafx.collections.ObservableMap;
import ru.vez.iso.desktop.ApplicationState;
import ru.vez.iso.desktop.model.UserDetails;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class LoginSrvImpl implements LoginSrv {

    private final Executor exec;
    private final ObservableMap<String, ApplicationState> appState;

    public LoginSrvImpl(Executor exec, ObservableMap<String, ApplicationState> appState) {
        this.exec = exec;
        this.appState = appState;
    }


    @Override
    public CompletableFuture<UserDetails> loginAsync(String username, String password) {

        if ("admin".equals(username) && "admin".equals(password) ) {
            return CompletableFuture.supplyAsync(() -> {
                System.out.println("loginAsync: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                UserDetails logged = new UserDetails(username, password, username+"-"+password);
                ApplicationState loggedState = new ApplicationState();
                loggedState.setUserDetails(logged);
                appState.put(ApplicationState.USER_DETAILS, loggedState);
                return logged;
            }, exec);
        } else {
            return CompletableFuture.supplyAsync(() -> {
                System.out.println("loginAsync: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return UserDetails.NOT_SIGNED_USER;
            }, exec);
        }

    }
}
