package com.seven.asimov.it.utils.logcat.wrappers;

public class CLQConstructedWrapper extends LogEntryWrapper{

    private static final String TAG = CLQConstructedWrapper.class.getSimpleName();

    private String dispatcher;
    private String htrx;
    private String csm;
    private String destinationIp;
    private int destinationPort;
    private int loPort;

    public CLQConstructedWrapper() {
    }

    public String getCsm() {
        return csm;
    }

    public void setCsm(String csm) {
        this.csm = csm;
    }

    public String getDestinationIp() {
        return destinationIp;
    }

    public void setDestinationIp(String destinationIp) {
        this.destinationIp = destinationIp;
    }

    public int getDestinationPort() {
        return destinationPort;
    }

    public void setDestinationPort(int destinationPort) {
        this.destinationPort = destinationPort;
    }

    public String getDispatcher() {
        return dispatcher;
    }

    public void setDispatcher(String dispatcher) {
        this.dispatcher = dispatcher;
    }

    public String getHtrx() {
        return htrx;
    }

    public void setHtrx(String htrx) {
        this.htrx = htrx;
    }

    public int getLoPort() {
        return loPort;
    }

    public void setLoPort(int loPort) {
        this.loPort = loPort;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(", dispatcher=").append(dispatcher)
                .append(", htrx=").append(htrx).append(", csm=").append(csm).append(", destinationIp=")
                .append(destinationIp).append(", destinationPort=").append(destinationPort).append(", loPort=")
                .append(loPort).toString();
    }
}
