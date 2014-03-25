package com.seven.asimov.it.utils.logcat.wrappers;

public class CreatedTaskSchedulerTaskWrapper extends LogEntryWrapper {

    private static final String TAG = CreatedTaskSchedulerTaskWrapper.class.getSimpleName();

    private int id;
    private int delay;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }
}
