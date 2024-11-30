/*package net.RMI.Client;

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
            service = (RemoteConnect6_gameServer) registry.lookup("RemoteConnect6_gameServer");
            clientId = UUID.randomUUID().toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setGameInfo(SerializableCommand command, int canvas_size, int columnCount) {
        try {
            service.gameInfo(command, canvas_size, columnCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String notifyButtonClicked() throws RemoteException {
        return service.notifyButtonClicked(clientId, this);
    }

    @Override
    public void startGame() throws RemoteException {
        Platform.runLater(() -> controller.updateGameStartedUI());
    }

    @Override
    public void receiveGameInfo(SerializableCommand command, int move, int winner) throws RemoteException {
        Platform.runLater(() -> {
            controller.updateMoveUI(command);
            if (winner == -1 && move != -1)
                controller.ProcessMove(move);
            else
                controller.udpateAfterReset(winner, move);
        });
    }

    @Override

    @Override
    public void resetClientsGame(SerializableCommand command, int color) throws RemoteException {
        Platform.runLater(() -> {
            controller.udpateAfterReset(color, move);
        });
    }

    @Override
    public void rejectRequest() throws RemoteException {
        controller.RejectRequest();
    }

    public int getColor() {
        return color;
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

    public void resetButtonClicked(int color) throws RemoteException {
        service.resetGame(color, this);
    }

    public void closeConnection() throws RemoteException {
        service.kickClient(this, color);
    }
}*/