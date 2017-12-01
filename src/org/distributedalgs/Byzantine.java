package org.distributedalgs;

import log.Logger_RMI;

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

    int[][] log = new int[20][5];
    int logCounter=0;

    public Byzantine(int id) throws RemoteException, AlreadyBoundException, NotBoundException  {
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


    public void broadcast(Message m) throws RemoteException, NotBoundException, AlreadyBoundException {
        for (String name : registry.list()) {
            // do not send message to self or logger
            if (!name.matches(".*BirSchSteph" + this.id) && !name.matches(".*Logger")){
                Byzantine_RMI remoteObject = (Byzantine_RMI) registry.lookup(name);
                remoteObject.send(m);
            }
        }
        System.out.println("Object " + id + " sent value: " + m.value);
    }

    public synchronized void updateLog(int messageDirection, int state, int round, int value, int senderId) throws RemoteException, NotBoundException, AlreadyBoundException {
        log[logCounter][0] = messageDirection;
        log[logCounter][1] = state;
        log[logCounter][2] = round;
        log[logCounter][3] = value;
        log[logCounter][4] = senderId;

        logCounter++;
        if(logCounter > log.length-1) {
            Logger_RMI logger_rmi = (Logger_RMI) LocateRegistry.getRegistry().lookup("Logger");
            //not sure if we need to  bind here.
            logger_rmi.sendLog(this.id,log);
            logCounter=0;
        }
    }

    public void send(Message m){
        try {
            updateLog(1, m.state, m.round, m.value, m.senderId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
