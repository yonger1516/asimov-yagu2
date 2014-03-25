package com.seven.asimov.it.utils.logcat.wrappers;

public class DebugDataTimeTriggerWrapper extends LogEntryWrapper {

    public DebugDataTimeTriggerWrapper(long timestamp) {
        setTimestamp(timestamp);
    }
    @Override
    public String toString() {
        return "Time trigger is active";
    }
}
