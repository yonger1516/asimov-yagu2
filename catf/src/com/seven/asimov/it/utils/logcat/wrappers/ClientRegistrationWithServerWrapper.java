package com.seven.asimov.it.utils.logcat.wrappers;

public class ClientRegistrationWithServerWrapper extends LogEntryWrapper {
    private long[] timestamps;
    private String z7tpAddress;

    public ClientRegistrationWithServerWrapper() {
        timestamps = new long[4];
    }

    public long[] getTimestamps() {
        return timestamps;
    }

    public void setTimestamps(long[] timestamps) {
        this.timestamps = timestamps;
    }

     public String getZ7tpAddress() {
        return z7tpAddress;
    }

    public void setZ7tpAddress(String z7tpAddress) {
        this.z7tpAddress = z7tpAddress;
    }

    @Override
    public String toString() {
        return "ClientRegistrationWithServerWrapper{" +
                "timestamps=" + timestamps +
                ", z7tpAddress='" + z7tpAddress + '\'' +
                '}';
    }
}

