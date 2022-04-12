package ru.vez.iso.desktop.login;

import javafx.collections.ObservableMap;
import lombok.extern.java.Log;
import ru.vez.iso.desktop.model.UserDetails;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Log
public class LoginSrvImpl implements LoginSrv {

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;

    public LoginSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec) {
        this.appState = appState;
        this.exec = exec;
    }

    @Override
    public void loginAsync(String username, String password) {

        CompletableFuture.supplyAsync(() -> {
            log.info(String.format("loginAsync, user: '%s'", username));
            makeDelaySec(1);    // TODO make LOGIN HTTP call
            return "admin".equals(username) && "admin".equals(password)
                    ? new UserDetails(username, password, username+"-"+password)
                    : UserDetails.NOT_SIGNED_USER;
        }, exec).thenAccept(userDetails ->
                appState.put(AppStateType.USER_DETAILS, AppStateData.<UserDetails>builder().value(userDetails).build())
        );
    }

    @Override
    public void logout() {
        log.info("logoutAsync");
        appState.put(AppStateType.USER_DETAILS, AppStateData.builder().value(UserDetails.NOT_SIGNED_USER).build());
    }

    //region Private

    private void makeDelaySec(int delay) {
        try {
            Thread.sleep(delay * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //endregion
}
