package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: amykytenko_cv
 * Date: 6/4/13
 * Time: 3:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class ReceivedPolicyWrapper extends LogEntryWrapper {
    private String size;
    private String version;

    public ReceivedPolicyWrapper() {
    }

    public ReceivedPolicyWrapper(String size, String version) {
        this.size = size;
        this.version = version;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "ReceivedPolicyWrapper{timestamp= " + getTimestamp() +
                " size='" + size + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
