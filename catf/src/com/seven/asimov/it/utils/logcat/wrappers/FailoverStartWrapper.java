package com.seven.asimov.it.utils.logcat.wrappers;

public class FailoverStartWrapper extends LogEntryWrapper {
    private String failoverType;

    public FailoverStartWrapper() {
    }

    public String getFailoverType() {
        return failoverType;
    }

    public void setFailoverType(String failoverType) {
        this.failoverType = failoverType;
    }

    @Override
    public String toString() {
        return "FailoverStartWrapper{timestamp= " + getTimestamp() +
                " type= " + failoverType + '}';
    }
}