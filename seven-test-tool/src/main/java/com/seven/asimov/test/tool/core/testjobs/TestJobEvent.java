package com.seven.asimov.test.tool.core.testjobs;

/**
 * TestJobEvent class.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public enum TestJobEvent {

    PIPELINE_SOCKET_CLOSED(-1), EMPTY(0), PIPELINE_STARTED(1), PIPELINE_COMPLETED(2), REQUEST_STARTED(10), REQUEST_HEADER(
            11), REQUEST_COMPLETED(12), RESPONSE_STARTED(20), RESPONSE_HEADER_STARTED(21), RESPONSE_HEADER_COMPLETED(22), RESPONSE_BODY_STARTED(
            23), RESPONSE_BODY_READING(24), RESPONSE_BODY_COMPLETED(25), RESPONSE_COMPLETED(26);

    private int mValue;

    public void setValue(int value) {
        this.mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    TestJobEvent(int value) {
        this.mValue = value;
    }
}
