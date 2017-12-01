package org.distributedalgs;

import java.rmi.Remote;

public interface Byzantine_RMI extends Remote {

    public void register(String name, Byzantine_RMI remoteObject) throws java.rmi.RemoteException;
}
