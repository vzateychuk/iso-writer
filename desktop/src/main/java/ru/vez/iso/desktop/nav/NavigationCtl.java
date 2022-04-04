package ru.vez.iso.desktop.nav;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Navigation controller. Load all other views/and shows application status
 */
public class NavigationCtl {

    private final AppService appService;

    public NavigationCtl(AppService appService) {
        this.appService = appService;
    }

    //region Controls

    private final Map<View, Parent> viewCache = new HashMap<>();
    @FXML private BorderPane navigationView;

    private View currView = View.WELCOME;

    public void onShowLogin(ActionEvent ev) {
        loadView(View.LOGIN);
    }
    public void onShowMain(ActionEvent ev) {
        loadView(View.MAIN);
    }
    public void onShowSettings(ActionEvent ev) {
        loadView(View.SETTINGS);
    }
    public void onShowDisks(ActionEvent ev) {
        loadView(View.DISK);
    }

    //endregion
    //region Private

    private void loadView(View view) {

        if (currView == view) {
            return;
        }

        try {
            Parent root;
            if (view.isCacheable() && viewCache.containsKey(view)) {

                System.out.println("Load from Cache: " + view.name());
                root = viewCache.get(view);

            } else {
                System.out.println("Load from File: " + view.getFileName());
                root = FXMLLoader.load(
                        getClass().getResource(view.getFileName())
                );
                viewCache.put(view, root);
            }

            navigationView.setCenter(root);
            currView = view;

        } catch (IOException e) {
            throw new RuntimeException("Unable to load: " + view.getFileName());
        }
    }

    //endregion
}
