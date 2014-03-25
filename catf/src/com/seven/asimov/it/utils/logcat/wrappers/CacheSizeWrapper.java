package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: User
 * Date: 30.09.13
 * Time: 21:00
 * To change this template use File | Settings | File Templates.
 */
public class CacheSizeWrapper extends LogEntryWrapper{
    private String cacheSize;

    public CacheSizeWrapper(long timestamp, String cacheSize) {
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
