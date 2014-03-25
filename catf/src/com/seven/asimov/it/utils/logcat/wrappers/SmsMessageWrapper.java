package com.seven.asimov.it.utils.logcat.wrappers;

public class SmsMessageWrapper extends LogEntryWrapper{
    private static final String TAG = SmsMessageWrapper.class.getSimpleName();
    private String messageBody;

    public SmsMessageWrapper() {
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return "SmsMessageWrapper{" +
                "messageBody='" + messageBody + '\'' +
                '}';
    }
}
