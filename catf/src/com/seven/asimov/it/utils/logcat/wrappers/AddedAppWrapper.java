package com.seven.asimov.it.utils.logcat.wrappers;

public class AddedAppWrapper extends LogEntryWrapper {
    private String appName;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    @Override
    public String toString() {
        return "AddedAppWrapper{" +
                "appName= " + appName + " timestamp=" + getTimestamp() +
                '}';
    }
}
