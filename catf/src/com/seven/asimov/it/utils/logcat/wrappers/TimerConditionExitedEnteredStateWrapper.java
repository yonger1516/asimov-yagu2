package com.seven.asimov.it.utils.logcat.wrappers;

public class TimerConditionExitedEnteredStateWrapper extends LogEntryWrapper{

    private String scriptName;

    public TimerConditionExitedEnteredStateWrapper() {

    }

    public void setScriptName(String line) {
        this.scriptName = line;
    }

    @Override
    public String toString() {
        return "TimerConditionExitedEnteredStateWrapper{timestamp= " + getTimestamp() +
                " script= " + scriptName + '}';
    }
}
