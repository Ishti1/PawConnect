package com.catconnect.app;

import com.catconnect.util.Session;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class CatConnectApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        Session.init();
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
        stage.setTitle("CatConnect");
        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setScene(new Scene(root, 1100, 700));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
