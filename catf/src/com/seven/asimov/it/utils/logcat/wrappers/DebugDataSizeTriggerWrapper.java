package com.seven.asimov.it.utils.logcat.wrappers;

public class DebugDataSizeTriggerWrapper extends LogEntryWrapper {
    public DebugDataSizeTriggerWrapper(long timestamp) {
        setTimestamp(timestamp);
    }

    @Override
    public String toString() {
        return "Size trigger is active";
    }
}
