package com.seven.asimov.it.utils.logcat.wrappers;

public class MsisdnValidationHttpRequestWrapper extends LogEntryWrapper {
    private static final String TAG = MsisdnValidationHttpRequestWrapper.class.getSimpleName();

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(" url: ").append(getUrl()).toString();
    }
}
