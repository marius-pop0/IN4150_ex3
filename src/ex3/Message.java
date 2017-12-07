package ex3;

import java.io.Serializable;

public class Message implements Serializable {
    int senderId;
    int round;
    int value;
    int state;

    static final int NOTIFY = 0;
    static final int PROPOSE = 1;

    /**
     * State 0=N, 1=P
     *
     */
    public Message(int senderId, int round, int value, int state) {
        this.senderId=senderId;
        this.round=round;
        this.value=value;
        this.state=state;
    }


    public String toString() {
        return "<Message: from process " + senderId + " (" + state + "," + round + "," + value + ")>";
    }
}
