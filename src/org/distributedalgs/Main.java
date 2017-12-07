package org.distributedalgs;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import static java.rmi.registry.Registry.REGISTRY_PORT;

public class Main {

    public static void main(String[] args) {
        if(args.length!=4){
            System.err.println("Must Provide 4 Args. Number of Processes, number of traitors, Starting Id, Server IP");
            System.exit(1);
        }
        int n = Integer.parseInt(args[0]);
        int f = Integer.parseInt(args[1]);
        int startId = Integer.parseInt(args[2]);
        String serverIP = args[3];

        // create and install a security manager
        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        Byzantine localObject=null;

        try {
            java.rmi.registry.LocateRegistry.createRegistry(REGISTRY_PORT);
        } catch (Exception e) {
        }
        try {
            for(int i=0;i<n;i++) {
                localObject = new Byzantine(startId+i, f, n);

                localObject.REGISTRY_IP = serverIP;
                localObject.updateRegistry(LocateRegistry.getRegistry(localObject.REGISTRY_IP));
                localObject.registry = LocateRegistry.getRegistry(localObject.REGISTRY_IP);
                System.out.println("initialized, this objects name: " + localObject.name);
            }

        }catch (Exception e){
            e.printStackTrace();
        }



        // Print the registry for testing purposes TODO: remove
        try {
            System.out.println("registry contents: ");
            for(String name : localObject.registry.list()) {
                System.out.println(name);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        //Wait for everyone to connect in the beginning
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
