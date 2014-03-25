package com.seven.asimov.it.utils.logcat.wrappers;

public class CLQConstructedDnsWrapper extends LogEntryWrapper{

    private static final String TAG = CLQConstructedDnsWrapper.class.getSimpleName();

    private String dispatcher;
    private String dtrx;
    private String destinationIp;
    private int destinationPort;

    public CLQConstructedDnsWrapper() {
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

    public String getDtrx() {
        return dtrx;
    }

    public void setDtrx(String dtrx) {
        this.dtrx = dtrx;
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public String toString() {
        return new StringBuilder().append(super.toString()).append(", dispatcher=").append(dispatcher)
                .append(", dtrx=").append(dtrx).append(", destinationIp=").append(destinationIp)
                .append(", destinationPort=").append(destinationPort).toString();
    }
}
