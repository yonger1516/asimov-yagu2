package com.seven.asimov.it.utils.logcat.wrappers;

public class CleanupReportProviderWrapper extends LogEntryWrapper {

    private static final String TAG = CleanupReportProviderWrapper.class.getSimpleName();

    @Override
    protected String getTAG() {
        return TAG;
    }
}
