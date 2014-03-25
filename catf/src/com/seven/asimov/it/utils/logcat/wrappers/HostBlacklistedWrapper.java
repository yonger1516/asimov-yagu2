package com.seven.asimov.it.utils.logcat.wrappers;

public class HostBlacklistedWrapper extends LogEntryWrapper {
    private String till;
    public String getTill() {
        return till;
    }

    public void setTill(String till) {
        this.till = till;
    }
}
