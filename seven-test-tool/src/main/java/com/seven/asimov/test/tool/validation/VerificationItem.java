package com.seven.asimov.test.tool.validation;


/**
 * VerificationItem.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */

public class VerificationItem {

    public VerificationItem() {

    }

    private int mStatusCode;
    private int mSize;
    private String mHeaders;
    private String mBody;
    private int mBodySize;
    private int mResponseTime;
    private boolean mStatus;

    private VerificationTags mVTag;

    public void setTag(VerificationTags tag) {
        this.mVTag = tag;
    }

    public VerificationTags getTag() {
        return mVTag;
    }

    private VerificationOperators mVOperator;

    public void setOperator(VerificationOperators operator) {
        this.mVOperator = operator;
    }

    public VerificationOperators getOperator() {
        return mVOperator;
    }

    public void setStatusCode(int statusCode) {
        this.mStatusCode = statusCode;
    }

    public int getStatusCode() {
        return mStatusCode;
    }

    public void setSize(int size) {
        this.mSize = size;
    }

    public int getSize() {
        return mSize;
    }

    public String getHeaders() {
        return mHeaders;
    }

    public void setHeaders(String mHeaders) {
        this.mHeaders = mHeaders;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String mBody) {
        this.mBody = mBody;
    }

    public int getBodySize() {
        return mBodySize;
    }

    public void setBodySize(int mBodySize) {
        this.mBodySize = mBodySize;
    }

    public int getResponseTime() {
        return mResponseTime;
    }

    public void setResponseTime(int mResponseTime) {
        this.mResponseTime = mResponseTime;
    }

    public boolean getStatus() {
        return mStatus;
    }

    public void setStatus(boolean mStatus) {
        this.mStatus = mStatus;
    }
}
