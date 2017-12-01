package org.distributedalgs;

import java.io.Serializable;

public class Message implements Serializable {
    int senderId;
    int round;
    int value;
    int state;

    public Message(int senderId, int round, int value, int state) {
        this.senderId=senderId;
        this.round=round;
        this.value=value;
        this.state=state;
    }

}
