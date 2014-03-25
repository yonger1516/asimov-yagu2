package com.seven.asimov.test.tool.utils;

/**
 * Parameter.
 *
 * @author Maksim Selivanov (mselivanov@seven.com)
 */
public class Parameter {

    public Parameter(String key, String value) {
        mKey = key;
        mValue = value;
    }

    private boolean mMultiLineHeader;
    private String mMultiLineHeaderPrefix;
    private String mKey;
    private String mValue;

    public void setKey(String key) {
        this.mKey = key;
    }

    public String getKey() {
        return mKey;
    }

    public void setValue(String value) {
        this.mValue = value;
    }

    public String getValue() {
        return mValue;
    }

    public void setMultiLineHeader(boolean multiLineHeader) {
        this.mMultiLineHeader = multiLineHeader;
    }

    public boolean isMultiLineHeader() {
        return mMultiLineHeader;
    }

    public String getMultiLineHeaderPrefix() {
        return mMultiLineHeaderPrefix;
    }

    public void setMultiLineHeaderPrefix(String multiLineHeaderPrefix) {
        this.mMultiLineHeaderPrefix = multiLineHeaderPrefix;
    }
}
