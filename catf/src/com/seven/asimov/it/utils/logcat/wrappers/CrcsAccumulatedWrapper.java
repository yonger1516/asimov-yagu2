package com.seven.asimov.it.utils.logcat.wrappers;

public class CrcsAccumulatedWrapper extends LogEntryWrapper {
    private static final String TAG = CrcsAccumulatedWrapper.class.getSimpleName();

    private int count;

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
