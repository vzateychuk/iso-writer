package ru.vez.iso.desktop.login;

import javafx.collections.ObservableMap;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import ru.vez.iso.desktop.shared.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class LoginSrvImpl implements LoginSrv {

    private static final Logger logger = LogManager.getLogger();
    private static final String API_LOGIN = "/login";

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private final MessageSrv msgSrv;
    private final HttpClientWrap httpClient;

    private Future<Void> future = CompletableFuture.allOf();

    public LoginSrvImpl(
            ObservableMap<AppStateType, AppStateData> appState,
            Executor exec,
            MessageSrv msgSrv,
            HttpClientWrap httpClient) {
        this.appState = appState;
        this.exec = exec;
        this.msgSrv = msgSrv;
        this.httpClient = httpClient;
    }

    @Override
    public void loginAsync(String username, String password) {

        logger.debug("user: '{}'", username);

        // Avoid multiply invocation
        if (!future.isDone()) {
            this.msgSrv.news("Операция выполняется");
            return;
        }
        // Trying to login async
        future = CompletableFuture.supplyAsync(() -> {
            this.msgSrv.news("Подключение: " + username);
            return this.login(username, password);
        }, exec).thenAccept(usr -> {
            this.msgSrv.news("Выполнен " + (usr.isLogged() ? String.format("вход: %s", usr.getUsername()) : "выход"));
            logger.debug("logged in: {}", username);
            appState.put(AppStateType.USER_DETAILS, AppStateData.<UserDetails>builder().value(usr).build());
        }).exceptionally(ex -> {
            this.msgSrv.news("Подключение не удалось, ошибка: " + ex.getLocalizedMessage());
            logger.error(ex);
            return null;
        } );
    }

    @Override
    public void logout() {
        appState.put(AppStateType.USER_DETAILS, AppStateData.<UserDetails>builder().value(UserDetails.NOT_SIGNED_USER).build());
        this.msgSrv.news("Выполнен выход");
    }

    //region PRIVATE

    UserDetails login(String username, String password) {

        // Create multipart POST request
        final String api = ((AppStateData<AppSettings>)appState.get(AppStateType.SETTINGS)).getValue().getBackendAPI()
                + API_LOGIN;
        final HttpPost httpPost = new HttpPost(api);
        final HttpEntity multipart = MultipartEntityBuilder.create()
                .addTextBody("username", username)
                .addTextBody("password", password)
                .build();
        httpPost.setEntity(multipart);

        String resp = this.httpClient.postDataRequest(httpPost);

        String token = this.removeBearer(resp);
        return !Strings.isBlank(token)
                ? new UserDetails(username, password, token)
                : UserDetails.NOT_SIGNED_USER;
    }

    String removeBearer(String source) {
        return source.contains("Bearer ") ? source.substring("Bearer ".length()) : source;
    }

    //endregion
}
