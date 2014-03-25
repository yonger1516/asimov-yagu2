package com.seven.asimov.it.utils.logcat.wrappers;


public class RadioLogEntry extends CrcsEntry {

    private RadioStateType currentState;
    private RadioStateType previousState;
    private long timeInCurrentState;
    private int timeInPreviousState;
    private int optimization;

    public long getTimeInCurrentState() {
        return timeInCurrentState;
    }

    public void setTimeInCurrentState(long timeInCurrentState) {
        this.timeInCurrentState = timeInCurrentState;
    }

    public int getTimeInPreviousState() {
        return timeInPreviousState;
    }

    public void setTimeInPreviousState(int timeInPreviousState) {
        this.timeInPreviousState = timeInPreviousState;
    }

    public int getOptimization() {
        return optimization;
    }

    public void setOptimization(int optimization) {
        this.optimization = optimization;
    }

    public RadioStateType getCurrentState() {
        return currentState;
    }

    public void setCurrentState(RadioStateType currentState) {
        this.currentState = currentState;
    }

    public RadioStateType getPreviousState() {
        return previousState;
    }

    public void setPreviousState(RadioStateType previousState) {
        this.previousState = previousState;
    }

    @Override
    public String toString() {
        return "[Id=" + getId() + " LogId=" + getLogId() + " Time=" + getTimestamp() +
                " CurrentState=" + getCurrentState() + " PreviousState=" + getPreviousState() +
                " TimeInCurrSt=" + getTimeInCurrentState() +
                " TimeInPrevSt=" + timeInPreviousState + " Opt=" + optimization + "]";
    }

}
