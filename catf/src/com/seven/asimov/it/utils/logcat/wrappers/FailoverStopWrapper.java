package com.seven.asimov.it.utils.logcat.wrappers;

public class FailoverStopWrapper extends LogEntryWrapper {
    private String failoverType;

    public FailoverStopWrapper() {
    }

    public String getFailoverType() {
        return failoverType;
    }

    public void setFailoverType(String failoverType) {
        this.failoverType = failoverType;
    }

    @Override
    public String toString() {
        return "FailoverStopWrapper{timestamp= " + getTimestamp() +
                " type= " + failoverType + '}';
    }
}