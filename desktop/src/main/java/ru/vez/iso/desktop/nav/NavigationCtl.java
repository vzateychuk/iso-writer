package ru.vez.iso.desktop.nav;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import ru.vez.iso.desktop.ViewType;

import java.util.Map;

/**
 * Navigation controller. Load all other views/and shows application status
 */
public class NavigationCtl {

    private final NavigationSrv service;
    private final Map<ViewType, Parent> viewCache;

    public NavigationCtl(NavigationSrv service, Map<ViewType, Parent> viewCache) {
        this.service = service;
        this.viewCache = viewCache;
    }

    //region Controls

    @FXML private BorderPane navigationView;

    private ViewType currView = ViewType.WELCOME;

    public void onShowLogin(ActionEvent ev) {
        loadView(ViewType.LOGIN);
    }
    public void onShowMain(ActionEvent ev) {
        loadView(ViewType.MAIN);
    }
    public void onShowSettings(ActionEvent ev) {
        loadView(ViewType.SETTINGS);
    }
    public void onShowDisks(ActionEvent ev) {
        loadView(ViewType.DISK);
    }

    //endregion
    //region Private

    private void loadView(ViewType view) {

        if (currView == view) {
            return;
        }

        Parent root = viewCache.get(view);
        navigationView.setCenter(root);
        currView = view;
    }

    //endregion
}
