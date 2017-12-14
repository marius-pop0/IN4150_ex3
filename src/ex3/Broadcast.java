package ex3;

import java.util.concurrent.ThreadLocalRandom;

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
            Thread.sleep(randomNum);
            if(localObject.traitor==true){localObject.broadcast(m);}
            else {localObject.traitorBroadcast(m);}
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
