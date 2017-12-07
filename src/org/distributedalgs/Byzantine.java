package org.distributedalgs;

import log.Logger_RMI;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.distributedalgs.Message.NOTIFY;
import static org.distributedalgs.Message.PROPOSE;

public class Byzantine extends UnicastRemoteObject implements Byzantine_RMI{

    String REGISTRY_IP;
    Registry registry;
    boolean registryPresent = false;
    int id;
    String name;
    int r;
    int v;
    boolean decided;
    int state;
    int numTraitors;
    int totalProcesses;
    List<int[]> nMessages = new ArrayList<int[]>();
    List<int[]> pMessages = new ArrayList<int[]>();

    final static int WAIT_FOR_N_MESSAGES = 0;
    final static int WAIT_FOR_P_MESSAGES = 1;
    final static int DECIDED = 2;

    int[][] log = new int[20][5];
    int logCounter=0;

    public Byzantine(int id, int f, int n) throws RemoteException, AlreadyBoundException, NotBoundException  {
        super();
        this.id=id;
        name = "rmi://localhost:1099/main.Byzantine" + id;
        r = 1;
        decided = false;
        numTraitors = f;
        totalProcesses = n;
        v = (new Random()).nextInt(2);
    }

    /**
     * Remote objects can call this method to register on this RMI registry.
     * @param name - the url of the remote object
     * @param remoteObject - the actual remote object
     */

    public void register(String name, Byzantine_RMI remoteObject) {
        try {
            java.rmi.Naming.rebind(name,remoteObject);
            /*System.out.println("registry contents: ");
            for(String n : LocateRegistry.getRegistry().list()) {
                System.out.println(n);
            }*/
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
            if (!name.matches(".*Byzantine" + this.id) && !name.matches(".*Logger")){
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
            System.out.println("Process " + id + " received " + m.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // save received message
        if(m.state == NOTIFY) {
            while(nMessages.size() <= m.round) {
                nMessages.add(nMessages.size(), new int[2]);
            }
            nMessages.get(m.round)[m.value]++;
        } else if(m.state == PROPOSE) {
            while(pMessages.size() <= m.round) {
                pMessages.add(pMessages.size(), new int[2]);
            }
            pMessages.get(m.round)[m.value]++;
        }

        if(state == WAIT_FOR_N_MESSAGES) {
            // await n-f messages of the form (N;r,*)
            if(nMessages.get(r)[0] + nMessages.get(r)[1] > totalProcesses-numTraitors) {
                try {
                    // received (n+f)/2 messages (N;r,w) with w=0
                    if (nMessages.get(r)[0] > (totalProcesses + numTraitors) / 2) {
                        broadcast(new Message(this.id, r, 0, 1));
                    }
                    // received (n+f)/2 messages (N;r,w) with w=1
                    else if (nMessages.get(r)[1] > (totalProcesses + numTraitors) / 2) {
                        broadcast(new Message(this.id, r, 1, 1));
                    }
                    // otherwise choose a random value
                    else {
                        broadcast(new Message(this.id, r, (new Random()).nextInt(2), 1));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(decided) {
                    state = DECIDED;
                } else {
                    state = WAIT_FOR_P_MESSAGES;
                }
            }
        }

        else if(m.state == WAIT_FOR_P_MESSAGES) {
            // await n-f messages of form (P;r,*)
            if(pMessages.get(r)[0] + pMessages.get(r)[1] > totalProcesses-numTraitors) {
                try {
                    // if more than f messages received of from (P;r,w=0) adopt value 0
                    if(pMessages.get(r)[0] > numTraitors) {
                        v = 0;
                        if(pMessages.get(r)[0] > numTraitors*3) {
                            decided = true;
                        }
                    }
                    // if more than f messages received of from (P;r,w=1) adopt value 1
                    else if(pMessages.get(r)[1] > numTraitors) {
                        v = 1;
                        if(pMessages.get(r)[1] > numTraitors*3) {
                            decided = true;
                        }
                    }
                    // if not more than f messages of either form received
                    else {
                        v = (new Random()).nextInt(2);
                    }
                    // increase the round number
                    r++;
                    broadcast(new Message(id, r, v, NOTIFY));
                    state = WAIT_FOR_N_MESSAGES;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void firstBroadcast() throws RemoteException, NotBoundException, AlreadyBoundException {
        broadcast(new Message(id, r, v, NOTIFY));
    }


}
