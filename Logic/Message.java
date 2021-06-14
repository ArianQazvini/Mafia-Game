package com.company.Logic;

import java.io.Serializable;

/**
 * Messages
 */
public class Message implements Serializable {
    private String message;
    private String sender;

    /**
     * Instantiates a new Message.
     *
     * @param message the message
     * @param sender  the sender
     */
    public Message(String message,String sender)
    {
        this.sender= sender;
        this.message= message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Sets sender.
     *
     * @param sender the sender
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets sender.
     *
     * @return the sender
     */
    public String getSender() {
        return sender;
    }
}
