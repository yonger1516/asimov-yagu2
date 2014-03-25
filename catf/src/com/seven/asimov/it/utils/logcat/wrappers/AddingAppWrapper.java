package com.seven.asimov.it.utils.logcat.wrappers;

public class AddingAppWrapper extends LogEntryWrapper {
    private String uuid;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "AddingAppWrapper{" +
                "uuid= " + uuid + " timestamp= " + getTimestamp() +'}';
    }
}
