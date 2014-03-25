package com.seven.asimov.it.utils.logcat.wrappers;

public class NaqForTrxWrapper extends LogEntryWrapper {
    private String hTrx;

    private String appUid;

    private String csmId;

    public String getCsmId() {
        return csmId;
    }

    public void setCsmId(String csmId) {
        this.csmId = csmId;
    }

    public String getAppUid() {
        return appUid;
    }

    public void setAppUid(String appUid) {
        this.appUid = appUid;
    }

    public String gethTrx() {
        return hTrx;
    }

    public void sethTrx(String hTrx) {
        this.hTrx = hTrx;
    }
}
