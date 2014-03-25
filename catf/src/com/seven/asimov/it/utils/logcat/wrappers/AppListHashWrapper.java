package com.seven.asimov.it.utils.logcat.wrappers;

import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;

import java.lang.Override;
import java.lang.String;

public class AppListHashWrapper extends LogEntryWrapper {
    private String listHash;

    public String getListHash() {
        return listHash;
    }

    public void setListHash(String listHash) {
        this.listHash = listHash;
    }

    @Override
    public String toString() {
        return "AppListHashWrapper{" +
                "listHash='" + listHash + '\'' + " timestamp= " + getTimestamp()
                + '}';
    }
}
