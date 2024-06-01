package org.example.wmashine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    public static Stage mainStage;
    public static Pane mainPane = new Pane();
    public static WMachine machine = new WMachine();

    @Override
    public void start(Stage stage) {
        mainStage = stage;

        machine.init();
        mainPane.getChildren().add(machine.getGroup());

        Scene scene = new Scene(mainPane);
        stage.setTitle("WMachine!");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}