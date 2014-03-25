package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: imiflig
 * Date: 11/21/13
 * Time: 2:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogLabelWrapper extends LogEntryWrapper {
    private static final String TAG = LogLabelWrapper.class.getSimpleName();

    private String tag;
    private String label;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder((super.toString())).append("; tag: ").append(tag).append("; label: ").append(label).toString();
    }
}
