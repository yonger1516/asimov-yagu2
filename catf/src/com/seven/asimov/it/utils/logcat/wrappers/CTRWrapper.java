package com.seven.asimov.it.utils.logcat.wrappers;

public class CTRWrapper extends LogEntryWrapper {
    String csmId;
    String resolverId;

    public String getCsmId() {
        return csmId;
    }

    public void setCsmId(String csmId) {
        this.csmId = csmId;
    }

    public String getResolverId() {
        return resolverId;
    }

    public void setResolverId(String resolverId) {
        this.resolverId = resolverId;
    }
}
