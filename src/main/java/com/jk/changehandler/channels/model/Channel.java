package com.jk.changehandler.channels.model;


public interface Channel {
    /**
     *
     * @param message message sent to clients
     * @return number of clients that received the message
     */
    public Integer publish(String message);
}
