package com.seven.asimov.it.utils.logcat.wrappers;

import java.util.Date;

public class CSPWrapper extends LogEntryWrapper {

    private String htrx;

    public void setHtrx(String htrx) {
        this.htrx = htrx;
    }

    public String getHtrx() {
        return htrx;
    }

    @Override
    public String toString(){
        return "CSPWrapper{" +
                "htrx='" + htrx + '\'' +
                " ,timestamp = "  + getTimestamp() +
                " datetime" + new Date(getTimestamp()) +
                '}';
    }
}
