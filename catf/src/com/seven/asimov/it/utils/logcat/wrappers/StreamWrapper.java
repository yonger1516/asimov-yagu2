package com.seven.asimov.it.utils.logcat.wrappers;


public class StreamWrapper extends LogEntryWrapper{

    private String originator;
    private String task;
    private String protocol;

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    public String getTask() {
        return this.task;
    }

    public String getOriginator() {
        return this.originator;
    }

    public String getProtocol() {
        return this.protocol;
    }
}
