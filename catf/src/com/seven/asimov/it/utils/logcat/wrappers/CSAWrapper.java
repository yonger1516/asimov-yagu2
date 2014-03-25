package com.seven.asimov.it.utils.logcat.wrappers;

public class CSAWrapper extends LogEntryWrapper {

    private String htrx;

    public void setHtrx(String htrx) {
        this.htrx = htrx;
    }

    public String getHtrx() {
        return htrx;
    }

    @Override
    public String toString() {
        return "CSAWrapper{" +
                "htrx='" + htrx + '\'' +
                '}';
    }
}
