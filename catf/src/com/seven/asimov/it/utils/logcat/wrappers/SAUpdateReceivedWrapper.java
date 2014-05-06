package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: rfu
 * Date: 4/30/14
 * Time: 2:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class SAUpdateReceivedWrapper extends LogEntryWrapper {

    private static final String TAG = SAUpdateReceivedWrapper.class.getSimpleName();

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
