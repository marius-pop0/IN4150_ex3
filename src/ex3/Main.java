package ex3;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import static java.rmi.registry.Registry.REGISTRY_PORT;

public class Main {

    public static void main(String[] args) {
        if(args.length!=6){
            System.err.println("Must Provide 5 Args. Number of Processes, total processes, number of traitors, Starting Id, Server IP, boolean start");
            System.exit(1);
        }
        int n = Integer.parseInt(args[0]);
        int totaln = Integer.parseInt(args[1]);
        int f = Integer.parseInt(args[2]);
        int startId = Integer.parseInt(args[3]);
        String serverIP = args[4];

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
                localObject = new Byzantine(startId+i, f, totaln);

                localObject.REGISTRY_IP = serverIP;
                localObject.updateRegistry(LocateRegistry.getRegistry(localObject.REGISTRY_IP));
                localObject.registry = LocateRegistry.getRegistry(localObject.REGISTRY_IP);
                System.out.println("initialized " + localObject.name + " with value " + localObject.v);
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

        // start the algorithm
        if(args[5].equals("true")){
            try {
                for (String name : localObject.registry.list()) {
                    if (name.matches("main\\.Byzantine.*")) {
                        Byzantine_RMI process = (Byzantine_RMI) localObject.registry.lookup(name);
                        process.firstBroadcast();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
