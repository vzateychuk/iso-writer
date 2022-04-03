package ru.vez.iso.desktop;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ViewSwitcher {

    private static final Map<View, Parent> viewCache = new HashMap<>();
    private static Scene scene;

    public static void setScene(Scene scene) {
        ViewSwitcher.scene = scene;
    }

    public static void switchTo(View view) {

        if (scene == null) {
            System.out.println("ViewSwitcher.switchTo: No scene");
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
                        ViewSwitcher.class.getResource(view.getFileName())
                );
                viewCache.put(view, root);
            }
            scene.setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
