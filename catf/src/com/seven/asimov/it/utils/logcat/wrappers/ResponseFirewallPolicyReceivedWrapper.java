package com.seven.asimov.it.utils.logcat.wrappers;

public class ResponseFirewallPolicyReceivedWrapper extends LogEntryWrapper {
    private String errorCode;
    private String result;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        return stringBuilder.append("ResponseFirewallPolicyReceivedWrapper{timestamp=").append(getTimestamp()).append(" EntryNumber=").append(getEntryNumber()).append(" errorCode=").append(errorCode).append(" result=").append(result).append("}").toString();
    }
}
