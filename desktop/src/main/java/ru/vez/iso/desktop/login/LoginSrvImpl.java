package ru.vez.iso.desktop.login;

import com.google.gson.JsonObject;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import ru.vez.iso.desktop.shared.HttpClientWrap;
import ru.vez.iso.desktop.shared.MessageSrv;
import ru.vez.iso.desktop.shared.UserDetails;
import ru.vez.iso.desktop.state.ApplicationState;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class LoginSrvImpl implements LoginSrv {

    private static final Logger logger = LogManager.getLogger();

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
            this.msgSrv.news("Операция выполняется, подождите...");
            return;
        }
        // Trying to login async
        future = CompletableFuture.runAsync(() -> {
            this.msgSrv.news("Подключение: " + username);
            UserDetails logged = this.login(username, password);
            this.state.setUserDetails(logged);
            this.msgSrv.news(
                    "Выполнен " + (logged.isLogged() ? String.format("вход: %s", logged.getUsername()) : "выход")
            );
        }, exec).exceptionally(ex -> {
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
        final String api = this.state.getSettings().getAuthAPI() + this.state.getSettings().getAuthPath();
        final HttpPost httpPost = new HttpPost(api);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("username", username));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("client_id", this.state.getSettings().getClientId()));
        params.add(new BasicNameValuePair("grant_type", this.state.getSettings().getGrantType()));
        params.add(new BasicNameValuePair("client_secret", this.state.getSettings().getClientSecret()));
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(params));
        } catch (UnsupportedEncodingException e) {
            this.msgSrv.news("Ошибка кодировки параметров Http запроса, свяжитесь с администратором.");
            logger.debug("Can't create HttpPOST request for: {}", username);
        }

        JsonObject jsonObject = this.httpClient.postDataRequest(httpPost);
        String token = jsonObject.get("access_token").getAsString();
        logger.debug("User: {} logged successfully, token: {}", username, token);

        return !Strings.isBlank(token)
                ? new UserDetails(username, password, token)
                : UserDetails.NOT_SIGNED_USER;
    }

    //endregion
}
