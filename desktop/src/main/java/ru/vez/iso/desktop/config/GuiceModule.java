package ru.vez.iso.desktop.config;

import com.google.inject.AbstractModule;
import ru.vez.iso.desktop.login.LoginSrv;
import ru.vez.iso.desktop.login.LoginSrvImpl;

public class GuiceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(LoginSrv.class).to(LoginSrvImpl.class);
    }
}
