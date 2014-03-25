package com.seven.asimov.it.utils.logcat.wrappers;


import java.util.LinkedList;

public class OctcpdWrapper extends LogEntryWrapper {
    private String port;

    public OctcpdWrapper(long timestamp, String port) {
        setTimestamp(timestamp);
        this.port = port;
    }

    public String getPort() {
        return port;
    }

    @Override
    public String toString() {
        return "OctcpdWrapper{" +
                "port: '" + port + '\'' +
                '}';
    }
}
