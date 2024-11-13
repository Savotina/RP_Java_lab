package net.RMI.Client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Создание экземпляра Client
        //Client client = new Client();

        // Загрузка FXML и установка контроллера
        FXMLLoader fxmlLoader = new FXMLLoader(ClientApplication.class.getResource("hello-view.fxml"));
        //ClientController controller = new ClientController(client);
        //fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load(), 700.0, 700.0);
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();

        // Передача экземпляра в контроллер
        //controller.initialize();
    }

    public static void main(String[] args) {
        launch();
    }
}
