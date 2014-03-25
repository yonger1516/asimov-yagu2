package com.seven.asimov.it.utils.logcat.wrappers;

public class NSCWrapper extends LogEntryWrapper{

    private long time;

    private int reason;

    public long getTime() {
        return time;
    }

    public long getTimestamp(){
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    @Override
    public String toString(){
        StringBuilder result = new StringBuilder("Time=");
        result.append(time);
        result.append(" Reason=");
        result.append(reason);
        return result.toString();
    }
}
