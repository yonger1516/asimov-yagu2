package com.seven.asimov.it.utils.logcat.wrappers;

public class RadioStateWrapper extends LogEntryWrapper {

    private int count;
    private boolean radioUp;
    private static final String TAG = RadioStateWrapper.class.getSimpleName();

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public boolean isRadioUp() {
        return radioUp;
    }

    public void setRadioUp(boolean radioUp) {
        this.radioUp = radioUp;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(" radio is ").append(isRadioUp() ? "up" : "down").toString();
    }
}
