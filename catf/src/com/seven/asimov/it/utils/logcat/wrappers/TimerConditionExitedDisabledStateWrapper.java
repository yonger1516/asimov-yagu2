package com.seven.asimov.it.utils.logcat.wrappers;


public class TimerConditionExitedDisabledStateWrapper extends LogEntryWrapper{

    private String scriptName;

    public TimerConditionExitedDisabledStateWrapper() {

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
