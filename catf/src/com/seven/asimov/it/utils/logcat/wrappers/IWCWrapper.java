package com.seven.asimov.it.utils.logcat.wrappers;

public class IWCWrapper extends LogEntryWrapper {
    private long timestamp;

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "IWCWrapper [ timestamp = " + timestamp + " ]";
    }
}
