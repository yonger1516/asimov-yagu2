package com.seven.asimov.it.utils.logcat.wrappers;

public class HeaderWrapper extends LogEntryWrapper {

    private static final String TAG = HeaderWrapper.class.getSimpleName();
    private String logLevel;
    private String header;
    private String value;

    public HeaderWrapper() {
    }

    public String getHeader() {
        return header;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public void setHeader(String header) {
        this.header = header;
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
        return new StringBuilder().append(TAG + "{")
                .append("header=").append(header)
                .append(", value=").append(value)
                .append(", logLevel=").append(logLevel)
                .append("}").toString();
    }
}
