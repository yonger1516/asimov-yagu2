package com.seven.asimov.it.utils.logcat.wrappers;

public class ChainManagerConstructorWrapper extends LogEntryWrapper {
    private String chainManagerId;

    public String getChainManagerId() {
        return chainManagerId;
    }

    public void setChainManagerId(String chainManagerId) {
        this.chainManagerId = chainManagerId;
    }
}
