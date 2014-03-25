package com.seven.asimov.it.utils.logcat.wrappers;

public class CacheOccupiedSpaceWrapper extends LogEntryWrapper{
    private String cacheOccupiedSpace;

    public CacheOccupiedSpaceWrapper(long timestamp, String cacheOccupiedSpace) {
        setTimestamp(timestamp);
        this.cacheOccupiedSpace = cacheOccupiedSpace;
    }

    public String getCacheOccupiedSpace() {
        return cacheOccupiedSpace;
    }

    public void setCacheOccupiedSpace(String cacheOccupiedSpace) {
        this.cacheOccupiedSpace = cacheOccupiedSpace;
    }

    @Override
    public String toString() {
        return "CacheSizeWrapper{" +
                ", cacheOccupiedSpace='" + cacheOccupiedSpace + '\'' +
                '}';
    }
}
