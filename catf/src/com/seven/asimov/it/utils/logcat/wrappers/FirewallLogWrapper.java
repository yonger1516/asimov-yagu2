package com.seven.asimov.it.utils.logcat.wrappers;


public class FirewallLogWrapper extends LogEntryWrapper {
    String  time;
    String  firewall;
    String  version;
    String  type;
    long    chainID;
    String  name;
    String  action;
    String  event;
    String  networkIface;
    Long  sequenceNumber;
    private String IPVersion;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getFirewall() {
        return firewall;
    }

    public void setFirewall(String firewall) {
        this.firewall = firewall;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getChainID() {
        return chainID;
    }

    public void setChainID(long chainID) {
        this.chainID = chainID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getNetworkIface() {
        return networkIface;
    }

    public void setNetworkIface(String networkIface) {
        this.networkIface = networkIface;
    }

    public Long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(Long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }


    public void setIPVersion(String IPVersion) {
        this.IPVersion = IPVersion;
    }

    public String getIPVersion() {
        return IPVersion;
    }
}
