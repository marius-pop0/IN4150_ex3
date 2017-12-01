package org.distributedalgs;

import java.io.Serializable;

public class Message implements Serializable {
    int senderId;

    public Message(int senderId) {
        this.senderId=senderId;
    }

}
