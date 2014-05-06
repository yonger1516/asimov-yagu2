package com.seven.asimov.it.rest.model;

public enum Status {

    UPTODATE(0),
    ADDED(1),
    REMOVED(2),
    CHANGED(3);

    private final int status;

    private Status(final int newStatus) {
        status = newStatus;
    }

    public int getValue() { return status; }
}
