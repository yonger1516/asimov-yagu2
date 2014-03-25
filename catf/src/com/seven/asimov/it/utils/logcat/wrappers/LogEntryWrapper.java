package com.seven.asimov.it.utils.logcat.wrappers;

import java.util.Date;

public abstract class LogEntryWrapper {

    private static final String TAG = LogEntryWrapper.class.getSimpleName();

    private long timestamp;

    private int entryNumber;

    protected String getTAG() {
        return TAG;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(int entryNumber) {
        this.entryNumber = entryNumber;
    }

    /*
    public boolean startedBefore(LogEntryWrapper lew) {
        return lew.getTimestamp() > this.getTimestamp();
    }

    public boolean startedAfter(LogEntryWrapper lew) {
        return this.getTimestamp() > lew.getTimestamp();
    }
    */
    @Override
    public String toString() {
        return new StringBuilder().append(getTAG()).append(": ").append("entryNumber=").append(entryNumber).append(" timestamp=").append(getTimestamp()).append(" ").append(new Date(getTimestamp()).toString()).toString();
    }
}
