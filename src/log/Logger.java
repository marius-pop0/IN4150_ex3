package log;

import ex3.Byzantine_RMI;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import static java.rmi.registry.Registry.REGISTRY_PORT;

public class Logger extends UnicastRemoteObject implements Logger_RMI {
    private Registry registry;
    String name =  "rmi://localhost:1099/Logger";

    protected Logger() throws RemoteException {
    }


    /**
     * Remote objects can call this method to send their latest logs to the Logger.
     * Columns of log depict:
     *  - First column: indicator for kind of log | 0: broadcast | 1: received message | 2: decided
     *  - Second column: state of message, either N or P
     *  - Third column: round number
     *  - Fourth column: value
     *  - Fifth column: senderID
     *
     *  For decided, put 'random' values in the columns that will normally hold a message
     * @param remoteId - id of the remote object
     * @param log - Log of last few actions of the remote object
     * @throws RemoteException
     */
    @Override
    public void sendLog(int remoteId, int[][] log) throws RemoteException {
        if(log[0].length != 5) {
            return;
        }

        PrintWriter out;
        try {
            out = new PrintWriter(new FileOutputStream("log_process_" + remoteId + ".txt", true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("Log for process with id: " + remoteId);
        for(int i=0; i<log.length; i++) {
            int state = log[i][0];
            String message = messageToString(log[i][1], log[i][2], log[i][3]);
            switch(state) {
                case 0: out.println("Broadcast message " + message);
                    System.out.println("Broadcast message " + message);
                    break;
                case 1: out.println("Received message " + message + " from process " + log[i][4]);
                    System.out.println("Received message " + message + " from process " + log[i][4]);
                    break;
                case 2: out.println("Decided on value: " + log[i][3]);
                    break;
                default: out.println("Wrong format");
                    System.out.println("Wrong format");
                    break;
            }
        }
        System.out.println();
        out.close();
    }

    /**
     * Method for nicely printing out the message
     * @param state
     * @param round
     * @param value
     */
    public String messageToString(int state, int round, int value) {
        String res = "(";
        if(state == 0) {
            res += "N,";
        } else if(state == 1) {
            res += "P,";
        }
        res += round + ",";
        res += value;

        return res;
    }

    @Override
    public void register(String name, Byzantine_RMI remoteObject){
        try {
            registry.rebind(name, remoteObject);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Logger logger = null;

        // create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            logger = new Logger();
            java.rmi.registry.LocateRegistry.createRegistry(REGISTRY_PORT);
            logger.registry = LocateRegistry.getRegistry();
            LocateRegistry.getRegistry().rebind(logger.name,logger);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("initialized logger with name: " + logger.name);
    }
}
