package com.seven.asimov.it.utils.logcat.wrappers;

public class AbortedWrapper extends LogEntryWrapper {

    private long timestamp;
    private String htrx;

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    public long getTimestamp(){
        return timestamp;
    }

    public void setHtrx(String htrx){
        this.htrx = htrx;
    }

    public String getHtrx(){
        return htrx;
    }

    @Override
    public String toString(){
        return "ABORTED HTRX [ timestamp=" + timestamp + " ,htrx=" + htrx + "]";
    }

}
