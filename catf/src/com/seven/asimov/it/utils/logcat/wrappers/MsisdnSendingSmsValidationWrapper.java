package com.seven.asimov.it.utils.logcat.wrappers;

public class MsisdnSendingSmsValidationWrapper extends LogEntryWrapper {
    private static final String TAG = MsisdnSendingSmsValidationWrapper.class.getSimpleName();

    private String phoneToSend;
    private String message;

    public String getPhoneToSend() {
        return phoneToSend;
    }

    public void setPhoneToSend(String phoneToSend) {
        this.phoneToSend = phoneToSend;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(" getPhoneToSend: ").append(getPhoneToSend()).append(" message: ").append(getMessage()).toString();
    }
}
