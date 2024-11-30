package net.RMI;

import net.RMI.Client.RemoteClientCallback;
import net.command.SerializableCommand;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteConnect6_gameServer extends Remote {
    void gameInfo(SerializableCommand command, int grid_size, int columnCount) throws RemoteException;
    String notifyButtonClicked(String clientId, RemoteClientCallback callback) throws RemoteException;
    void resetGame(int color, RemoteClientCallback callback) throws RemoteException;
    void kickClient(RemoteClientCallback callback, int color) throws RemoteException;
}
