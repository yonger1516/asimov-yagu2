package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: user
 * Date: 9/19/13
 * Time: 4:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class HtrxNotOptimizingWrapper extends LogEntryWrapper {
    private String dispatcher;
    private String htrxID;
    private boolean optimize;
    private boolean transparent;

    public String getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(String dispatcher) {
        this.dispatcher = dispatcher;
    }

    public String getHtrxID() {
        return htrxID;
    }

    public void setHtrxID(String htrxID) {
        this.htrxID = htrxID;
    }

    public boolean isOptimize() {
        return optimize;
    }

    public void setOptimize(boolean optimize) {
        this.optimize = optimize;
    }

    public boolean isTransparent() {
        return transparent;
    }

    public void setTransparent(boolean transparent) {
        this.transparent = transparent;
    }
}
