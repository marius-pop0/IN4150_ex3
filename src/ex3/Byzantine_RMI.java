package ex3;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Byzantine_RMI extends Remote {

    public void register(String name, Byzantine_RMI remoteObject) throws java.rmi.RemoteException;

    public void send(Message message) throws java.rmi.RemoteException, AlreadyBoundException, NotBoundException;

    public void firstBroadcast() throws RemoteException, NotBoundException, AlreadyBoundException;
}
