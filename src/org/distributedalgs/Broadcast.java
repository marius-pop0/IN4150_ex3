package org.distributedalgs;

import java.util.concurrent.ThreadLocalRandom;

import static org.distributedalgs.Byzantine.DECIDED;

public class Broadcast implements Runnable {
    Byzantine localObject;
    Message m;

    public Broadcast(Byzantine localObject, Message m){
        this.localObject = localObject;
        this.m = m;
    }

    @Override
    public void run(){
        int randomNum = ThreadLocalRandom.current().nextInt(0, 3000);
        try {
            if (localObject.state != DECIDED) {
                Thread.sleep(randomNum);
                localObject.broadcast(m);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
