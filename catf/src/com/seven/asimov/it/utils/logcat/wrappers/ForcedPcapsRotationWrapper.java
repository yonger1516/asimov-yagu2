package com.seven.asimov.it.utils.logcat.wrappers;

public class ForcedPcapsRotationWrapper extends LogEntryWrapper{
    private String cacheSize;

    public ForcedPcapsRotationWrapper(long timestamp, String cacheSize) {
        setTimestamp(timestamp);
        this.cacheSize = cacheSize;
    }

    public String getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(String cacheSize) {
        this.cacheSize = cacheSize;
    }

    @Override
    public String toString() {
        return "CacheSizeWrapper{" +
                "cacheSize='" + cacheSize + '\'' +
                '}';
    }
}
