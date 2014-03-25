package com.seven.asimov.it.utils.logcat.wrappers;

public class HttpsFCLWrapper extends LogEntryWrapper{
    String fc;
    String dst;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDst() {
        return dst;
    }

    public void setDst(String dst) {
        this.dst = dst;
    }

    public String getFc() {
        return fc;
    }

    public void setFc(String fc) {
        this.fc = fc;
    }

    String uid;
}
