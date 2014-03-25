package com.seven.asimov.test.tool.core.testjobs;


import com.seven.asimov.test.tool.core.Request;
import com.seven.asimov.test.tool.core.Response;
import com.seven.asimov.test.tool.core.TestFactory;
import com.seven.asimov.test.tool.validation.ValidationFactory;

/**
 * TestJob class.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public class TestJob {

    private String mTestSuiteName;
    private final TestJobType mType;
    private TestJobEvent mState = TestJobEvent.EMPTY;
    private boolean mLongPoll;
    private Request mRequest;
    private Response mResponse;

    public TestJob(TestJobType type) {
        mType = type;
    }

    public TestJob(String testSuiteName, TestJobType type) {
        mTestSuiteName = testSuiteName;
        mType = type;
    }

    public void setState(TestJobEvent event) {
        this.mState = event;
    }

    public TestJobEvent getState() {
        return mState;
    }

    public void setIsLongPoll(boolean isLongPoll) {
        this.mLongPoll = isLongPoll;
    }

    public boolean isLongPoll() {
        return mLongPoll;
    }

    public TestJobType getType() {
        return mType;
    }

    public void setRequest(Request request) {
        this.mRequest = request;
    }

    public Request getRequest() {
        return mRequest;
    }

    public void setResponse(Response response) {
        this.mResponse = response;
    }

    public Response getResponse() {
        return mResponse;
    }

    public void setTestSuiteName(String testSuiteName) {
        this.mTestSuiteName = testSuiteName;
    }

    public String getTestSuiteName() {
        return mTestSuiteName;
    }

    private Boolean mValidatedOk;

    public void setValidatedOk(Boolean validatedOk) {
        this.mValidatedOk = validatedOk;
    }

    public Boolean isValidatedOk() {
        return mValidatedOk;
    }

    public Boolean validate(TestJobEvent event, int counter) {

        mValidatedOk = ValidationFactory.validate(this, event, counter, TestFactory.getTestSuite()
                .getVerificationPattern());

        return mValidatedOk;
    }
}
