package com.seven.asimov.it.utils.logcat.wrappers;

public class TrafficLogEntry extends CrcsEntry {

    private String typeTraffic;
    private int trafficTypeId;
    private int rx;
    private int tx;
    private int optimization;

    public String getTypeTraffic() {
        return typeTraffic;
    }

    public void setTypeTraffic(String typeTraffic) {
        this.typeTraffic = typeTraffic;
    }

    public int getRx() {
        return rx;
    }

    public void setRx(int rx) {
        this.rx = rx;
    }

    public int getTx() {
        return tx;
    }

    public void setTx(int tx) {
        this.tx = tx;
    }

    public int getOptimization() {
        return optimization;
    }

    public void setOptimization(int optimization) {
        this.optimization = optimization;
    }

    public int getTrafficTypeId() {
        return trafficTypeId;
    }

    public void setTrafficTypeId(int trafficTypeId) {
        this.trafficTypeId = trafficTypeId;
    }

    @Override
    public String toString() {
        return "[id=" + getId() + " Logid=" + getLogId() + " Time=" + getTimestamp() + " TypeTraffic=" + typeTraffic +
                " TypeTrafficId=" + trafficTypeId + " rx=" + rx + " tx=" + tx + " opt=" + optimization;
    }
}
