package com.seven.asimov.it.utils.logcat.wrappers;

public class NetworktypeConditionWrapper extends LogEntryWrapper{

    String networkType;

    public NetworktypeConditionWrapper () {

    }

    public String getNetworkType() {
        return this.networkType;
    }

    public void setNetworkType (String networkType) {
        this.networkType = networkType;
    }

    @Override
    public String toString() {
        return "NetworktypeConditionWrapper{timestamp= " + getTimestamp() +
                " type= " + networkType + '}';
    }
}
