package com.seven.asimov.it.utils.logcat.wrappers;

public class FirewallPolicyMgmtDataRequestWrapper extends LogEntryWrapper{

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.append("FirewallPolicyMgmtDataRequestWrapper{timestamp=").append(getTimestamp()).append(" EntryNumber=").append(getEntryNumber()).append("}").toString();
    }
}
