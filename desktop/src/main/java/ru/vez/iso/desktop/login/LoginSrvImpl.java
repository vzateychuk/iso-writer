package ru.vez.iso.desktop.login;

import javafx.collections.ObservableMap;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@Log
public class LoginSrvImpl implements LoginSrv {

    private static final Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private final MessageSrv msgSrv;
    private Future<Void> future = CompletableFuture.allOf();

    public LoginSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec, MessageSrv msgSrv) {
        this.appState = appState;
        this.exec = exec;
        this.msgSrv = msgSrv;
    }

    @Override
    public void loginAsync(String username, String password) {

        // Avoid multiply pressing
        if (!future.isDone()) {
            this.msgSrv.news("Операция выполняется");
            return;
        }

        future = CompletableFuture.supplyAsync(() -> {
            logger.debug("user: '{}'", username);
            UtilsHelper.makeDelaySec(1);    // TODO make LOGIN HTTP call
            return "admin".equals(username) && "admin".equals(password)
                    ? new UserDetails(username, password, username+"-"+password)
                    : UserDetails.NOT_SIGNED_USER;
        }, exec).thenAccept(userDetails -> {
            appState.put(AppStateType.USER_DETAILS, AppStateData.<UserDetails>builder().value(userDetails).build());
            this.msgSrv.news("Выполнен вход: " + username);
        }).exceptionally((ex) -> {
            logger.error("Unable: " + ex.getLocalizedMessage());
            return null;
        } );
    }

    @Override
    public void logout() {
        appState.put(AppStateType.USER_DETAILS, AppStateData.builder().value(UserDetails.NOT_SIGNED_USER).build());
        this.msgSrv.news("Выполнен выход");
    }
}
