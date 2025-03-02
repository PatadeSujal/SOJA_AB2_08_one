package com.soja.farmerseller;

import com.google.firebase.Timestamp;

public class MessageManager {
    private String sender;
    private String msg;
    private Timestamp timestamp;

    public MessageManager(Timestamp timestamp, String msg,String sender ) {
        this.sender = sender;
        this.msg = msg;
        this.timestamp = timestamp;
    }

    public MessageManager() {} // Empty constructor for Firestore

    public String getSender() { return sender; }
    public String getMsg() { return msg; }
    public Timestamp getTimestamp() { return timestamp; }
}
