package com.seven.asimov.it.utils.logcat.wrappers;

import java.lang.Override;
import java.lang.String;
import java.lang.StringBuilder;

public class DispatchersConfigurationInvalidWrapper extends LogEntryWrapper {

    private static final String TAG = DispatchersConfigurationInvalidWrapper.class.getSimpleName();
    private String dispatcherName;
    private String ports;
    private String type;
    private String zOrder;
    private boolean invalidMsgFound;


    public String getDispatcherName() {
        return dispatcherName;
    }

    public void setDispatcherName(String dispatcherName) {
        this.dispatcherName = dispatcherName;
    }

    public String getPorts() {
        return ports;
    }

    public void setPorts(String ports) {
        this.ports = ports;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getzOrder() {
        return zOrder;
    }

    public void setzOrder(String zOrder) {
        this.zOrder = zOrder;
    }

    public boolean isInvalidMsgFound() {
        return invalidMsgFound;
    }

    public void setInvalidMsgFound(boolean invalidMsgFound) {
        this.invalidMsgFound = invalidMsgFound;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(" dispatcher name=").append(dispatcherName)
                .append(", ports=").append(ports).append(", type=").append(type).append(", z-order=").append(zOrder)
                .append(", invalidMsgFound=").append(invalidMsgFound).toString();
    }
}
