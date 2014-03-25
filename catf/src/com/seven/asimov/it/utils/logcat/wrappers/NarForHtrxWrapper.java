package com.seven.asimov.it.utils.logcat.wrappers;

public class NarForHtrxWrapper extends LogEntryWrapper {
    private String appUid;

    private String idleTime;

    public String getIdleTime() {
        return idleTime;
    }

    public void setIdleTime(String idleTime) {
        this.idleTime = idleTime;
    }

    public String getAppUid() {
        return appUid;
    }

    public void setAppUid(String appUid) {
        this.appUid = appUid;
    }
}
