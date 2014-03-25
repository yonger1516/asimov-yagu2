package com.seven.asimov.it.utils.logcat.wrappers;

public class Dispatcher extends LogEntryWrapper {
    private long timestamp;
    private String crash = "Dispatchers didn't reconnect correctly";

    public Dispatcher(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }
}