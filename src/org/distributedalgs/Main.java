package org.distributedalgs;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import static java.rmi.registry.Registry.REGISTRY_PORT;

public class Main {

    public static void main(String[] args) {
        if(args.length!=3){
            System.err.println("Must Provide 3 Args. Number of Processes, Starting Id, Server IP");
            System.exit(1);
        }
        int n = Integer.parseInt(args[0]);
        int startId = Integer.parseInt(args[1]);
        String serverIP = args[2];

        // create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        Byzantine localObject=null;

        for(int i=0;i<n;i++) {
            try {
                localObject = new Byzantine(startId+i);
                java.rmi.registry.LocateRegistry.createRegistry(REGISTRY_PORT);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            try {
                localObject.REGISTRY_IP = serverIP;
                localObject.updateRegistry(LocateRegistry.getRegistry(localObject.REGISTRY_IP));
                localObject.registry = LocateRegistry.getRegistry(localObject.REGISTRY_IP);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
