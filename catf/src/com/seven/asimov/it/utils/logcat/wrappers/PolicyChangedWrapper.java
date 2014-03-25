package com.seven.asimov.it.utils.logcat.wrappers;

public class PolicyChangedWrapper extends LogEntryWrapper {
    private String important;

    public PolicyChangedWrapper() {
    }

    public PolicyChangedWrapper(String important) {
        this.important = important;
    }

    public String getImportant() {
        return important;
    }

    public void setImportant(String important) {
        this.important = important;
    }

    @Override
    public String toString() {
        return "PolicyChangedWrapper{timestamp= " + getTimestamp() +
                " important='" + important + '\'' +
                '}';
    }
}
