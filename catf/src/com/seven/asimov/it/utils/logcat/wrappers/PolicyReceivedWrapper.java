package com.seven.asimov.it.utils.logcat.wrappers;

public class PolicyReceivedWrapper extends LogEntryWrapper{

    private static final String TAG = PolicyReceivedWrapper.class.getSimpleName();

    private String logLevel;
    private String name;
    private String value;

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).
                append(", logLevel=").append(logLevel).
                append(", name=").append(name).
                append(", value=").append(value).toString();
    }
}
