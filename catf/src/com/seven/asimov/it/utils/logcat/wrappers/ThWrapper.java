package com.seven.asimov.it.utils.logcat.wrappers;

public class ThWrapper extends LogEntryWrapper {
    private static final String TAG = ThWrapper.class.getSimpleName();
    private String logLevel;
    private int id;
    private byte type;

    public ThWrapper() {
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public long getType() {
        return type;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return  new StringBuilder().append("ThWrapper{")
                .append("logLevel=").append(logLevel)
                .append(", id=").append(id)
                .append(", type=").append(type)
                .toString();
    }
}
