package ru.vez.iso.desktop.login;

import javafx.collections.ObservableMap;
import org.apache.http.HttpEntity;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

public class LoginSrvImpl implements LoginSrv {

    private static final Logger logger = LogManager.getLogger();

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

        String api = ((AppStateData<AppSettings>) appState.get(AppStateType.SETTINGS)).getValue().getAbddAPI();
        HttpPost httpPost = new HttpPost(api);

        HttpEntity multipart = MultipartEntityBuilder.create()
                .addTextBody("username", username)
                .addTextBody("password", password)
                .build();

        httpPost.setEntity(multipart);

        future = CompletableFuture.supplyAsync(() -> {
            logger.debug("user: '{}'", username);

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(httpPost)) {

                final HttpEntity resEntity = response.getEntity();
                if (resEntity == null) {
                    throw new RuntimeException("NULL Response received");
                }
                logger.debug("Response content length: {}", resEntity.getContentLength());
                EntityUtils.consume(resEntity);

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            return "admin".equals(username) && "admin".equals(password)
                    ? new UserDetails(username, password, username+"-"+password)
                    : UserDetails.NOT_SIGNED_USER;
        }, exec).thenAccept(usr -> {
            appState.put(AppStateType.USER_DETAILS, AppStateData.<UserDetails>builder().value(usr).build());
            String msg = "Выполнен " + (usr.isLogged() ? String.format("вход: %s", username) : "выход");
            this.msgSrv.news(msg);
        }).exceptionally(ex -> {
            logger.error(ex);
            return null;
        } );
    }

    @Override
    public void logout() {
        this.loginAsync("", "");
    }
}
