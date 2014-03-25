package com.seven.asimov.it.utils.logcat.wrappers;

public class TimerConditionEnteredExitedStateWrapper extends LogEntryWrapper{
    private String scriptName;

    public TimerConditionEnteredExitedStateWrapper() {

    }

    public void setScriptName(String line) {
        this.scriptName = line;
    }

    @Override
    public String toString() {
        return "TimerConditionDisabledExitedStateWrapper{timestamp= " + getTimestamp() +
                " script= " + scriptName + '}';
    }
}
