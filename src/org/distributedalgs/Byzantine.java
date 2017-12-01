package org.distributedalgs;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Byzantine extends UnicastRemoteObject implements Byzantine_RMI{

    String REGISTRY_IP;
    Registry registry;
    boolean registryPresent = false;
    int id;
    String name;

    public Byzantine(int id) {
        super();
        this.id=id;
        name = "rmi://localhost:1099/main.Byzantine" + id;
    }

    /**
     * Remote objects can call this method to register on this RMI registry.
     * @param name - the url of the remote object
     * @param remoteObject - the actual remote object
     */

    public void register(String name, Byzantine_RMI remoteObject) {
        try {
            java.rmi.Naming.rebind(name,remoteObject);
            System.out.println("registry contents: ");
            for(String n : LocateRegistry.getRegistry().list()) {
                System.out.println(n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void updateRegistry(Registry remoteRegistry) throws RemoteException, NotBoundException, AlreadyBoundException, MalformedURLException {
        for (String name: remoteRegistry.list()) {
            if(!registryPresent) {
                if(!name.matches(".*Logger")) {
                    java.rmi.Naming.rebind(name, remoteRegistry.lookup(name));
                    ((Byzantine_RMI) remoteRegistry.lookup(name)).register(this.name, this);
                } else {
                    java.rmi.Naming.rebind(name, remoteRegistry.lookup(name));
                    ((Logger_RMI) remoteRegistry.lookup(name)).register(this.name, this);
                }
            }
        }
    }


    public void broadcast(Message m){

    }
}
