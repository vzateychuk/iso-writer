package ru.vez.iso.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.vez.iso.desktop.nav.AppServiceNavImpl;
import ru.vez.iso.desktop.nav.NavigationCtl;

import java.io.IOException;

public class DesktopApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("navigation.fxml"));
        loader.setControllerFactory(
                t -> new NavigationCtl(new AppServiceNavImpl())
        );

        stage.setScene(new Scene(loader.load()));
        stage.setTitle("ISO Writer App");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
