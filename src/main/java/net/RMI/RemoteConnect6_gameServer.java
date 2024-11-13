package net.RMI;

import net.RMI.Client.RemoteClientCallback;
import net.command.SerializableCommand;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteConnect6_gameServer extends Remote {
    String CirclesInfo(SerializableCommand command, int grid_size) throws RemoteException;
    String[] notifyButtonClicked(String clientId, RemoteClientCallback callback) throws RemoteException;
    void ResetGame(int color, RemoteClientCallback callback) throws RemoteException;
}
