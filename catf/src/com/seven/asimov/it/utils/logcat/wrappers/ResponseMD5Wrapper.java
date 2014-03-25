package com.seven.asimov.it.utils.logcat.wrappers;

public class ResponseMD5Wrapper extends LogEntryWrapper  {
    private long timestamp;
    private String md5;

    public ResponseMD5Wrapper(long timestamp, String md5) {
        this.timestamp = timestamp;
        this.md5 = md5;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public String getMD5() {
        return md5;
    }

    @Override
    public String toString() {
        return "ResponseMD5Wrapper{" +
                "timestamp=" + timestamp +
                ", md5='" + md5 + '\'' +
                '}';
    }
}