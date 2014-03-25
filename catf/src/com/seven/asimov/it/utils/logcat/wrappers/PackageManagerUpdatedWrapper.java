package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: amykytenko_cv
 * Date: 5/24/13
 * Time: 8:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class PackageManagerUpdatedWrapper extends LogEntryWrapper {

    @Override
    public String toString() {
        return "PackageManagerUpdatedWrapper{" +
                "timestamp=" + getTimestamp() +
                '}';
    }
}
