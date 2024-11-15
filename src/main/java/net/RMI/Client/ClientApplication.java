package net.RMI.Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.rmi.RemoteException;

public class ClientApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("connect6.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 700.0, 700.0);
        stage.setResizable(false);
        stage.setScene(scene);

        stage.setOnCloseRequest(event -> {
            ClientController sceneController = fxmlLoader.getController();
            try {
                sceneController.CloseConnection();
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        });

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
