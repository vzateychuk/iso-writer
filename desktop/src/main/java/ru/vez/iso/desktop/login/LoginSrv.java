package ru.vez.iso.desktop.login;

import ru.vez.iso.desktop.model.UserDetails;

import java.util.concurrent.CompletableFuture;

public interface LoginSrv {
    CompletableFuture<UserDetails> loginAsync(String username, String password);
}
