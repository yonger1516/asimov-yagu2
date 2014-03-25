package com.seven.asimov.it.utils.logcat.wrappers;

public class DispatcherCheckWrapper extends LogEntryWrapper {
    private String dispatcherName;
    private String dispatcherId;
    private String dispatcherPid;
    private String dispatcherPort;
    private String dispatcherStatus;

    public String getDispatcherName() {
        return dispatcherName;
    }

    public void setDispatcherName(String dispatcherName) {
        this.dispatcherName = dispatcherName;
    }

    public String getDispatcherId() {
        return dispatcherId;
    }

    public void setDispatcherId(String dispatcherId) {
        this.dispatcherId = dispatcherId;
    }

    public String getDispatcherPid() {
        return dispatcherPid;
    }

    public void setDispatcherPid(String dispatcherPid) {
        this.dispatcherPid = dispatcherPid;
    }

    public String getDispatcherPort() {
        return dispatcherPort;
    }

    public void setDispatcherPort(String dispatcherPort) {
        this.dispatcherPort = dispatcherPort;
    }

    public String getDispatcherStatus() {
        return dispatcherStatus;
    }

    public void setDispatcherStatus(String dispatcherStatus) {
        this.dispatcherStatus = dispatcherStatus;
    }

    @Override
    public String toString() {
        return "DispatcherCheckWrapper{" +
                "dispatcherName='" + dispatcherName + '\'' +
                ", dispatcherId='" + dispatcherId + '\'' +
                ", dispatcherPid='" + dispatcherPid + '\'' +
                ", dispatcherPort='" + dispatcherPort + '\'' +
                ", dispatcherStatus='" + dispatcherStatus + '\'' +
                '}';
    }
}
