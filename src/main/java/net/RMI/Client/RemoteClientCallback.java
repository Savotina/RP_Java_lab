package net.RMI.Client;

import net.command.SerializableCommand;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteClientCallback extends Remote {
    void startGame() throws RemoteException;
    void receiveGameInfo(SerializableCommand command, int move, int winner) throws RemoteException;
    void setColor(int color) throws RemoteException; // Добавленный метод
    void resetClientsGame(SerializableCommand command, int color) throws RemoteException;
    void rejectRequest() throws RemoteException;
}
