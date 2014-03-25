package com.seven.asimov.it.utils.logcat.wrappers;

public class CrcsStorageFilled_95_Wrapper extends LogEntryWrapper {
    private static final String TAG = CrcsStorageFilled_95_Wrapper.class.getSimpleName();
    private int percent;
    private int recordsDeleted;

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public int getRecordsDeleted() {
        return recordsDeleted;
    }

    public void setRecordsDeleted(int recordsDeleted) {
        this.recordsDeleted = recordsDeleted;
    }

    @Override
    protected String getTAG() {return TAG;};

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(" percent is ").append(getPercent()).append("; recordsDeleted is ").append(getRecordsDeleted()).toString();
    }

}