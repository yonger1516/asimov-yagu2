package com.seven.asimov.it.utils.logcat.wrappers;


public class ProcessingCLQWrapper extends LogEntryWrapper{
    private String DTRX;
    private String host;
    private String uid;
    private String size;

    public String getDTRX() {
        return DTRX;
    }

    public void setDTRX(String DTRX) {
        this.DTRX = DTRX;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
