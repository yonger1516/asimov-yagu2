package com.seven.asimov.it.utils.logcat.wrappers;

public class TryToForceCrcsReportSendingWrapper extends LogEntryWrapper {
    private static final String TAG = TryToForceCrcsReportSendingWrapper.class.getSimpleName();

    @Override
    protected String getTAG() {
        return TAG;
    }
}