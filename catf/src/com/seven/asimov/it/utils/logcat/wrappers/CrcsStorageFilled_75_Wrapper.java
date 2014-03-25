package com.seven.asimov.it.utils.logcat.wrappers;

import com.seven.asimov.it.utils.logcat.wrappers.LogEntryWrapper;

public class CrcsStorageFilled_75_Wrapper extends LogEntryWrapper {
    private static final String TAG = CrcsStorageFilled_75_Wrapper.class.getSimpleName();
    private int percent;
    private int recordCount;

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    @Override
    protected String getTAG() {return TAG;};

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(" percent is ").append(getPercent()).append("; recordCount is ").append(getRecordCount()).toString();
    }

}