package com.seven.asimov.it.utils.logcat.wrappers;

public class FclPostponedWrapper extends LogEntryWrapper {
    String csm;
    String fck;
    String forCsm;

    public String getForCsm() {
        return forCsm;
    }

    public void setForCsm(String forCsm) {
        this.forCsm = forCsm;
    }

    public String getFck() {
        return fck;
    }

    public void setFck(String fck) {
        this.fck = fck;
    }

    public String getCsm() {
        return csm;
    }

    public void setCsm(String csm) {
        this.csm = csm;
    }

}
