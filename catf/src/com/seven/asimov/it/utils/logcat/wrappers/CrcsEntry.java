package com.seven.asimov.it.utils.logcat.wrappers;

public abstract class CrcsEntry extends LogEntryWrapper {

    private long id;
    private long logId;
    private long timestamp;

    long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getLogId() {
        return logId;
    }

    public void setLogId(long logId) {
        this.logId = logId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


}
