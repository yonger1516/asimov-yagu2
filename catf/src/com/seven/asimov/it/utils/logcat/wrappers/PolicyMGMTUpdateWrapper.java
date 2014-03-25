package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: amykytenko_cv
 * Date: 6/4/13
 * Time: 3:19 PM
 * To change this template use File | Settings | File Templates.
 */
public class PolicyMGMTUpdateWrapper extends LogEntryWrapper {
    private String packetID;

    public PolicyMGMTUpdateWrapper(String packetID) {
        this.packetID = packetID;
    }

    public PolicyMGMTUpdateWrapper() {
    }

    public String getPacketID() {
        return packetID;
    }

    public void setPacketID(String packetID) {
        this.packetID = packetID;
    }

    @Override
    public String toString() {
        return "PolicyMGMTUpdateWrapper{timestamp= " + getTimestamp() +
                " packetID='" + packetID + '\'' +
                '}';
    }
}
