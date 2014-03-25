package com.seven.asimov.it.utils.logcat.wrappers;

public class RadioTimerWrapper extends LogEntryWrapper {
    private String state;
    private String timerScheduled;
    private String alreadyActive;

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTimerScheduled() {
        return timerScheduled;
    }

    public void setTimerScheduled(String timerScheduled) {
        this.timerScheduled = timerScheduled;
    }

    public String getAlreadyActive() {
        return alreadyActive;
    }

    public void setAlreadyActive(String alreadyActive) {
        this.alreadyActive = alreadyActive;
    }

}
