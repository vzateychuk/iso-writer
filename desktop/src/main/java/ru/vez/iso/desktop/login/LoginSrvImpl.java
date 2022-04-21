package ru.vez.iso.desktop.login;

import javafx.collections.ObservableMap;
import lombok.extern.java.Log;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.model.UserDetails;
import ru.vez.iso.desktop.state.AppStateData;
import ru.vez.iso.desktop.state.AppStateType;
import ru.vez.iso.desktop.utils.UtilsHelper;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

@Log
public class LoginSrvImpl implements LoginSrv {

    private static Logger logger = LogManager.getLogger();

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private Future<Void> future = CompletableFuture.allOf();

    public LoginSrvImpl(ObservableMap<AppStateType, AppStateData> appState, Executor exec) {
        this.appState = appState;
        this.exec = exec;
    }

    @Override
    public void loginAsync(String username, String password) {

        // Avoid multiply pressing
        if (!future.isDone()) {
            logger.debug("Async operation in progress, skipping");
            return;
        }

        logger.debug(String.format("loginAsync, supplyAsync() user: '%s'", username));
        future = CompletableFuture.supplyAsync(() -> {
            UtilsHelper.makeDelaySec(1);    // TODO make LOGIN HTTP call
            return "admin".equals(username) && "admin".equals(password)
                    ? new UserDetails(username, password, username+"-"+password)
                    : UserDetails.NOT_SIGNED_USER;
        }, exec).thenAccept(userDetails ->
                appState.put(AppStateType.USER_DETAILS, AppStateData.<UserDetails>builder().value(userDetails).build())
        ).exceptionally((ex) -> {
            logger.debug("Unable: " + ex.getLocalizedMessage());
            return null;
        } );
    }

    @Override
    public void logout() {
        logger.debug("logout");
        appState.put(AppStateType.USER_DETAILS, AppStateData.builder().value(UserDetails.NOT_SIGNED_USER).build());
    }
}
