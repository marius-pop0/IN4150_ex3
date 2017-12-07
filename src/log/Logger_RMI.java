package log;

import ex3.Byzantine_RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Logger_RMI extends Remote {
    public void sendLog(int remoteId, int[][] log) throws RemoteException;

    public void register(String name, Byzantine_RMI remoteObject) throws java.rmi.RemoteException;
}
