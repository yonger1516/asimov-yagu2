package com.seven.asimov.test.tool.core.testjobs;

/**
 * TestJobState - Test job states enum.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public enum TestJobState {

    NOT_RUNNING("NOT_RUNNING"), IS_RUNNING("IS_RUNNING");

    private String mValue;

    public void setValue(String value) {
        this.mValue = value;
    }

    public String getValue() {
        return mValue;
    }

    TestJobState(String value) {
        this.mValue = value;
    }
}
