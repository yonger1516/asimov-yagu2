package com.seven.asimov.it.utils.logcat.wrappers;

public class TimerConditionEnteredDisabledStateWrapper extends LogEntryWrapper{
    private String scriptName;

    public TimerConditionEnteredDisabledStateWrapper() {

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
