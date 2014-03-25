package com.seven.asimov.it.utils.logcat.wrappers;

/**
 * Created with IntelliJ IDEA.
 * User: akaliev
 * Date: 10/9/13
 * Time: 10:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class CCVWrapper extends LogEntryWrapper {
    private  String fck;
    private int payloadSize;

    public String getFck() {
        return fck;
    }

    public void setFck(String fck) {
        this.fck = fck;
    }

    public int getPayloadSize() {
        return payloadSize;
    }

    public void setPayloadSize(int payloadSize) {
        this.payloadSize = payloadSize;
    }

    @Override
    public String toString() {
        return super.toString()+" CCVWrapper{ fck='" + fck + '\'' +", payloadSize=" + payloadSize +'}';
    }
}
