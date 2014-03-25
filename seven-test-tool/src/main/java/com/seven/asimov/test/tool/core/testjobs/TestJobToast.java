package com.seven.asimov.test.tool.core.testjobs;

import java.io.Serializable;

/**
 * TestProgress class.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public class TestJobToast implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 5288270054681379072L;

    private TestJobType mTestJobType;
    private int mConnection;
    private String mHttpStatus;
    private String mConnectionStatus;
    private String mError;
    private String mMessage;
    private Boolean mValidated;

    public TestJobToast(TestJobType testJobType, int connection) {
        this.mTestJobType = testJobType;
        this.mConnection = connection;
    }

    public TestJobType getTestJobType() {
        return mTestJobType;
    }

    public void setConnection(int connection) {
        this.mConnection = connection;
    }

    public int getConnection() {
        return mConnection;
    }

    public void setHttpStatus(String httpStatus) {
        this.mHttpStatus = httpStatus;
    }

    public String getHttpStatus() {
        return mHttpStatus;
    }

    public void setConnectionStatus(String connectionStatus) {
        this.mConnectionStatus = connectionStatus;
    }

    public String getConnectionStatus() {
        return mConnectionStatus;
    }

    public void setError(String error) {
        this.mError = error;
    }

    public String getError() {
        return mError;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setValidated(Boolean isValidated) {
        this.mValidated = isValidated;
    }

    public Boolean isValidated() {
        return mValidated;
    }
}
