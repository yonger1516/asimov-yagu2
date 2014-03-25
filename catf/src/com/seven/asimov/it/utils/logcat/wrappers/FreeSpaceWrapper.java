package com.seven.asimov.it.utils.logcat.wrappers;

public class FreeSpaceWrapper extends LogEntryWrapper {
    private String freeSpace;

    public FreeSpaceWrapper(long timestamp, String freeSpace)  {
        setTimestamp(timestamp);
        this.freeSpace = freeSpace;
    }

    public String getFreeSpace() {
        return freeSpace;
    }

    public void setFreeSpace(String freeSpace) {
        this.freeSpace = freeSpace;
    }

    @Override
    public String toString() {
        return "CacheSizeWrapper{" +
                ", cacheOccupiedSpace='" + freeSpace + '\'' +
                '}';
    }
}
