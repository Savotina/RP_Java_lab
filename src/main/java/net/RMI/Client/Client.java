package net.RMI.Client;

import net.RMI.Client.RemoteClientCallback;
import net.RMI.RemoteConnect6_gameServer;
import net.command.SerializableCommand;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import javafx.application.Platform;

public class Client extends UnicastRemoteObject implements RemoteClientCallback {
    private RemoteConnect6_gameServer service;
    private String clientId;
    private int color;
    private int move;
    private ClientController controller;

    public Client(ClientController controller) throws RemoteException {
        this.controller = controller;
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 8080);

            // Получение удаленного объекта
            service = (RemoteConnect6_gameServer) registry.lookup("RemoteConnect6_gameServer");

            // Генерация уникального идентификатора клиента
            clientId = UUID.randomUUID().toString();
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public void SetCirclesInfo(SerializableCommand command, int canvas_size) {
        try {
            // Вызов удаленного метода
            String response = service.CirclesInfo(command, canvas_size);
            System.out.println(response);
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    public String[] notifyButtonClicked() throws RemoteException {
        return service.notifyButtonClicked(clientId, this);
    }

    @Override
    public void startGame() throws RemoteException {
        //move = 0;
        //System.out.println("Game started! Move variable updated to: " + move);
        // Вызов метода контроллера для обновления интерфейса пользователя
        Platform.runLater(() -> controller.updateGameStartedUI());
    }

    @Override
    public void ReceiveCirclesInfo(SerializableCommand command, int move, int winner) throws RemoteException {
        //System.out.println("Received move: x=" + command.x + ", y=" + command.y + ", color=" + command.color);
        // Вызов метода контроллера для обновления интерфейса пользователя
        Platform.runLater(() -> {
            controller.updateMoveUI(command);
            if (winner == -1)
                controller.ProcessMove(move);
            else
                controller.udpateAfterReset(winner);
        });
    }

    @Override
    public void SetColor(int color) throws RemoteException {
        this.color = color;
        System.out.println("Client color set to: " + color);
        // Вызов метода контроллера для обновления интерфейса пользователя
        Platform.runLater(() -> controller.updateClientColor(color));
    }

    @Override
    public void ResetClientsGame(SerializableCommand command, int color) throws RemoteException {
        System.out.println("Reset the game, winner is " + color);
        Platform.runLater(() -> {
            controller.udpateAfterReset(color);
        });
        System.out.println("color =" + this.color);
    }

    public static void main(String[] args) {
        try {
            ClientController controller = new ClientController();
            Client client = new Client(controller);
            // Здесь вы можете инициализировать ваш GUI и передать controller в него
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getMove() {
        return move;
    }

    public void setMove(int move) {
        if (move == 1)
            this.move = 0;
        else
            this.move = 1;
    }

    public void ResetButtonClicked(int color) throws RemoteException {
        service.ResetGame(color, this);
    }
}