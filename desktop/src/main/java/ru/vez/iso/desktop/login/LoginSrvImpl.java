package ru.vez.iso.desktop.login;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import ru.vez.iso.desktop.shared.HttpClientWrap;
import ru.vez.iso.desktop.shared.MessageSrv;
import ru.vez.iso.desktop.shared.UserDetails;
import ru.vez.iso.desktop.state.ApplicationState;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class LoginSrvImpl implements LoginSrv {

    private static final Logger logger = LogManager.getLogger();
    private static final String API_LOGIN = "/login";

    private final ApplicationState state;
    private final Executor exec;
    private final MessageSrv msgSrv;
    private final HttpClientWrap httpClient;

    private Future<Void> future = CompletableFuture.allOf();

    public LoginSrvImpl(
            ApplicationState state,
            Executor exec,
            MessageSrv msgSrv,
            HttpClientWrap httpClient
    ) {
        this.state = state;
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
        }, exec).thenAccept(userDetails -> {
            this.state.setUserDetails(userDetails);
            this.msgSrv.news(
                    "Выполнен " + (userDetails.isLogged() ? String.format("вход: %s", userDetails.getUsername()) : "выход")
            );
            logger.debug("logged in: {}", username);
        }).exceptionally(ex -> {
            this.msgSrv.news("Подключение не удалось: " + ex.getCause().getLocalizedMessage());
            logger.error(ex);
            return null;
        } );
    }

    @Override
    public void logout() {
        this.state.setUserDetails(UserDetails.NOT_SIGNED_USER);
        this.msgSrv.news("Выполнен выход");
    }

    //region PRIVATE

    UserDetails login(String username, String password) {

        // Create multipart POST request
        final String api = this.state.getSettings().getBackendAPI() + API_LOGIN;
        final HttpPost httpPost = new HttpPost(api);
        final HttpEntity multipart = MultipartEntityBuilder.create()
                .addTextBody("username", username)
                .addTextBody("password", password)
                .build();
        httpPost.setEntity(multipart);

        String token = this.httpClient.postDataRequest(httpPost);

        // String token = this.removeBearer(resp);
        return !Strings.isBlank(token)
                ? new UserDetails(username, password, token)
                : UserDetails.NOT_SIGNED_USER;
    }

    String removeBearer(String source) {
        return source.contains("Bearer ") ? source.substring("Bearer ".length()) : source;
    }

    //endregion
}
