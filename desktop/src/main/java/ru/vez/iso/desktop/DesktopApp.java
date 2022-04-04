package ru.vez.iso.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ru.vez.iso.desktop.repo.DerbyDAOImpl;

import java.io.IOException;

public class DesktopApp extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("navigation.fxml"));
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("ISO Writer App");
        stage.show();
    }

    public static void main(String[] args) {
        try {
            DerbyDAOImpl dao = new DerbyDAOImpl();
            dao.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        launch(args);
    }
}
