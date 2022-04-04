package ru.vez.iso.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Callback;
import ru.vez.iso.desktop.disks.DiskCtl;
import ru.vez.iso.desktop.disks.DisksSrvImpl;
import ru.vez.iso.desktop.login.LoginCtl;
import ru.vez.iso.desktop.login.LoginSrvImpl;
import ru.vez.iso.desktop.main.MainCtl;
import ru.vez.iso.desktop.main.MainSrvImpl;
import ru.vez.iso.desktop.nav.NavigationCtl;
import ru.vez.iso.desktop.nav.NavigationSrvImpl;
import ru.vez.iso.desktop.settings.SettingsCtl;
import ru.vez.iso.desktop.settings.SettingsSrvImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * DI implemented for all Views/Services
 * */
public class DesktopApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Map<ViewType, Parent> viewCache = buildViewCache();
        Parent navigation = buildView(ViewType.NAVIGATION, t->new NavigationCtl(new NavigationSrvImpl(), viewCache));

        stage.setTitle("ISO Writer App");
        stage.setScene(new Scene(navigation));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    //region Private

    private Map<ViewType, Parent> buildViewCache() throws IOException {

        Map<ViewType, Parent> viewCache = new HashMap<>();

        viewCache.put(ViewType.LOGIN, buildView( ViewType.LOGIN, t -> new LoginCtl(new LoginSrvImpl())));
        viewCache.put(ViewType.MAIN, buildView( ViewType.MAIN, t -> new MainCtl(new MainSrvImpl())));
        viewCache.put(ViewType.DISK, buildView( ViewType.DISK, t -> new DiskCtl(new DisksSrvImpl())));
        viewCache.put(ViewType.SETTINGS, buildView( ViewType.SETTINGS, t -> new SettingsCtl(new SettingsSrvImpl())));

        return viewCache;
    }

    private Parent buildView(ViewType view, Callback<Class<?>, Object> controllerFactory) throws IOException {

        System.out.println("buildView from file: " + view.getFileName());
        FXMLLoader loader = new FXMLLoader(getClass().getResource(view.getFileName()));
        loader.setControllerFactory(controllerFactory);
        Parent parent = loader.load();
        return parent;
    }

    //endregion
}
