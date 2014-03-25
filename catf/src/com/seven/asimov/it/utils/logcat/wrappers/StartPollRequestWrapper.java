package com.seven.asimov.it.utils.logcat.wrappers;

public class StartPollRequestWrapper extends LogEntryWrapper{

    private static final String TAG = StartPollRequestWrapper.class.getSimpleName();
    private String logLevel;

    public StartPollRequestWrapper() {
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(", logLevel=").append(logLevel).toString();
    }
}
