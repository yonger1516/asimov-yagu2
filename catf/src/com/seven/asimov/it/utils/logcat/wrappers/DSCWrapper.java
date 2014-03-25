package com.seven.asimov.it.utils.logcat.wrappers;


public class DSCWrapper extends LogEntryWrapper{
    private int sec;
    private int usec;
    private int dispID;
    private int action;
    private int reason;

    public int getSec() {
        return sec;
    }

    public void setSec(int sec) {
        this.sec = sec;
    }

    public int getUsec() {
        return usec;
    }

    public void setUsec(int usec) {
        this.usec = usec;
    }

    public int getDispID() {
        return dispID;
    }

    public void setDispID(int dispID) {
        this.dispID = dispID;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }
}
