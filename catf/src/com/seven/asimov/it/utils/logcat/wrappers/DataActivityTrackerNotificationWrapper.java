package com.seven.asimov.it.utils.logcat.wrappers;

public class DataActivityTrackerNotificationWrapper extends LogEntryWrapper {
    private static final String TAG = DataActivityTrackerNotificationWrapper.class.getSimpleName();

    private String notification;

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(" notification: ").append(getNotification()).append(" timestamp: ").append(getTimestamp()).toString();
    }
}
