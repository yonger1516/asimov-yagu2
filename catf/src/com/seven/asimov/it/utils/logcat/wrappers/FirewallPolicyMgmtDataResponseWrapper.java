package com.seven.asimov.it.utils.logcat.wrappers;

public class FirewallPolicyMgmtDataResponseWrapper extends LogEntryWrapper {

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.append("FirewallPolicyMgmtDataResponseWrapper{timestamp=").append(getTimestamp()).append(" EntryNumber=").append(getEntryNumber()).append("}").toString();
    }
}
