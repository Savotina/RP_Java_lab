package net.RMI.Client;

import net.command.SerializableCommand;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteClientCallback extends Remote {
    void startGame() throws RemoteException;
    void ReceiveCirclesInfo(SerializableCommand command, int move, int winner) throws RemoteException;
    void SetColor(int color) throws RemoteException; // Добавленный метод
    void ResetClientsGame(SerializableCommand command, int color) throws RemoteException;
}
