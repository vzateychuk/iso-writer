package ru.vez.iso.desktop.login;

import ru.vez.iso.desktop.model.UserDetails;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class LoginSrvImpl implements LoginSrv {

    private final Executor exec;

    public LoginSrvImpl(Executor exec) {
        this.exec = exec;
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
                return new UserDetails(username, password, username+"-"+password);
            });
        } else {
            return CompletableFuture.supplyAsync(() -> {
                System.out.println("loginAsync: " + Thread.currentThread().getName());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return UserDetails.NOT_SIGNED_USER;
            });
        }

    }
}
