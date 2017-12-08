package ex3;

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

import static ex3.Message.NOTIFY;
import static ex3.Message.PROPOSE;

public class Byzantine extends UnicastRemoteObject implements Byzantine_RMI{

    String REGISTRY_IP;
    Registry registry;
    boolean registryPresent = false;
    int id;
    String name;
    int r;
    int v;
    boolean decided;
    boolean traitor=false;
    int state;
    int numTraitors;
    int totalProcesses;
    List<int[]> nMessages = new ArrayList<>();
    List<int[]> pMessages = new ArrayList<>();

    final static int WAIT_FOR_N_MESSAGES = 0;
    final static int WAIT_FOR_P_MESSAGES = 1;
    final static int DECIDED = 2;

    int[][] log = new int[20][5];
    int logCounter=0;

    public Byzantine(int id, int f, int n,boolean traitor) throws RemoteException, AlreadyBoundException, NotBoundException  {
        super();
        this.id=id;
        name = "rmi://localhost:1099/main.Byzantine" + id;
        r = 1;
        decided = false;
        numTraitors = f;
        totalProcesses = n;
        this.traitor=traitor;
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
            // do not send message to logger
            if (!name.matches(".*Logger") && name.matches("main\\.Byzantine.*")){
                Byzantine_RMI remoteObject = (Byzantine_RMI) registry.lookup(name);
                remoteObject.send(m);
            }
        }
        System.out.println("Process " + id + " sent message: " + m.toString());
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

    public synchronized void send(Message m){
        try {
            updateLog(1, m.state, m.round, m.value, m.senderId);
            System.out.println("Process " + id + " received " + m.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        while(nMessages.size() <= m.round) {
            nMessages.add(nMessages.size(), new int[3]);
        }
        while(pMessages.size() <= m.round) {
            pMessages.add(pMessages.size(), new int[3]);
        }

        // save received message
        if(m.state == NOTIFY) {
            nMessages.get(m.round)[m.value]++;
        } else if(m.state == PROPOSE) {
            pMessages.get(m.round)[m.value]++;
        }

        if(state == WAIT_FOR_N_MESSAGES) {
            // await n-f messages of the form (N;r,*)
            if(nMessages.get(r)[0] + nMessages.get(r)[1] + nMessages.get(r)[2] >= totalProcesses-numTraitors) {
                System.out.println("Process: " +id+ " Has received all needed Notify Messages for round "+r);
                try {
                    // received (n+f)/2 messages (N;r,w) with w=0
                    if (nMessages.get(r)[0] > (totalProcesses + numTraitors) / 2) {
                        checkTraitorAndSend(0,PROPOSE);
                    }
                    // received (n+f)/2 messages (N;r,w) with w=1
                    else if (nMessages.get(r)[1] > (totalProcesses + numTraitors) / 2) {
                        checkTraitorAndSend(1,PROPOSE);
                    }
                    // otherwise choose a random value
                    else {
                        checkTraitorAndSend(2,PROPOSE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if(decided) {
                    state = DECIDED;
                } else {
                    if(state != DECIDED) {
                        state = WAIT_FOR_P_MESSAGES;
                    }
                }
            }
        }

        else if(state == WAIT_FOR_P_MESSAGES) {
            // await n-f messages of form (P;r,*)
            if(pMessages.get(r)[0] + pMessages.get(r)[1] + pMessages.get(r)[2] >= totalProcesses-numTraitors) {
                System.out.println("Process: " +id+ " Received enough Propose messages for round " +r);
                try {
                    // if more than f messages received of from (P;r,w=0) adopt value 0
                    if(pMessages.get(r)[0] > numTraitors) {
                        v = 0;
                        if(pMessages.get(r)[0] > numTraitors*3) {
                            System.out.println("Process: "+id+ " Has decided "+v);
                            decided = true;
                        }
                    }
                    // if more than f messages received of from (P;r,w=1) adopt value 1
                    else if(pMessages.get(r)[1] > numTraitors) {
                        v = 1;
                        if(pMessages.get(r)[1] > numTraitors*3) {
                            System.out.println("Process: "+id+ " Has decided "+v);
                            decided = true;
                        }
                    }
                    // if not more than f messages of either form received
                    else {
                        v = (new Random()).nextInt(2);
                    }
                    // increase the round number
                    r++;
                    while(nMessages.size() <= r) {
                        nMessages.add(new int[3]);
                    }
                    while(pMessages.size() <= r) {
                        pMessages.add(new int[3]);
                    }

                    checkTraitorAndSend(v,NOTIFY);

                    if(state != DECIDED) {
                        state = WAIT_FOR_N_MESSAGES;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void firstBroadcast() throws RemoteException, NotBoundException, AlreadyBoundException {
        Runnable run = new Broadcast(this, new Message(id, r, v, NOTIFY));
        new Thread(run).start();
    }

    public void buildMessageSend(Message m) throws InterruptedException {
        Runnable run = new Broadcast(this, m);
        new Thread(run).start();
    }

    public void checkTraitorAndSend(int messageValue,int messageState) throws InterruptedException {
        if(traitor){
            int behaviour = new Random().nextInt(5);
            switch (behaviour){
                //behaviour 0 - Send Normal Message
                case 0:
                    buildMessageSend(new Message(id, r, messageValue, messageState));
                    break;
                //behaviour 1 - Send Random Message Value
                case 1:
                    buildMessageSend(new Message(id, r, new Random().nextInt(3), messageState));
                    break;
                //behaviour 2 - Send Flipped State
                case 2:
                    buildMessageSend(new Message(id, r, messageValue, 1-messageState));
                    break;
                //behaviour 3 - Send Flipped State and Random Message Message
                case 3:
                    buildMessageSend(new Message(id, r, new Random().nextInt(3), 1-messageState));
                    break;
                //behaviour 4 - Dont send Anything
                case 4:
                    break;

            }

        }else {
            buildMessageSend(new Message(id, r, messageValue, messageState));
        }

    }
}
