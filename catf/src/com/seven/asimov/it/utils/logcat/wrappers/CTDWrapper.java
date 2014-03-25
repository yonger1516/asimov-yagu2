package com.seven.asimov.it.utils.logcat.wrappers;

public class CTDWrapper extends LogEntryWrapper {

    private long time;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTimestamp(){
        return time;
    }

    @Override
    public String toString(){
        return "CTD [Timestamp = " + time + " ]" ;
    }
}
