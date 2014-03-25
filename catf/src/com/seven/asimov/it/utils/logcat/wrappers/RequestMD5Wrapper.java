package com.seven.asimov.it.utils.logcat.wrappers;

public class RequestMD5Wrapper extends LogEntryWrapper {

        private String md5;

    public RequestMD5Wrapper(long timestamp, String md5) {
        setTimestamp(timestamp);
        this.md5 = md5;
    }

       public String getMD5() {
        return md5;
    }

    @Override
    public String toString() {
        return "RequestMD5Wrapper{" +
                "md5='" + md5 + '\'' +
                '}';
    }
}
