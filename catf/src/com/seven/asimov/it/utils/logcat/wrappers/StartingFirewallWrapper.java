package com.seven.asimov.it.utils.logcat.wrappers;

public class StartingFirewallWrapper extends LogEntryWrapper {

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.append("StartingFirewallWrapper{timestamp=").append(getTimestamp()).append(" EntryNumber=").append(getEntryNumber()).append("}").toString();
    }
}
