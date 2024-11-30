package net;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("/net/connect6.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700.0, 700.0);
        stage.setResizable(false);
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            ClientController sceneController = fxmlLoader.getController();
            sceneController.CloseConnection();
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
