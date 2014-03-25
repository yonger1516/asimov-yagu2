package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: amykytenko_cv
 * Date: 5/27/13
 * Time: 12:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class PolicyMGMTdataRequestWrapper extends LogEntryWrapper {
    private String z7TP;

    public String getZ7TP() {
        return z7TP;
    }

    public void setZ7TP(String z7TP) {
        this.z7TP = z7TP;
    }

    @Override
    public String toString() {
        return "PolicyMGMTdataRequestWrapper{" +
                "z7TP='" + z7TP + "\' " + "timestamp='" +
                getTimestamp() + '\'' +
                '}';
    }
}
