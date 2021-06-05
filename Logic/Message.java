package com.company.Logic;

import java.io.Serializable;

public class Message implements Serializable {
    private String message;
    private String sender;
    public Message(String message,String sender)
    {
        this.sender= sender;
        this.message= message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setSender(String sender) {
        this.sender = sender;
    }
    public String getMessage() {
        return message;
    }
    public String getSender() {
        return sender;
    }
}
