package ru.vez.iso.desktop.login;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.collections.ObservableMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.vez.iso.desktop.shared.*;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class LoginSrvImpl implements LoginSrv {

    private static final Logger logger = LogManager.getLogger();
    private static final String API_LOGIN = "/login";

    private final ObservableMap<AppStateType, AppStateData> appState;
    private final Executor exec;
    private final MessageSrv msgSrv;

    private Future<Void> future = CompletableFuture.allOf();

    public LoginSrvImpl(
            ObservableMap<AppStateType, AppStateData> appState,
            Executor exec,
            MessageSrv msgSrv
    ) {
        this.appState = appState;
        this.exec = exec;
        this.msgSrv = msgSrv;
        logger.debug("Created");
    }

    @Override
    public void loginAsync(String username, String password) {

        // Avoid multiply pressing
        if (!future.isDone()) {
            this.msgSrv.news("Операция выполняется");
            return;
        }

        // Create multipart POST request
        String api = ((AppStateData<AppSettings>) appState.get(AppStateType.SETTINGS)).getValue().getAbddAPI() + API_LOGIN;
        HttpPost httpPost = new HttpPost(api);

        HttpEntity multipart = MultipartEntityBuilder.create()
                .addTextBody("username", username)
                .addTextBody("password", password)
                .build();
        httpPost.setEntity(multipart);

        future = CompletableFuture.supplyAsync(() -> {
            logger.debug("user: '{}'", username);
            this.msgSrv.news("Подключение: " + username);

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(httpPost)) {
                // Create response handler
                int code = response.getStatusLine().getStatusCode();
                final HttpEntity resEntity = response.getEntity();
                if (code != HttpStatus.SC_OK || resEntity == null) {
                    throw new RuntimeException("Server sent bad response: " + code);
                }
                Reader reader = new InputStreamReader(resEntity.getContent(), StandardCharsets.UTF_8);
                JsonObject jsonObject  = new Gson().fromJson(reader, JsonObject.class);
                EntityUtils.consume(resEntity);

                String token = this.removeBearer(jsonObject.get("data").getAsString());
                UserDetails user = jsonObject.get("ok").getAsBoolean()
                        ? new UserDetails(username, password, token)
                        : UserDetails.NOT_SIGNED_USER;
                return user;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }, exec).thenAccept(usr -> {
            appState.put(AppStateType.USER_DETAILS, AppStateData.<UserDetails>builder().value(usr).build());
            this.msgSrv.news("Выполнен " + (usr.isLogged() ? String.format("вход: %s", usr.getUsername()) : "выход"));
        }).exceptionally(ex -> {
            this.msgSrv.news("Неуспешно, ошибка: " + ex.getLocalizedMessage());
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

    private String removeBearer(String source) {
        return source.contains("Bearer ") ? source.substring("Bearer ".length()) : source;
    }

    //endregion
}
