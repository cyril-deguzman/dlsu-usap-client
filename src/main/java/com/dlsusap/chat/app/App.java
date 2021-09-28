package com.dlsusap.chat.app;

import com.dlsusap.chat.app.controllers.Controller;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        System.out.println(getClass().getResource("dlsusap_client.fxml"));
        FXMLLoader clientLoader = new FXMLLoader(getClass().getResource("dlsusap_client.fxml"));
        Parent root = clientLoader.load();
        Controller clientController = clientLoader.getController();
        clientController.setStage(primaryStage);

        primaryStage.setTitle("DLSUsap");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
