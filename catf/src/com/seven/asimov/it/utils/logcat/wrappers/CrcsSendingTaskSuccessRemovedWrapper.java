package com.seven.asimov.it.utils.logcat.wrappers;

public class CrcsSendingTaskSuccessRemovedWrapper extends LogEntryWrapper {

    private static final String TAG = CrcsSendingTaskSuccessRemovedWrapper.class.getSimpleName();

    private int idTaskRemoved;

    public int getId() {
        return idTaskRemoved;
    }

    public void setId(int idTaskRemoved) {
        this.idTaskRemoved = idTaskRemoved;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
